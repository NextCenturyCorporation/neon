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


describe('bar chart', function () {


    it('should aggregate the data values based when the chart categories are strings', function () {
        var data = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": "category0", "values": 9});
        expected.push({ "key": "category1", "values": 13});
        expected.push({ "key": "category2", "values": 1});
        expected.push({ "key": "category3", "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should aggregate the data values based when the chart categories are numbers', function () {
        var data = [
            {"category": 0, "count": 2},
            {"category": 1, "count": 4},
            {"category": 0, "count": 7},
            {"category": 2, "count": 1},
            {"category": 2.5, "count": 6},
            {"category": 2.5, "count": 4},
            {"category": 1, "count": 9},
            {"category": 3, "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": 0, "values": 9});
        expected.push({ "key": 1, "values": 13});
        expected.push({ "key": 2, "values": 1});
        expected.push({ "key": 2.5, "values": 10});
        expected.push({ "key": 3, "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should aggregate the data values based when the chart categories are booleans', function () {
        var data = [
            {"category": true, "count": 2},
            {"category": false, "count": 4},
            {"category": false, "count": 7},
            {"category": true, "count": 1},
            {"category": true, "count": 9},
            {"category": false, "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": true, "values": 12});
        expected.push({ "key": false, "values": 19});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should aggregate the data values based when the chart categories are dates', function () {
        var data = [
            {"category": new Date(2013, 2, 15), "count": 2},
            {"category": new Date(2013, 2, 16), "count": 4},
            {"category": new Date(2013, 2, 15), "count": 7},
            {"category": new Date(2013, 2, 17), "count": 1},
            {"category": new Date(2013, 2, 16), "count": 9},
            {"category": new Date(2013, 2, 18), "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": new Date(2013, 2, 15), "values": 9});
        expected.push({ "key": new Date(2013, 2, 16), "values": 13});
        expected.push({ "key": new Date(2013, 2, 17), "values": 1});
        expected.push({ "key": new Date(2013, 2, 18), "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should ignores data missing the x or y attributes', function () {
        var data = [
            {"category": "category0"},
            {"count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        // data appears in this array in the order it was read in, but when on the chart,
        // the categories are sorted
        expected.push({ "key": "category0", "values": 7});
        expected.push({ "key": "category2", "values": 1});
        expected.push({ "key": "category1", "values": 9});
        expected.push({ "key": "category3", "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
    });


    it('should sort the categories when they are dervied from the data', function () {
        var data = [
            {"category": "category0"},
            {"count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        var opts = { "data": data, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = ["category0", "category1", "category2", "category3"];
        expect(chart.categories_).toBeEqualArray(expected);
    });


    it('should be able to take a function to get the category name', function () {
        var data = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        var opts = { "data": data, "x": function (item) {
            return item.category;
        }, "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": "category0", "values": 9});
        expected.push({ "key": "category1", "values": 13});
        expected.push({ "key": "category2", "values": 1});
        expected.push({ "key": "category3", "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should allow categories to be explicitly specified', function () {
        var data = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];

        // add an additional 2 categories with no corresponding data
        var categories = ["category0", "category1", "category2", "category3", "category4", "category5"];
        var opts = { "data": data, "x": function (item) {
            return item.category;
        }, "y": "count", "categories": categories};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        // the data still should not have the categories, but the x-axis will show them
        expected.push({ "key": "category0", "values": 9});
        expected.push({ "key": "category1", "values": 13});
        expected.push({ "key": "category2", "values": 1});
        expected.push({ "key": "category3", "values": 8});

        expect(chart.data_).toBeEqualArray(expected);
        expect(chart.categories_).toBeEqualArray(categories);
        expect(chart.x_.domain()).toBeEqualArray(categories);
    });

    it('should allow a function for categories', function () {
        var data = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];

        var categories = function (data) {
            // leave off category3 intentonally to make sure not all values are being used if not specified in here
            return ["category0", "category1", "category2"];
        };
        var opts = { "data": data, "x": function (item) {
            return item.category;
        }, "y": "count", "categories": categories};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        expected.push({ "key": "category0", "values": 9});
        expected.push({ "key": "category1", "values": 13});
        expected.push({ "key": "category2", "values": 1});

        expect(chart.data_).toBeEqualArray(expected);
    });


    it('should use the count if there is no y-attribute specified', function () {
        var data = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category0", "count": 3},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        var opts = { "data": data, "x": "category" };
        var chart = new charts.BarChart('#chart', opts);

        var expected = [];

        expected.push({ "key": "category0", "values": 3});
        expected.push({ "key": "category1", "values": 2});
        expected.push({ "key": "category2", "values": 1});
        expected.push({ "key": "category3", "values": 1});

        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should not have tick values defined when there is not data', function () {
        var opts = {  "data": [], "x": "category", "y": "count" };
        var chart = new charts.BarChart('#chart', opts);
        expect(chart.tickValues_).toBeUndefined();
    });

    it('should compute the x scale bounds', function () {
        var data = [
            {"category": "category1", "count": 2},
            {"category": "category3", "count": 4},
            {"category": "category2", "count": 7},
            {"category": "category5", "count": 1},
            {"category": "category4", "count": 9},
            {"category": "category0", "count": 8},
            {"category": "category4", "count": 3}
        ];
        var opts = {  "data": data, "x": "category", "y": "count", "width": 600, "margin": {"left": 0, "right": 0}};
        var chart = new charts.BarChart('#chart', opts);

        // the x scale values map to the pixels in the width of the chart
        expect(chart.x_(chart.categories_[0])).toEqual(0);
        expect(chart.x_(chart.categories_[1])).toEqual(100);
        expect(chart.x_(chart.categories_[2])).toEqual(200);
        expect(chart.x_(chart.categories_[3])).toEqual(300);
        expect(chart.x_(chart.categories_[4])).toEqual(400);
        expect(chart.x_(chart.categories_[5])).toEqual(500);
    });

    it('should compute the y scale bounds', function () {
        var data = [
            {"category": "category2", "count": 1},
            {"category": "category0", "count": 5},
            {"category": "category1", "count": 10}
        ];
        var opts = { "data": data, "x": "category", "y": "count", "height": 100, "margin": {"top": 0, "bottom": 0}};
        var chart = new charts.BarChart('#chart', opts);

        // the y scale values map to the pixels in the height of the chart
        // note: the y scale is inverted (but is drawn properly)
        expect(chart.y_(0)).toEqual(100);
        expect(chart.y_(5)).toEqual(50);
        expect(chart.y_(10)).toEqual(0);
    });

    it('should allow margins to be overridden', function () {
        var marginOverrides = { "top": 100, "left": 120};
        var opts = {  "data": [], "x": "date", "y": "count", "margin": marginOverrides};
        var chart = new charts.BarChart('#chart', opts);
        var margin = chart.margin;
        expect(margin.top).toEqual(marginOverrides.top);
        expect(margin.left).toEqual(marginOverrides.left);
        expect(margin.bottom).toEqual(charts.BarChart.DEFAULT_MARGIN_.bottom);
        expect(margin.right).toEqual(charts.BarChart.DEFAULT_MARGIN_.right);

        // make sure we didn't set the override value equal to the default value
        expect(margin.top).not.toEqual(charts.BarChart.DEFAULT_MARGIN_.top);
        expect(margin.left).not.toEqual(charts.BarChart.DEFAULT_MARGIN_.left);
    });

});
