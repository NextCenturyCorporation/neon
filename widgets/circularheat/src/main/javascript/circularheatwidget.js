neon.ready(function () {
    var databaseName;
    var tableName;
    var filterKey;
    var chart;

    var HOURS_IN_WEEK = 168;
    var HOURS_IN_DAY = 24;

    var clientId = neon.eventing.messaging.getInstanceId();

    initialize();

    function initialize() {

        neon.query.SERVER_URL = $("#neon-server").val();

        neon.eventing.messaging.registerForNeonEvents({
            activeDatasetChanged: onDatasetChanged,
            filtersChanged: onFiltersChanged
        });

        neon.toggle.createOptionsPanel("#options-panel");
        initChart();
        restoreState();
    }

    function initChart() {
        chart = circularHeatChart()
            .segmentHeight(20)
            .innerRadius(20)
            .numSegments(24)
            .radialLabels(["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"])
            .segmentLabels(["12am", "1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm", "9pm", "10pm", "11pm"])
            .margin({top: 20, right: 20, bottom: 20, left: 20});
        queryForChartData();
    }

    function onFiltersChanged(message) {
        queryForChartData();
    }

    function onDatasetChanged(message) {
        databaseName = message.database;
        tableName = message.table;
        neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
            filterKey = filterResponse;
        });
        neon.query.getFieldNames(databaseName, tableName, neon.widget.CIRCULAR_HEAT, populateFromColumns);
    }

    function populateFromColumns(data) {
        var element = new neon.dropdown.Element("date", "temporal");
        neon.dropdown.populateAttributeDropdowns(data, element, queryForChartData);
    }

    function queryForChartData() {
        var dateField = getDateField();

        if (!dateField) {
            drawChart({data: []});
            return;
        }

        //TODO: NEON-603 Add support for dayOfWeek to query API
        var groupByDayClause = new neon.query.GroupByFunctionClause('dayOfWeek', dateField, 'day');
        var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'hour');

        var query = new neon.query.Query()
            .selectFrom(databaseName, tableName)
            .groupBy(groupByDayClause, groupByHourClause)
            .where(dateField, '!=', null)
            .aggregate(neon.query.COUNT, null, 'count');

        var stateObject = buildStateObject(dateField, query);

        neon.query.executeQuery(query, drawChart);
        neon.query.saveState(clientId, stateObject);
    }

    function drawChart(queryResults) {
        var data = createHeatChartData(queryResults);

        d3.select('#circularheatchart')
            .selectAll('svg')
            .remove();

        d3.select('#circularheatchart')
            .selectAll('svg')
            .data([data])
            .enter()
            .append('svg')
            .call(chart);
    }

    function createHeatChartData(queryResults){
        var rawData = queryResults.data;

        var data = [];

        for (var i = 0; i < HOURS_IN_WEEK; i++) {
            data[i] = 0;
        }

        _.each(rawData, function (element) {
            data[(element.day - 1) * HOURS_IN_DAY + element.hour] = element.count;
        });

        return data;
    }

    function getDateField() {
        return $('#date option:selected').val();
    }

    function buildStateObject(dateField, query) {
        return {
            filterKey: filterKey,
            columns: neon.dropdown.getFieldNamesFromDropdown("date"),
            selectedField: dateField,
            query: query
        };
    }

    function restoreState() {
        neon.query.getSavedState(clientId, function (data) {
            filterKey = data.filterKey;
            if(!filterKey){
                return;
            }
            databaseName = data.filterKey.dataSet.databaseName;
            tableName = data.filterKey.dataSet.tableName;
            var element = new neon.dropdown.Element("date", "temporal");
            neon.dropdown.populateAttributeDropdowns(data.columns, element, queryForChartData);
            neon.dropdown.setDropdownInitialValue("date", data.selectedField);
            neon.query.executeQuery(data.query, drawChart);
        });
    }

});


