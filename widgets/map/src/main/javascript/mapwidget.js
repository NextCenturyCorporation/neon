

neon.ready(function () {
    var databaseName;
    var tableName;
    var filterKey;
    var map;

    var clientId = neon.eventing.messaging.getInstanceId();

    var options = ['latitude', 'longitude', 'color-by', 'size-by'];

    neon.eventing.messaging.registerForNeonEvents({
        activeDatasetChanged: onActiveDatasetChanged,
        filtersChanged: onFiltersChanged
    });

    initialize();
    restoreState();

    function initialize() {
        neon.query.SERVER_URL = $("#neon-server").val();
        map = new coreMap.Map("map");
        installOptionsPanels();
        setMapMappingFunctions();
        setLayerChangeListener();
        setApplyFiltersListener();
        map.draw();
        map.register("moveend", this, onMapMovement);
    }

    function onMapMovement() {
        var checked = $("#auto-filter").is(':checked');

        if (checked && neon.mapWidgetUtils.latitudeAndLongitudeAreSelected()) {
            $("#map-redraw-button").attr("disabled", true);
            var filter = createFilterFromExtent();
            neon.eventing.publishing.replaceFilter(filterKey, filter);
        }
        else {
            $("#map-redraw-button").removeAttr("disabled");
            var query = buildQuery();
            var stateObject = buildStateObject(query);
            neon.query.saveState(clientId, stateObject);
        }
    }

    function installOptionsPanels() {
        neon.toggle.createOptionsPanel("#options-panel");
        $("#functions-toggle-id").click(function () {
            $("#functions-panel").slideToggle("slow");
        });
    }

    function setMapMappingFunctions() {
        neon.mapWidgetUtils.addDropdownChangeListener("latitude", function (value) {
            map.latitudeMapping = value;
        });
        neon.mapWidgetUtils.addDropdownChangeListener("longitude", function (value) {
            map.longitudeMapping = value;
        });
        neon.mapWidgetUtils.addDropdownChangeListener("size-by", function (value) {
            if (value) {
                map.sizeMapping = value;
            }
            else {
                map.sizeMapping = coreMap.Map.DEFAULT_SIZE_MAPPING;
            }
        });
        neon.mapWidgetUtils.addDropdownChangeListener("color-by", function (value) {
            map.categoryMapping = value;
        });
    }

    function setLayerChangeListener() {
        neon.mapWidgetUtils.setLayerChangeListener(function () {
            if ($('#points').is(':checked')) {
                map.toggleLayers();
                $('#color-by-group').show();
            }
            else {
                map.toggleLayers();
                $('#color-by-group').hide();
            }
            var query = buildQuery();
            var stateObject = buildStateObject(query);
            neon.query.saveState(clientId, stateObject);
        });
    }

    function setApplyFiltersListener() {
        $('#map-redraw-button').click(function () {
            var filter = createFilterFromExtent();
            neon.eventing.publishing.replaceFilter(filterKey, filter);
        });
        $('#map-reset-button').click(function () {
            map.reset();
            var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
            neon.eventing.publishing.replaceFilter(filterKey, filter);

        });
        $('#auto-filter').click(onMapMovement);
    }

    function createFilterFromExtent() {
        var lonField = neon.mapWidgetUtils.getLongitudeField();
        var latField = neon.mapWidgetUtils.getLatitudeField();

        var extent = map.getExtent();

        var leftClause = neon.query.where(lonField, ">=", extent.minimumLongitude);
        var rightClause = neon.query.where(lonField, "<=", extent.maximumLongitude);
        var bottomClause = neon.query.where(latField, ">=", extent.minimumLatitude);
        var topClause = neon.query.where(latField, "<=", extent.maximumLatitude);
        var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);

        //Deal with different dateline crossing scenarios.
        if (extent.minimumLongitude < -180 && extent.maximumLongitude > 180) {
            filterClause = neon.query.and(topClause, bottomClause);
        }
        else if (extent.minimumLongitude < -180) {
            leftClause = neon.query.where(lonField, ">=", extent.minimumLongitude + 360);
            var leftDateLine = neon.query.where(lonField, "<=", 180);
            var rightDateLine = neon.query.where(lonField, ">=", -180);
            var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
            filterClause = neon.query.and(topClause, bottomClause, datelineClause);
        }
        else if (extent.maximumLongitude > 180) {
            rightClause = neon.query.where(lonField, "<=", extent.maximumLongitude - 360);
            var rightDateLine = neon.query.where(lonField, ">=", -180);
            var leftDateLine = neon.query.where(lonField, "<=", 180);
            var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
            filterClause = neon.query.and(topClause, bottomClause, datelineClause);
        }

        return new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
    }

    function onActiveDatasetChanged(message) {
        databaseName = message.database;
        tableName = message.table;
        map.reset();
        neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
            filterKey = filterResponse;
        });
        neon.query.getFieldNames(databaseName, tableName, neon.widget.MAP, populateFromColumns);
    }

    function populateFromColumns(data) {
        var elements = [new neon.dropdown.Element("latitude", "numeric"), new neon.dropdown.Element("longitude", "numeric"),
            new neon.dropdown.Element("color-by", ["text", "numeric"]), new neon.dropdown.Element("size-by", "numeric")
        ];
        neon.dropdown.populateAttributeDropdowns(data, elements, queryForMapData);
    }

    function onFiltersChanged() {
        queryForMapData();
    }

    function queryForMapData() {
        ensureMappings();
        if (!neon.mapWidgetUtils.latitudeAndLongitudeAreSelected()) {
            return;
        }

        var query = buildQuery();
        var stateObject = buildStateObject(query);
        neon.query.executeQuery(query, redrawMapData);
        neon.query.saveState(clientId, stateObject);
    }

    function ensureMappings() {
        map.latitudeMapping = neon.mapWidgetUtils.getLatitudeField();
        map.longitudeMapping = neon.mapWidgetUtils.getLongitudeField();
        if (neon.mapWidgetUtils.getSizeByField()) {
            map.sizeMapping = neon.mapWidgetUtils.getSizeByField();
        }
        else {
            map.sizeMapping = coreMap.Map.DEFAULT_SIZE_MAPPING;
        }
        map.categoryMapping = neon.mapWidgetUtils.getColorByField();
    }

    function buildQuery() {
        var query = new neon.query.Query().selectFrom(databaseName, tableName);
        appendGroupByClause(query);
        appendSizeByFieldClause(query);
        return query;
    }

    function appendGroupByClause(query) {
        var groupByFields = [neon.mapWidgetUtils.getLatitudeField(), neon.mapWidgetUtils.getLongitudeField()];
        var colorByField = neon.mapWidgetUtils.getColorByField();

        if (colorByField) {
            groupByFields.push(colorByField);
        }
        neon.query.Query.prototype.groupBy.apply(query, groupByFields);
    }

    function appendSizeByFieldClause(query) {
        // if a specific field to size the radius by was chosen, use that. otherwise use a generic count
        var sizeByField = neon.mapWidgetUtils.getSizeByField();
        if (sizeByField) {
            query.aggregate(neon.query.SUM, sizeByField, sizeByField);
        }
        else {
            query.aggregate(neon.query.COUNT, '*', coreMap.Map.DEFAULT_SIZE_MAPPING);
        }
    }

    function redrawMapData(mapData) {
        map.setData(mapData.data);
        map.draw();
    }

    function buildStateObject(query) {
        return {
            filterKey: filterKey,
            columns: neon.dropdown.getFieldNamesFromDropdown("latitude"),
            selectedLatitude: neon.mapWidgetUtils.getLatitudeField(),
            selectedLongitude: neon.mapWidgetUtils.getLongitudeField(),
            selectedColorBy: neon.mapWidgetUtils.getColorByField(),
            selectedSizeBy: neon.mapWidgetUtils.getSizeByField(),
            selectedLayer: neon.mapWidgetUtils.getLayer(),
            selectedExtent: map.getExtent(),
            autoFilterChecked: $("#auto-filter").is(':checked'),
            query: query
        };
    }

    function restoreState() {
        neon.query.getSavedState(clientId, function (data) {
            filterKey = data.filterKey;
            if (!filterKey) {
                return;
            }
            databaseName = data.filterKey.dataSet.databaseName;
            tableName = data.filterKey.dataSet.tableName;
            var elements = [new neon.dropdown.Element("latitude", "numeric"), new neon.dropdown.Element("longitude", "numeric"),
                new neon.dropdown.Element("color-by", ["text", "numeric"]), new neon.dropdown.Element("size-by", "numeric")
            ];
            neon.dropdown.populateAttributeDropdowns(data.columns, elements, queryForMapData);
            neon.dropdown.setDropdownInitialValue("latitude", data.selectedLatitude);
            neon.dropdown.setDropdownInitialValue("longitude", data.selectedLongitude);
            neon.dropdown.setDropdownInitialValue("color-by", data.selectedColorBy);
            neon.dropdown.setDropdownInitialValue("size-by", data.selectedSizeBy);

            _.each(options, function (selector) {
                $('#' + selector).change();
            });

            if (data.autoFilterChecked) {
                $('#auto-filter').attr('checked', true);
            }

            neon.mapWidgetUtils.setLayer(data.selectedLayer);
            if (data.selectedLayer === "heatmap") {
                map.toggleLayers();
                $('#color-by-group').hide();
                $('#heatmap').attr('checked', true);
            }
            map.zoomToExtent(data.selectedExtent);

            neon.query.executeQuery(data.query, redrawMapData);
        });
    }

});
