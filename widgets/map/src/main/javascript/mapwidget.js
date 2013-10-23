/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

$(function () {

    OWF.ready(function () {
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = $("#neon-server").val();

        var databaseName;
        var tableName;
        var filterKey;
        var clientId = OWF.getInstanceId();

        var options = ['latitude', 'longitude', 'color-by', 'size-by'];

        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: onActiveDatasetChanged,
            filtersChanged: onFiltersChanged
        });
        var eventPublisher = new neon.eventing.OWFEventPublisher(messageHandler);

        var map;

        neon.toggle.createOptionsPanel("#options-panel");
        initialize();
        restoreState();

        function initialize() {
            map = new coreMap.Map("map");
            setMapMappingFunctions();
            setLayerChangeListener();
            setApplyFiltersListener();
            map.draw();
            map.register("moveend", this, onMapMovement);
        }

        function onMapMovement() {
            var query = buildQuery();
            var stateObject = buildStateObject(query);
            neon.query.saveState(clientId, stateObject);
        }

        function setMapMappingFunctions() {
            neon.mapWidgetUtils.addDropdownChangeListener("latitude", function (value) {
                map.latitudeMapping = value;
            });
            neon.mapWidgetUtils.addDropdownChangeListener("longitude", function (value) {
                map.longitudeMapping = value;
            });
            neon.mapWidgetUtils.addDropdownChangeListener("size-by", function (value) {
                map.sizeMapping = value;
            });
            neon.mapWidgetUtils.addDropdownChangeListener("color-by", function (value) {
                map.colorMapping = value;
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

        function setApplyFiltersListener(){
            $('#map-redraw-button').click(function() {
                var filter = createFilterFromExtent();
                eventPublisher.replaceFilter(filterKey, filter);
            });
        }

        function createFilterFromExtent(){
            var lonField = neon.mapWidgetUtils.getLongitudeField();
            var latField = neon.mapWidgetUtils.getLatitudeField();

            var extent = map.getExtent();
            var leftClause = neon.query.where(lonField, ">=", extent.minimumLongitude);
            var rightClause = neon.query.where(lonField, "<=", extent.maximumLongitude);
            var bottomClause = neon.query.where(latField, ">=", extent.minimumLatitude);
            var topClause = neon.query.where(latField, "<=", extent.maximumLatitude);
            var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);

            //if the current extent includes the international date line
            if(extent.minimumLongitude && extent.maximumLongitude < 0) {
                leftClause = neon.query.where(lonField, ">=", extent.minimumLongitude + 360);
                var leftDateLine = neon.query.where(lonField, "<=", 180);
                var rightDateLine = neon.query.where(lonField, ">=", -180);

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
            neon.dropdown.populateAttributeDropdowns(data, options, queryForMapData);
        }

        function onFiltersChanged() {
            queryForMapData();
        }

        function queryForMapData(){
            if(!neon.mapWidgetUtils.latitudeAndLongitudeAreSelected()){
                return;
            }

            var query = buildQuery();
            var stateObject = buildStateObject(query);
            neon.query.executeQuery(query, redrawMapData);
            neon.query.saveState(clientId, stateObject);
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
                query.aggregate(neon.query.COUNT, null, coreMap.Map.DEFAULT_SIZE_MAPPING);
            }
        }

        function redrawMapData(mapData){
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
                query: query
            };
        }

        function restoreState() {
            neon.query.getSavedState(clientId, function (data) {
                filterKey = data.filterKey;
                databaseName = data.filterKey.dataSet.databaseName;
                tableName = data.filterKey.dataSet.tableName;
                neon.dropdown.populateAttributeDropdowns(data.columns, options, queryForMapData);

                neon.dropdown.setDropdownInitialValue("latitude", data.selectedLatitude);
                neon.dropdown.setDropdownInitialValue("longitude", data.selectedLongitude);
                neon.dropdown.setDropdownInitialValue("color-by", data.selectedColorBy);
                neon.dropdown.setDropdownInitialValue("size-by", data.selectedSizeBy);
                //TODO: fix size-by so that it still sizes by count if nothing is selected

                _.each(options, function(selector) {
                    $('#' + selector).change();
                });

                neon.mapWidgetUtils.setLayer(data.selectedLayer);
                if(data.selectedLayer === "heatmap") {
                    map.toggleLayers();
                    $('#color-by-group').hide();
                    $('#heatmap').attr('checked', true);
                }
                map.zoomToExtent(data.selectedExtent);

                neon.query.executeQuery(data.query, redrawMapData);
            });
        }

    });
});
