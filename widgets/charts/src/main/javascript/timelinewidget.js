/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */



neon.ready(function () {
    var timeline;
    var COUNT_FIELD_NAME = 'Count';
    var clientId = neon.eventing.messaging.getInstanceId();

    initialize();

    function initialize() {
        neon.query.SERVER_URL = $("#neon-server").val();
        neon.toggle.createOptionsPanel("#options-panel");
        drawChart();
        populateTimeGranularityDropdown();

        neon.eventing.messaging.registerForNeonEvents({
            activeDatasetChanged: function (message) {
                neon.chartWidget.onActiveDatasetChanged(message, drawChart, neon.widget.TIMELINE);
            },
            activeConnectionChanged: neon.chartWidget.onConnectionChanged,
            filtersChanged: onFiltersChanged
        });


        configureButtons();
        restoreState();
    }

    function configureButtons() {
        $('#redraw-bounds').click(function () {
            drawChart();
        });
        $('#reset-filter').click(function () {
            neon.eventing.publishing.removeFilter(neon.chartWidget.getFilterKey(), drawChart);
        });
    }

    function onFiltersChanged(message, sender) {
        if (sender !== neon.eventing.messaging.getIframeId()) {
            drawChart();
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
            query.aggregate(neon.query.COUNT, '*', COUNT_FIELD_NAME);
        }
        var stateObject = buildStateObject(query);
        neon.query.executeQuery(neon.chartWidget.getConnectionId(), query, doDrawChart);
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
        var element = new neon.dropdown.Element("time-granularity");
        neon.dropdown.populateAttributeDropdowns({data: charts.Timeline.GRANULARITIES_}, element, drawChart);
    }

    function buildStateObject(query) {
        return {
            connectionId: neon.chartWidget.getConnectionId(),
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
            neon.chartWidget.onConnectionChanged(data.connectionId);
            neon.chartWidget.setFilterKey(data.filterKey);
            neon.chartWidget.setDatabaseName(data.filterKey.dataSet.databaseName);
            neon.chartWidget.setTableName(data.filterKey.dataSet.tableName);
            var elements = [new neon.dropdown.Element("x", "temporal"), new neon.dropdown.Element("y", "numeric")];
            neon.dropdown.populateAttributeDropdowns(data.columns, elements, drawChart);
            neon.dropdown.setDropdownInitialValue("x", data.xValue);
            neon.dropdown.setDropdownInitialValue("y", data.yValue);
            neon.dropdown.setDropdownInitialValue("time-granularity", data.timeGranularity);
            neon.query.executeQuery(neon.chartWidget.getConnectionId(), data.query, doDrawChart);
        });
    }
});
