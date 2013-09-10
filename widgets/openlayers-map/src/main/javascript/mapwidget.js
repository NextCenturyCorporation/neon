$(document).ready(function () {

    OWF.ready(function () {
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = $("#neon-server").val();

        var databaseName;
        var tableName;
        var filterKey;

        var latField;
        var lonField;
        var heatmap;

        var currentData;

        function isNumeric(field) {
            return $.isNumeric(currentData[field]);
        }

        // a counter of how many items are in each location group used to size the dots if no other size-by attribute is provided
        var COUNT_FIELD_NAME = 'count_';

        var map;

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
            neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
                filterKey = filterResponse;
            });

            neon.query.getFieldNames(databaseName, tableName, populateFromColumns);
        }

        function populateFromColumns(data) {
            neon.dropdown.populateAttributeDropdowns(data, ['latitude', 'longitude', 'color-by', 'size-by'], redrawMap);
        }

        function initMap() {
            map = new OpenLayers.Map('map');
            layer = new OpenLayers.Layer.OSM();

            // create our heatmap layer
            heatmap = new OpenLayers.Layer.Heatmap("Heatmap Layer", map, layer, {visible: true, radius: 10}, {isBaseLayer: false, opacity: 0.3, projection: new OpenLayers.Projection("EPSG:4326")});
            map.addLayers([layer, heatmap]);

            map.zoomToMaxExtent();

            map.events.register("moveend", map, onExtentChanged);
        }

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
                    neon.query.executeQuery(query, doRedrawMap);
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

            heatmap.setDataSet(transformedTestData);
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

        neon.toggle.createOptionsPanel("#options-panel");
        initMap();

    });

});


