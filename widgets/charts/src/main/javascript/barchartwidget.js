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


neon.ready(function () {

    var COUNT_FIELD_NAME = 'Count';
    var clientId = neon.eventing.messaging.getInstanceId();
    initialize();

    function initialize() {
        neon.query.SERVER_URL = $("#neon-server").val();

        neon.eventing.messaging.registerForNeonEvents({
            activeDatasetChanged: function (message) {
                neon.chartWidget.onActiveDatasetChanged(message, drawChart, neon.widget.BARCHART);
            },
            filtersChanged: drawChart
        });

        neon.toggle.createOptionsPanel("#options-panel");
        drawChart();
        restoreState();
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

        var query = new neon.query.Query()
            .selectFrom(neon.chartWidget.getDatabaseName(), neon.chartWidget.getTableName())
            .where(xAttr, '!=', null).groupBy(xAttr);

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

        //We need this because we set a window listener which holds a reference to old barchart objects.
        //We should really only use one barchart object, but that will be fixed as part of NEON-294
        $(window).off("resize");
        var opts = { "data": data.data, "x": xAttr, "y": yAttr, responsive: true};
        var chart = new charts.BarChart('#chart', opts).draw();
    }

    function buildStateObject(query) {
        return {
            filterKey: neon.chartWidget.getFilterKey(),
            columns: neon.dropdown.getFieldNamesFromDropdown("x"),
            xValue: neon.chartWidget.getXAttribute(),
            yValue: neon.chartWidget.getYAttribute(),
            query: query
        };
    }

    function restoreState() {
        neon.query.getSavedState(clientId, function (data) {
            neon.chartWidget.setFilterKey(data.filterKey);
            neon.chartWidget.setDatabaseName(data.filterKey.dataSet.databaseName);
            neon.chartWidget.setTableName(data.filterKey.dataSet.tableName);

            var elements = [new neon.dropdown.Element("x", ["text", "numeric"]), new neon.dropdown.Element("y", "numeric")];
            neon.dropdown.populateAttributeDropdowns(data.columns, elements, drawChart);
            neon.dropdown.setDropdownInitialValue("x", data.xValue);
            neon.dropdown.setDropdownInitialValue("y", data.yValue);
            neon.query.executeQuery(data.query, doDrawChart);
        });
    }

});
