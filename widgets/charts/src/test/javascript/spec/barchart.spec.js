


describe('bar chart', function () {

    /** some example key/value data. it will be reset before each test, so tests can modify it as needed */
    var categoryData;
    var categoryDataSums;

    beforeEach(function () {
        categoryData = [
            {"category": "category0", "count": 2},
            {"category": "category1", "count": 4},
            {"category": "category0", "count": 7},
            {"category": "category2", "count": 1},
            {"category": "category1", "count": 9},
            {"category": "category3", "count": 8}
        ];
        categoryDataSums = expectedData({"category0": 9, "category1": 13, "category2": 1, "category3": 8});
    });


    it('should aggregate the data values based when the chart categories are strings', function () {
        var opts = { "data": categoryData, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        expect(chart.data_).toBeEqualArray(categoryDataSums);
    });


    it('should ignores data missing the x or y attributes', function () {
        // remove some of the attributes
        delete categoryData[0].count;
        delete categoryData[1].category;
        var opts = { "data": categoryData, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = [];

        // data appears in this array in the order it was read in, but when on the chart,
        // the categories are sorted
        var expected = expectedData({"category0": 7, "category2": 1, "category1": 9, "category3": 8});
        expect(chart.data_).toBeEqualArray(expected);
    });


    it('should sort the categories when they are dervied from the data', function () {
        // by deleting categoryData[1].category, it causes the categories to be in non ascending order, but
        // the chart still sorts them
        delete categoryData[1].category;
        var opts = { "data": categoryData, "x": "category", "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        var expected = ["category0", "category1", "category2", "category3"];
        expect(chart.categories).toBeEqualArray(expected);
    });


    it('should be able to take a function to get the category name', function () {
        var opts = { "data": categoryData, "x": function (item) {
            return item.category;
        }, "y": "count"};
        var chart = new charts.BarChart('#chart', opts);
        expect(chart.data_).toBeEqualArray(categoryDataSums);
    });

    it('should allow categories to be explicitly specified', function () {
        // add an additional 2 categories with no corresponding data
        var categories = ["category0", "category1", "category2", "category3", "category4", "category5"];
        var opts = { "data": categoryData, "x": function (item) {
            return item.category;
        }, "y": "count", "categories": categories};
        var chart = new charts.BarChart('#chart', opts);
        // the data still should not have the categories, but the x-axis will show them
        expect(chart.data_).toBeEqualArray(categoryDataSums);
        expect(chart.categories).toBeEqualArray(categories);
        expect(chart.x.domain()).toBeEqualArray(categories);
    });

    it('should allow a function for categories', function () {
        var categories = function (data) {
            // leave off category3 intentonally to make sure not all values are being used if not specified in here
            return ["category0", "category1", "category2"];
        };
        var opts = { "data": categoryData, "x": function (item) {
            return item.category;
        }, "y": "count", "categories": categories};
        var chart = new charts.BarChart('#chart', opts);
        var expected = expectedData({"category0": 9, "category1": 13, "category2": 1});
        expect(chart.data_).toBeEqualArray(expected);
    });


    it('should use the count if there is no y-attribute specified', function () {
        // add another category0 value just to give another data point
        categoryData.push({"category": "category0", "count": 8});

        var opts = { "data": categoryData, "x": "category" };
        var chart = new charts.BarChart('#chart', opts);

        var expected = expectedData({"category0": 3, "category1": 2, "category2": 1, "category3": 1});
        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should not have tick values defined when there is not data', function () {
        var opts = {  "data": [], "x": "category", "y": "count" };
        var chart = new charts.BarChart('#chart', opts);
        expect(chart.tickValues_).toBeUndefined();
    });

    it('should compute the x scale bounds', function () {
        // add categories 4 and 5 to make the scale bounds computations simpler
        categoryData.push({"category": "category5", "count": 1});
        categoryData.push({"category": "category4", "count": 9});

        var opts = {  "data": categoryData, "x": "category", "y": "count", "width": 600, "margin": {"left": 0, "right": 0}};
        var chart = new charts.BarChart('#chart', opts);

        // the x scale values map to the pixels in the width of the chart
        expect(chart.x(chart.categories[0])).toEqual(0);
        expect(chart.x(chart.categories[1])).toEqual(100);
        expect(chart.x(chart.categories[2])).toEqual(200);
        expect(chart.x(chart.categories[3])).toEqual(300);
        expect(chart.x(chart.categories[4])).toEqual(400);
        expect(chart.x(chart.categories[5])).toEqual(500);
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
        expect(chart.y(0)).toEqual(100);
        expect(chart.y(5)).toEqual(50);
        expect(chart.y(10)).toEqual(0);
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
        var expected = expectedData({"0": 9, "1": 13, "2": 1, "2.5": 10, "3": 8}).map(function (el) {
            return {"key": +el.key, "values": el.values};
        });
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
        var expected = expectedData({ "true": 12, "false": 19}).map(function (el) {
            return {"key": el.key === "true", "values": el.values};
        });
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
        // the javascript engine used by jasmine doesn't seem to be able to parse '2013-2-15' so use a date string it can read
        var expected = expectedData({"Mar 15, 2013": 9, "Mar 16, 2013": 13, "Mar 17, 2013": 1, "Mar 18, 2013": 8}).map(function (el) {
            return {"key": new Date(el.key), "values": el.values};
        });
        expect(chart.data_).toBeEqualArray(expected);
    });

    it('should calculate a plot width of 0 when the chart is not wide enough', function(){
        var HORIZONTAL_MARGINS = 70;
        var CHART_WIDTH = 50;
        var CATEGORIES = 50;

        var data = [];
        for(var i = 0 ; i < CATEGORIES; i++){
            data[i] = { category: i, count: 5};
        }
        //The chart width (50) + horizontal margins(70) fits the number of categories (50)
        var opts = { "data": data, "x": "category", "y": "count", "width": HORIZONTAL_MARGINS + CHART_WIDTH};
        var chart = new charts.BarChart('#chart', opts);

        expect(chart.categories.length).toEqual(CATEGORIES);
        expect(chart.x.rangeBand()).toEqual(1);
        expect(chart.plotWidth).toEqual(CATEGORIES);

        //The chart width(49) + horizontal margins(70) does not fit the number of categories(50)
        opts = { "data": data, "x": "category", "y": "count", "width": HORIZONTAL_MARGINS + CHART_WIDTH - 1};
        chart = new charts.BarChart('#chart', opts);

        expect(chart.categories.length).toEqual(CATEGORIES);
        expect(chart.x.rangeBand()).toEqual(0);
        expect(chart.plotWidth).toEqual(0);
    });

    /**
     * Takes an object whose key value pairs represent the expected output and transforms it into an array of
     * data in the format returned by the chart
     * @param {Object} keyValuePairs
     * @return {Array}
     * @method
     */
    function expectedData(keyValuePairs) {
        var expected = [];
        for (var key in keyValuePairs) {
            expected.push({"key": key, "values": keyValuePairs[key]});
        }
        return expected;
    }

    /**
     * Creates a set of data where the odd values are the "category" values and the even values are
     * the "values"
     * @param alternatingKeyValues
     */
    function createCategoryData(alternatingKeyValues) {
        var data = [];
        for (var i = 0; i < alternatingKeyValues.length; i += 2) {
            data.push({"category": alternatingKeyValues[i], "count": alternatingKeyValues[i + 1]});
        }
    }

});
