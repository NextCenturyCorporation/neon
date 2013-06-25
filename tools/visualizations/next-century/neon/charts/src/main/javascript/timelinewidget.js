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


$(document).ready(function () {

    OWF.ready(function (o) {

        var datasource;
        var datasetId;
        var filterId;


        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: populateAttributeDropdowns,
            filtersChanged: updateFilterId

        });
        var eventPublisher = new neon.eventing.OWFEventPublisher(messageHandler);

        function updateFilterId(message) {
            // keep track of own filter so we can just replace it rather than keep adding new ones
            if (message._source === messageHandler.id) {
                filterId = message.addedIds[0];
            }
        }

        function populateAttributeDropdowns(message) {
            datasource = message.database;
            datasetId = message.table;
            neon.query.getFieldNames(datasource, datasetId, doPopuplateAttributeDropdowns);
        };

        function doPopuplateAttributeDropdowns(data) {
            ['x', 'y'].forEach(function (selectId) {
                var select = $('#' + selectId);
                select.empty();
                select.append($('<option></option>').attr('value', '').text('(Select Field)'));
                data.fieldNames.forEach(function (field) {
                    select.append($('<option></option>').attr('value', field).text(field));
                });
                select.change(drawChart);
            });
        };

        function getXAttribute() {
            return $('#x option:selected').val();
        }

        function getYAttribute() {
            return $('#y option:selected').val();
        }

        /**
         * Redraws the chart based on the user selected attribtues
         */
        function drawChart() {

            $('#chart').empty();

            var xAttr = getXAttribute();
            var yAttr = getYAttribute();

            if (!xAttr || !yAttr) {
                doDrawChart(null);
                return;
            }

            var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, xAttr, 'hour');
            var groupByDayClause = new neon.query.GroupByFunctionClause(neon.query.DAY, xAttr, 'day');
            var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, xAttr, 'month');
            var groupByYearClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, xAttr, 'year');

            var query = new neon.query.Query()
                .selectFrom(datasource, datasetId)
                .groupBy(groupByYearClause, groupByMonthClause, groupByDayClause, groupByHourClause)
                .aggregate(neon.query.SUM, yAttr, yAttr);

            neon.query.executeQuery(query, doDrawChart);
        };

        function doDrawChart(data) {

            var xAttr = getXAttribute();
            var yAttr = getYAttribute();

            var dataByDate = data ? data.data.map(function (el) {
                //month is 1-based
                var date = new Date(el.year, el.month - 1, el.day, el.hour);
                var count = el[yAttr];
                var result = {};
                result[xAttr] = date;
                result[yAttr] = count;
                return result;
            }) : [];

            var granularity = $('#time-granularity option:selected').val();
            var opts = { "data": dataByDate, "x": xAttr, "y": yAttr,
                "interval": granularity, width: 600, height: 400};


            var timeline = new charts.Timeline('#chart', opts);
            configureFiltering(timeline, xAttr);
            timeline.draw();
        };

        function configureFiltering(timeline, xAttr) {
            timeline.onFilter(function (startDate, endDate) {
                var startFilterClause = neon.query.where(xAttr, '>=', startDate);
                var endFilterClause = neon.query.where(xAttr, '<', endDate);
                var filterClause = neon.query.and(startFilterClause, endFilterClause);
                var filter = new neon.query.Filter().selectFrom(datasource, datasetId).where(filterClause);


                if (filterId) {
                    eventPublisher.replaceFilter(filterId, filter)
                }
                else {
                    eventPublisher.addFilter(filter);
                }
            });
        };

        function populateTimeGranularityDropdown() {
            var dropdown = $('#time-granularity');
            [charts.Timeline.HOUR, charts.Timeline.DAY, charts.Timeline.MONTH, charts.Timeline.YEAR].forEach(function (el) {
                var displayText = el.charAt(0).toUpperCase() + el.slice(1);
                var option = $('<option></option>').attr('value', el).text(displayText);
                if (el === charts.Timeline.MONTH) {
                    option.attr('selected', true);
                }
                dropdown.append(option);
            });
            dropdown.change(drawChart);
        };

        populateTimeGranularityDropdown();
        drawChart();

    });


});