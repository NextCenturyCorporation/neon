$(document).ready(function () {

    OWF.ready(function () {
        var databaseName;
        var tableName;
        var filterKey;

        var dateField;
        var chart;

        var HOURS_IN_WEEK = 168;
        var HOURS_IN_DAY = 24;

        // instantiating the message handler adds it as a listener
        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: onDatasetChanged,
            filtersChanged: onFiltersChanged
        });

        function onFiltersChanged(message) {
            redrawChart();
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
            ['date'].forEach(function (selectId) {
                var select = $('#' + selectId);
                select.empty();
                select.append($('<option></option>').attr('value', '').text('(Select Field)'));
                data.fieldNames.forEach(function (field) {
                    select.append($('<option></option>').attr('value', field).text(field));
                });
                select.change(redrawChart);
            });
        }

        function initChart() {
            chart = circularHeatChart()
                .segmentHeight(20)
                .innerRadius(20)
                .numSegments(24)
                .radialLabels(["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"])
                .segmentLabels(["12am", "1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm", "9pm", "10pm", "11pm"])
                .margin({top: 20, right: 20, bottom: 20, left: 20});
        }

        function redrawChart() {
            dateField = getDateField();

            if (!dateField) {
                doRedrawChart({data: []});
                return;
            }

            // TODO: Add support for dayOfWeek to query API
            var groupByDayClause = new neon.query.GroupByFunctionClause('dayOfWeek', dateField, 'day');
            var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'hour');

            var query = new neon.query.Query()
                .selectFrom(databaseName, tableName)
                .groupBy(groupByDayClause, groupByHourClause)
                .aggregate(neon.query.COUNT, null, 'count');

            neon.query.executeQuery(query, doRedrawChart);
        }

        function doRedrawChart(queryResults) {
            rawData = queryResults.data;

            data = [];

            for (ii = 0; ii < HOURS_IN_WEEK; ++ii) {
                data[ii] = 0;
            }

            rawData.forEach(function(d) {
                data[(d.day-1)*HOURS_IN_DAY + d.hour] = d.count;
            });

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

        function getDateField() {
            return $('#date option:selected').val();
        }

        initChart();

    });

});


