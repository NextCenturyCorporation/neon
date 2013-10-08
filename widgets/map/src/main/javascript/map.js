$(function () {

    OWF.ready(function () {
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = $("#neon-server").val();

        var databaseName;
        var tableName;
        var filterKey;

        var latField;
        var lonField;
        var heatmapLayer;
        var pointsLayer;

        var currentData;
        var clientId = OWF.getInstanceId();

        function isNumeric(field) {
            return $.isNumeric(currentData[field]);
        }

        // a counter of how many items are in each location group used to size the dots if no other size-by attribute is provided
        var COUNT_FIELD_NAME = 'count_';

        var MIN_RADIUS = 3;
        var MAX_RADIUS = 20;

        var map;

        // instantiating the message handler adds it as a listener
        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: onDatasetChanged,
            filtersChanged: onFiltersChanged
        });
        var eventPublisher = new neon.eventing.OWFEventPublisher(messageHandler);

        neon.toggle.createOptionsPanel("#options-panel");
        initMap();
        restoreState();

        function onFiltersChanged(message) {
            redrawMap();
        }

        function onDatasetChanged(message) {
            databaseName = message.database;
            tableName = message.table;
            neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
                filterKey = filterResponse;
            });
            neon.query.getFieldNames(databaseName, tableName, neon.widget.MAP, populateFromColumns);
        }

        function populateFromColumns(data) {
            neon.dropdown.populateAttributeDropdowns(data, ['latitude', 'longitude', 'color-by', 'size-by'], redrawMap);
        }

        function initMap() {
            map = new OpenLayers.Map('map');
            layer = new OpenLayers.Layer.OSM();

            //default styling for points layer
            var defaultStyle = new OpenLayers.StyleMap(OpenLayers.Util.applyDefaults(
                {fillColor: "#00FF00", fillOpacity:.8, strokeOpacity:.8, strokeWidth: 1, pointRadius: "4"},
                OpenLayers.Feature.Vector.style["default"]
            ));

            // create heatmap and points layers
            heatmapLayer = new OpenLayers.Layer.Heatmap("Heatmap Layer", map, layer, {visible: true, radius: 10}, {isBaseLayer: false, opacity: 0.3, projection: new OpenLayers.Projection("EPSG:4326")});
            pointsLayer = new OpenLayers.Layer.Vector("Points Layer", {styleMap: defaultStyle});

            //add base map layer
            map.addLayer(layer);

            //add default points layer
            map.addLayer(pointsLayer);

            map.zoomToMaxExtent();

            map.events.register("moveend", map, onExtentChanged);
        }

        $(document).ready(function() {
            $("input[name='layer-group']").change(function() {
                if($('#points').is(':checked')) {
                    map.addLayer(pointsLayer);
                    map.removeLayer(heatmapLayer);
                    $('#size-by-group').show();
                    $('#color-by-group').show();
                }
                else {
                    map.removeLayer(pointsLayer);
                    map.addLayer(heatmapLayer);
                    $('#size-by-group').hide();
                    $('#color-by-group').hide();
                }
            });
        });

        function onExtentChanged() {
            if (!(latField && lonField)) {
                return;
            }

            var extent = map.getExtent();
            var llPoint = new OpenLayers.LonLat(extent.left, extent.bottom);
            var urPoint = new OpenLayers.LonLat(extent.right, extent.top);
            var proj_1 = new OpenLayers.Projection("EPSG:4326");
            var proj_2 = new OpenLayers.Projection("EPSG:900913");
            llPoint.transform(proj_2, proj_1);
            urPoint.transform(proj_2, proj_1);
            var minLon = Math.min(llPoint.lon, urPoint.lon);
            var maxLon = Math.max(llPoint.lon, urPoint.lon);

            var minLat = Math.min(llPoint.lat, urPoint.lat);
            var maxLat = Math.max(llPoint.lat, urPoint.lat);

            var leftClause = neon.query.where(lonField, ">=", minLon);
            var rightClause = neon.query.where(lonField, "<=", maxLon);
            var bottomClause = neon.query.where(latField, ">=", minLat);
            var topClause = neon.query.where(latField, "<=", maxLat);
            var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);
            var filter = new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);

            eventPublisher.replaceFilter(filterKey, filter);
        }

        function redrawMap() {
            latField = getLatField();
            lonField = getLonField();

            if (latField && lonField) {
                var query = new neon.query.Query().selectFrom(databaseName, tableName).limit(1);
                neon.query.executeQuery(query, function (results) {
                    if (results.data[0] !== undefined) {
                        currentData = results.data[0];
                    }
                    var sizeByField = getSizeByField();
                    var colorByField = getColorByField();


                    var query = buildQuery(latField, lonField, sizeByField, colorByField);
                    var stateObject = buildStateObject(query);
                    neon.query.executeQuery(query, doRedrawMap);
                    neon.query.saveState(clientId, stateObject);
                });
            }
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

            addFeaturesToPointsLayer(data);

            var transformedTestData = { max: 1, data: [] },
                datalen = data.length,
                nudata = [];

            while (datalen--) {
                nudata.push({
                    lonlat: new OpenLayers.LonLat(data[datalen][lonField], data[datalen][latField]),
                    count: data[datalen].count_
                });
            }

            transformedTestData.data = nudata;

            heatmapLayer.setDataSet(transformedTestData);
        }

        function addFeaturesToPointsLayer(data) {
            var latField = getLatField();
            var lonField = getLonField();
            var newData = [];

            var colorByField = getColorByField();
            var sizeByField = getSizeByField();

            // If no size by attribute is provided, just use a raw count
            sizeByField = sizeByField || COUNT_FIELD_NAME;

            _.each(data, function (element) {
                var point = new OpenLayers.Geometry.Point(element[lonField], element[latField]);
                point.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
                var feature = new OpenLayers.Feature.Vector(point);

                //possible values for radius are 5-25, this formula ensures that all radii are in that range
                var radius = Math.floor((element[sizeByField] % 16) + 5);

                feature.style = new OpenLayers.Symbolizer.Point({
                    fillColor: "#00FF00",
                    fillOpacity:.8,
                    strokeOpacity:.8,
                    strokeWidth: 1,
                    pointRadius: radius
                });

                newData.push(feature);
            });

            pointsLayer.removeAllFeatures();
            pointsLayer.addFeatures(newData);
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

        function buildStateObject(query) {
            return {
                filterKey: filterKey,
                columns: neon.dropdown.getFieldNamesFromDropdown("latitude"),
                selectedLatitude: getLatField(),
                selectedLongitude: getLonField(),
                selectedColorBy: getColorByField(),
                selectedSizeBy: getSizeByField(),
                query: query
            };
        }

        function restoreState() {
            neon.query.getSavedState(clientId, function (data) {
                filterKey = data.filterKey;
                databaseName = data.filterKey.dataSet.databaseName;
                tableName = data.filterKey.dataSet.tableName;
                neon.dropdown.populateAttributeDropdowns(data.columns, ['latitude', 'longitude', 'color-by', 'size-by'], redrawMap);
                neon.dropdown.setDropdownInitialValue("latitude", data.selectedLatitude);
                neon.dropdown.setDropdownInitialValue("longitude", data.selectedLongitude);
                neon.dropdown.setDropdownInitialValue("color-by", data.selectedColorBy);
                neon.dropdown.setDropdownInitialValue("size-by", data.selectedSizeBy);

                latField = getLatField();
                lonField = getLonField();
                neon.query.executeQuery(data.query, doRedrawMap);
            });
        }

    });

});


