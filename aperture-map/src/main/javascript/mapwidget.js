$(document).ready(function () {
    // TODO: Map legend for ordinal color values
    // TODO: Extract out OWF code from generic map widget code (in this case we may not bother since we may develop our own map)
    // TODO: Make the default attributes configurable (again may not want to bother since we may develop our own map)

    OWF.ready(function () {
        var databaseName;
        var tableName;
        var filterKey;

        var latField;
        var lonField;

        var currentData;

        function isNumeric(field) {
            return $.isNumeric(currentData[field]);
        }

        // a counter of how many items are in each location group used to size the dots if no other size-by attribute is provided
        var COUNT_FIELD_NAME = 'count_';
        var ORDINAL_COLORS = d3.scale.category20().range().map(function (color) {
            return new aperture.Color(color);
        });
        var HEATMAP_COLORS = [new aperture.Color('#FFFF00'), new aperture.Color('#FF0000')];
        var DEFAULT_FILL_COLOR = new aperture.Color('#00FF00');
        var MIN_RADIUS = 5;
        var MAX_RADIUS = 25;

        var map, locationsLayer, pointsLayer;


        // instantiating the message handler adds it as a listener
        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: onDatasetChanged,
            filtersChanged: onFiltersChanged
        });
        var eventPublisher = new neon.eventing.OWFEventPublisher(messageHandler);

        function onFiltersChanged(message) {
            redrawMap();
        }

        function onDatasetChanged(message) {
            databaseName = message.database;
            tableName = message.table;
            neon.query.registerForFilterKey(databaseName, tableName, function(filterResponse){
                filterKey = filterResponse;
            });

            neon.query.getFieldNames(databaseName, tableName, populateAttributeDropdowns);
        }

        function populateAttributeDropdowns(data) {
            ['latitude', 'longitude', 'color-by', 'size-by'].forEach(function (selectId) {
                var select = $('#' + selectId);
                select.empty();
                select.append($('<option></option>').attr('value', '').text('(Select Field)'));
                data.fieldNames.forEach(function (field) {
                    select.append($('<option></option>').attr('value', field).text(field));
                });
                select.change(redrawMap);
            });
        }

        function initMap() {
            map = new aperture.geo.Map('#map');
            locationsLayer = map.addLayer(aperture.geo.MapNodeLayer);
            pointsLayer = locationsLayer.addLayer(aperture.RadialLayer);
            pointsLayer.map('stroke').asValue('#888');
            map.zoomTo(0, 0, 2);
            map.on("zoom", onExtentChanged);
            map.on("panend", onExtentChanged);
        }

        function onExtentChanged() {
            var extent = map.map_.getExtent();
            var llPoint = new OpenLayers.LonLat(extent.left, extent.bottom);
            var urPoint = new OpenLayers.LonLat(extent.right, extent.top);
            var proj_1 = new OpenLayers.Projection("EPSG:4326");
            var proj_2 = new OpenLayers.Projection("EPSG:900913");
            llPoint.transform(proj_2, proj_1);
            urPoint.transform(proj_2, proj_1);
            var minLon = Math.min(llPoint.lon,urPoint.lon);
            var maxLon = Math.max(llPoint.lon,urPoint.lon);

            var minLat = Math.min(llPoint.lat,urPoint.lat);
            var maxLat = Math.max(llPoint.lat,urPoint.lat);

            var leftClause = neon.query.where(lonField, ">=", minLon);
            var rightClause = neon.query.where(lonField, "<=", maxLon);
            var bottomClause = neon.query.where(latField, ">=", minLat);
            var topClause = neon.query.where(latField, "<=", maxLat);
            var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);
            var filter = new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);

            eventPublisher.replaceFilter(filterKey, filter);
        }

        function redrawMap() {
            var query = new neon.query.Query().selectFrom(databaseName, tableName).limit(1);
            neon.query.executeQuery(query, function(results){
                if(results.data[0] !== undefined){
                    currentData = results.data[0];
                }
                latField = getLatField();
                lonField = getLonField();
                var sizeByField = getSizeByField();
                var colorByField = getColorByField();

                if (latField && lonField) {
                    var query = buildQuery(latField, lonField, sizeByField, colorByField);
                    neon.query.executeQuery(query, doRedrawMap);
                }
                else {
                    locationsLayer.all([]);
                    map.all().redraw();
                }
            });
        }

        function buildQuery(latField, lonField, sizeByField, colorByField) {
            var query = new neon.query.Query().selectFrom(databaseName, tableName);
            appendGroupByClause(query, latField, lonField, colorByField);
            appendSizeByFieldClause(query, sizeByField);
            return query;
        }

        function appendGroupByClause(query, latField, lonField, colorByField) {
            var groupByFields = [latField, lonField];

            // if there is a color by field, either sum it or group by its values depending on whether it is numeric or not
            if (colorByField) {
                if (isNumeric(colorByField)) {
                    query.aggregate(neon.query.SUM, colorByField, colorByField);
                }
                else {
                    groupByFields.push(colorByField);
                }
            }
            neon.query.Query.prototype.groupBy.apply(query, groupByFields);
        }

        function appendSizeByFieldClause(query, sizeByField) {
            // if a specific field to size the radius by was chosen, use that. otherwise use a generic count
            if (sizeByField) {
                query.aggregate(neon.query.SUM, sizeByField, sizeByField);
            }
            else {
                query.aggregate(neon.query.COUNT, null, COUNT_FIELD_NAME);
            }
        }

        function doRedrawMap(queryResults) {
            var data = queryResults.data;

            var latField = getLatField();
            var lonField = getLonField();
            locationsLayer.map('latitude').from(latField);
            locationsLayer.map('longitude').from(lonField);

            var colorByField = getColorByField();
            var sizeByField = getSizeByField();

            // If no size by attribute is provided, just use a raw count
            sizeByField = sizeByField || COUNT_FIELD_NAME;
            var minSize = minValue(data, sizeByField);
            var maxSize = maxValue(data, sizeByField);
            var countScale = new aperture.Scalar('size', [minSize, maxSize]).mapKey([MIN_RADIUS, MAX_RADIUS]);

            pointsLayer.map('radius').from(sizeByField).using(countScale);
            pointsLayer.map('opacity').asValue(0.8);
            applyFill(colorByField, data);

            locationsLayer.all(data);
            map.all().redraw();
        }

        function applyFill(colorByField, data) {
            if (colorByField) {
                var colorScale;
                // if coloring by a numeric field, find the min/max values and map the other values in between
                // otherwise, map unique values to specific colors
                if (isNumeric(colorByField)) {
                    var min = minValue(data, colorByField);
                    var max = maxValue(data, colorByField);
                    colorScale = new aperture.Scalar('color', [min, max]).mapKey(HEATMAP_COLORS);
                }
                else {
                    var values = uniqueValues(data, colorByField);
                    colorScale = new aperture.Ordinal('color', values).mapKey(ORDINAL_COLORS);
                }
                pointsLayer.map('fill').from(colorByField).using(colorScale);
            }
            else {
                pointsLayer.map('fill').asValue(DEFAULT_FILL_COLOR);
            }
        }

        function getLatField() {
            return $('#latitude option:selected').val();
        }

        function getLonField() {
            return $('#longitude option:selected').val();
        }

        function getSizeByField() {
            return $('#size-by option:selected').val();
        }

        function getColorByField() {
            return $('#color-by option:selected').val();
        }

        function uniqueValues(data, attribute) {
            return d3.nest().key(function (el) {
                return el[attribute];
            }).map(data, d3.map).keys();
        }

        function minValue(data, attribute) {
            return d3.min(data, function (el) {
                return el[attribute];
            });
        }

        function maxValue(data, attribute) {
            return d3.max(data, function (el) {
                return el[attribute];
            });
        }


        initMap();

    });

});


