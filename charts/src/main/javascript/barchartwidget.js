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

    OWF.ready(function () {

        var COUNT_FIELD_NAME = 'Count';

        // just creating the message handler will receive messages
        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: function (message) {
                //defined in chartwidget.js
                onActiveDatasetChanged(message, drawChart);
            },
            filtersChanged: drawChart
        });


        /**
         * Redraws the chart based on the user selected attribtues
         * @method drawChart
         */
        function drawChart() {

            var xAttr = getXAttribute();
            var yAttr = getYAttribute();

            if (!xAttr) {
                doDrawChart({data: []});
                return;
            }

            var query = new neon.query.Query()
                .selectFrom(databaseName, tableName)
                .where(xAttr, '!=', null).groupBy(xAttr);

            if (yAttr) {
                query.aggregate(neon.query.SUM, yAttr, yAttr);
            }
            else {
                query.aggregate(neon.query.COUNT, null, COUNT_FIELD_NAME);
            }
            neon.query.executeQuery(query, doDrawChart);
        }

        function doDrawChart(data) {
            $('#chart').empty();
            var xAttr = getXAttribute();
            var yAttr = getYAttribute();

            if (!yAttr) {
                yAttr = COUNT_FIELD_NAME;
            }

            var opts = { "data": data.data, "x": xAttr, "y": yAttr, responsive: true};
            var chart = new charts.BarChart('#chart', opts);
            chart.draw();
        }

        drawChart();

    });


});