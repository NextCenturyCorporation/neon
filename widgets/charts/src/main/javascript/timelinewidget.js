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
        var timeline;
        var COUNT_FIELD_NAME = 'Count';
        var clientId = OWF.getInstanceId();

        initialize();

        function initialize(){
            OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
            neon.query.SERVER_URL = $("#neon-server").val();
            neon.eventing.messageHandler.subscribeToNeonEvents({
                activeDatasetChanged: function (message) {
                    neon.chartWidget.onActiveDatasetChanged(message, drawChart, neon.widget.TIMELINE);
                },
                filtersChanged: onFiltersChanged
            });


            neon.toggle.createOptionsPanel("#options-panel");
            populateTimeGranularityDropdown();
            configureButtons();
            drawChart();
            restoreState();
        }

        function configureButtons() {
            configureRedrawBoundsButton();
            configureResetFilterButton();
        }

        function getRedrawBoundsButton() {
            return $('#redraw-bounds');
        }

        function getResetFilterButton() {
            return $('#reset-filter');
        }

        function disableResetFilterButton() {
            getResetFilterButton().attr('disabled', 'disabled');
        }

        function configureRedrawBoundsButton() {
            getRedrawBoundsButton().click(function () {
                drawChart();
            });
        }

        function configureResetFilterButton() {
            // initially disabled until filter added
            disableResetFilterButton();
            getResetFilterButton().click(function () {
                neon.eventing.publishing.removeFilter(neon.chartWidget.getFilterKey(), drawChart);
            });
        }

        function onFiltersChanged(message, sender) {
            if (sender === OWF.getIframeId()) {
                getResetFilterButton().removeAttr('disabled');
            }
            else{
                drawChart();
                disableResetFilterButton();
            }
        }

        /**
         * Redraws the chart based on the user selected attribtues
         * @method drawChart
         */
        function drawChart() {

            var xAttr = neon.chartWidget.getXAttribute();
            var yAttr = neon.chartWidget.getYAttribute();

            if (!xAttr) {
                doDrawChart({data: []});
                return;
            }

            var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, xAttr, 'hour');
            var groupByDayClause = new neon.query.GroupByFunctionClause(neon.query.DAY, xAttr, 'day');
            var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, xAttr, 'month');
            var groupByYearClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, xAttr, 'year');

            var query = new neon.query.Query()
                .selectFrom(neon.chartWidget.getDatabaseName(), neon.chartWidget.getTableName())
                .where(xAttr, '!=', null)
                .groupBy(groupByYearClause, groupByMonthClause, groupByDayClause, groupByHourClause);

            if (yAttr) {
                query.aggregate(neon.query.SUM, yAttr, yAttr);
            }
            else {
                query.aggregate(neon.query.COUNT, null, COUNT_FIELD_NAME);
            }
            var stateObject = buildStateObject(query);
            neon.query.executeQuery(query, doDrawChart);
            neon.query.saveState(clientId, stateObject);
        }

        function doDrawChart(data) {

            $('#chart').empty();

            var xAttr = neon.chartWidget.getXAttribute();
            var yAttr = neon.chartWidget.getYAttribute();

            if (!yAttr) {
                yAttr = COUNT_FIELD_NAME;
            }

            var dataByDate = data.data.map(function (el) {
                //month is 1-based
                var date = new Date(Date.UTC(el.year, el.month - 1, el.day, el.hour));
                var count = el[yAttr];
                var result = {};
                result[xAttr] = date;
                result[yAttr] = count;
                return result;
            });

            var granularity = $('#time-granularity option:selected').val();
            var opts = { "data": dataByDate, "x": xAttr, "y": yAttr,
                "interval": granularity, responsive: true};

            // TODO: We need this because we set a window listener which holds a reference to old timeline objects.
            // We should really only use one timeline object, but that will be fixed as part of NEON-294
            $(window).off("resize");
            timeline = new charts.Timeline('#chart', opts);
            configureFiltering(timeline, xAttr);
            timeline.draw();
        }

        function configureFiltering(timeline, xAttr) {
            timeline.onFilter(function (startDate, endDate) {
                var startFilterClause = neon.query.where(xAttr, '>=', startDate);
                var endFilterClause = neon.query.where(xAttr, '<', endDate);
                var filterClause = neon.query.and(startFilterClause, endFilterClause);
                var filter = new neon.query.Filter().selectFrom(neon.chartWidget.getDatabaseName(), neon.chartWidget.getTableName()).where(filterClause);

                neon.eventing.publishing.replaceFilter(neon.chartWidget.getFilterKey(), filter);
            });
        }

        function populateTimeGranularityDropdown() {

            neon.dropdown.populateAttributeDropdowns({fieldNames: charts.Timeline.GRANULARITIES_}, "time-granularity", drawChart);
        }

        function buildStateObject(query) {
            return {
                filterKey: neon.chartWidget.getFilterKey(),
                columns: neon.dropdown.getFieldNamesFromDropdown("x"),
                xValue: neon.chartWidget.getXAttribute(),
                yValue: neon.chartWidget.getYAttribute(),
                timeGranularity: $('#time-granularity').val(),
                query: query
            };
        }

        function restoreState() {
            neon.query.getSavedState(clientId, function (data) {
                neon.chartWidget.setFilterKey(data.filterKey);
                neon.chartWidget.setDatabaseName(data.filterKey.dataSet.databaseName);
                neon.chartWidget.setTableName(data.filterKey.dataSet.tableName);
                neon.dropdown.populateAttributeDropdowns(data.columns, ['x','y'], drawChart);
                neon.dropdown.setDropdownInitialValue("x", data.xValue);
                neon.dropdown.setDropdownInitialValue("y", data.yValue);
                neon.dropdown.setDropdownInitialValue("time-granularity", data.timeGranularity);
                neon.query.executeQuery(data.query, doDrawChart);
                getResetFilterButton().removeAttr('disabled');
            });
        }
    });


});