
$(document).ready(function () {

    OWF.ready(function () {

        var kmlLayer;
        var map;
        var mapBaseColor = new aperture.Color('#3f3f3f');
        var owfEventPublisher
        var dataSourceName;
        var datasetId;
        var category;
        var aggregateField;
        var aggregationOperation;
        var selection = new aperture.Set('name');
        var aggregateValuesByCategory;
        var dataLookup = function () {
            return aggregateValuesByCategory[this.name];
        };

        /** this "connected" variable indicates if the query widget has told the map to display data */
        var connected = false;


        initOwfEventPublisher();
        initMap();

        $('#initializing').remove();

        // This is invoked when the query widget tells the map widget to display the data
        OWF.Intents.receive(
            {
                action: 'map',
                dataType: 'application/vnd.neon.map.categorical'
            },
            function (sender, intent, data) {
                connected = true;
                dataSourceName = data.dataSourceName;
                datasetId = data.datasetId;
                aggregateField = data.aggregateField;
                aggregationOperation = data.aggregationOperation;

                // TODO: Show the different categories that are available to chart by. For now, just use the first one.
                var categories = data.categories;
                category = categories[0];
                loadData();
            }
        );

        function initOwfEventPublisher() {
            var messageHandler = new neon.eventing.MessageHandler({
                filtersChanged: loadData,
                selectionChanged: redraw
            });
            owfEventPublisher = new neon.query.OWFEventPublisher(messageHandler);
        }

        function initMap() {
            map = new aperture.geo.Map({
                id: 'map-display'
            });
            map.setExtents(-126.210937, 48.922499, -65.730469, 24.839076);
            addKmlLayer();
            redraw();
        }

        function addKmlLayer() {
            kmlLayer = map.addLayer(aperture.geo.MapGISLayer, {}, {
                format: "KML",
                url: "data/us_states.kml"
            });

            // set default values
            kmlLayer.map('stroke').asValue('#888');
            kmlLayer.map('stroke-width').asValue(.5);
            kmlLayer.map('fill').asValue(mapBaseColor);

            // mapping functions for the selected states
            kmlLayer.map('stroke').filter(selection.constant(aperture.palette.color('selected')));
            kmlLayer.map('stroke-width').filter(selection.constant(5));
            addClickListener();
        }

        function addClickListener() {
            // This is just a simple handler that always replaces the current selection
            kmlLayer.on('click', function (evt) {
                var selected = evt.data.name;
                if (!selection.contains(selected)) {
                    selection.clear();
                    selection.add(selected);
                    with (neon.query) {
                        // set all items where the category (state) equals the selected state
                        var filter = new Filter().selectFrom(dataSourceName, datasetId).where(category, '=', selected);
                        owfEventPublisher.setSelectionWhere(filter);
                    }
                }
                else {
                    selection.remove(selected);
                    owfEventPublisher.clearSelection();
                }
                redraw();
            });
        };

        function redraw() {
            // TODO: Is there a way to just redraw the states that changed?
            map.all().redraw();
        }

        function loadData() {
            if (connected) {
                with (neon.query) {
                    // aggregate the data by category (state)
                    var query = new Query().selectFrom(dataSourceName, datasetId).groupBy(category).aggregate(aggregationOperation, aggregateField, 'aggregate');
                    executeQuery(query, function (results) {
                        var data = results.data;
                        loadChloropleth(data);
                    });
                }
            }
        }

        function loadChloropleth(data) {
            aggregateValuesByCategory = {};
            var mapRange = new aperture.Scalar('values');
            var mapKey = mapRange.mapKey([new aperture.Color('#737'), new aperture.Color('#fbf')]);
            populateAggregateValuesMap(data, mapRange);
            fillStates(mapKey);
            redraw();
        }

        function populateAggregateValuesMap(data, mapRange) {
            data.forEach(function (row) {
                var value = row.aggregate;
                mapRange.expand(value);
                aggregateValuesByCategory[row[category]] = value;
            });
        }

        function fillStates(mapKey) {
            kmlLayer.map('fill').from(dataLookup).using(mapKey).filter(
                function (color) {
                    if (!dataLookup.call(this)) {
                        return mapBaseColor;
                    }
                    return color;
                }
            );
        }
    });
});


