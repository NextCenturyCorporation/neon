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


describe('timeline', function () {


    it('should aggregate the data values based on the chart time period', function () {
        var data = [
            {"date": new Date(2013, 0, 7), "count": 2},
            {"date": new Date(2013, 1, 1), "count": 4},
            {"date": new Date(2013, 0, 8), "count": 7},
            {"date": new Date(2013, 2, 1), "count": 1},
            {"date": new Date(2013, 1, 1), "count": 9},
            {"date": new Date(2013, 4, 13), "count": 8}
        ];
        var opts = { "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        var expected = [];

        expected.push({ "key": new Date(2013, 0), "values": 9});
        expected.push({ "key": new Date(2013, 1), "values": 13});
        expected.push({ "key": new Date(2013, 2), "values": 1});
        expected.push({ "key": new Date(2013, 4), "values": 8});

        expect(timeline.data_).toBeEqualArray(expected);
    });

    it('should compute the time periods', function () {
        var data = [
            {"date": new Date(2013, 1, 7), "count": 2},
            {"date": new Date(2013, 3, 1), "count": 4},
            {"date": new Date(2013, 0, 8), "count": 7},
            {"date": new Date(2013, 2, 1), "count": 1},
            {"date": new Date(2013, 4, 1), "count": 9},
            {"date": new Date(2013, 5, 13), "count": 8}
        ];
        var opts = {  "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        var timePeriods = timeline.timePeriods_;

        // even though the data starts at 1/8, the time period begins at the beginning of the month (the user
        // only ever sees the date indicating the beginning of the data - internally it uses the beginning of the
        // time period date)
        var expected = [
            new Date(2013, 0, 1),
            new Date(2013, 1, 1),
            new Date(2013, 2, 1),
            new Date(2013, 3, 1),
            new Date(2013, 4, 1),
            new Date(2013, 5, 1)
        ];
        expect(timePeriods).toBeEqualArray(expected);
    });

    it('should not have any tick values when there is not data', function () {
        var opts = {  "data" : [], "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        var tickValues = timeline.computeTickValues_();
        expect(tickValues).toBeEqualArray([]);
    });

    it('should compute tick intervals at regular intervals when charting by day', function () {
        var data = [
            {"date": new Date(2013, 2, 07), "count": 2},
            {"date": new Date(2013, 3, 15), "count": 4}
        ];
        var opts = {  "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.DAY};
        var timeline = new charts.Timeline('#chart', opts);
        var expected = [
            new Date(2013, 2, 7),
            new Date(2013, 2, 14),
            new Date(2013, 2, 21),
            new Date(2013, 2, 28),
            new Date(2013, 3, 4),
            new Date(2013, 3, 11)
        ];

        var tickValues = timeline.computeTickValues_();
        expect(tickValues).toBeEqualArray(expected);

    });

    it('should compute the x scale bounds', function () {
        var data = [
            {"date": new Date(2013, 1, 7), "count": 2},
            {"date": new Date(2013, 3, 1), "count": 4},
            {"date": new Date(2013, 0, 8), "count": 7},
            {"date": new Date(2013, 2, 1), "count": 1},
            {"date": new Date(2013, 4, 1), "count": 9},
            {"date": new Date(2013, 5, 13), "count": 8}
        ];
        var opts = {  "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH, "width": 600, "margin": {"left": 0, "right": 0}};
        var timeline = new charts.Timeline('#chart', opts);
        expect(timeline.x_(timeline.timePeriods_[0])).toEqual(0);
        expect(timeline.x_(timeline.timePeriods_[1])).toEqual(100);
        expect(timeline.x_(timeline.timePeriods_[2])).toEqual(200);
        expect(timeline.x_(timeline.timePeriods_[3])).toEqual(300);
        expect(timeline.x_(timeline.timePeriods_[4])).toEqual(400);
        expect(timeline.x_(timeline.timePeriods_[5])).toEqual(500);
    });

    it('should compute the y scale bounds', function () {
        var data = [
            {"date": new Date(2013, 1, 7), "count": 1},
            {"date": new Date(2013, 3, 1), "count": 5},
            {"date": new Date(2013, 0, 8), "count": 10}
        ];
        var opts = { "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH, "height": 100, "margin": {"top": 0, "bottom": 0}};
        var timeline = new charts.Timeline('#chart', opts);
        // the y scale is inverted (but is drawn properly)
        expect(timeline.y_(0)).toEqual(100);
        expect(timeline.y_(5)).toEqual(50);
        expect(timeline.y_(10)).toEqual(0);
    });

    it('should compute the minimum date', function () {
        var data = [
            {"date": new Date(2013, 1, 7), "count": 2},
            {"date": new Date(2013, 3, 1), "count": 4},
            {"date": new Date(2013, 0, 8), "count": 7}
        ];
        var opts = {  "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        expect(timeline.computeMinDate_(data)).toEqual(new Date(2013, 0, 8));
    });

    it('should compute the maximum date', function () {
        var data = [
            {"date": new Date(2013, 1, 7), "count": 2},
            {"date": new Date(2013, 3, 1), "count": 4},
            {"date": new Date(2013, 0, 8), "count": 7}
        ];
        var opts = {  "data" : data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        // the max date is exclusive so it is 1ms higher than the end of the chart
        expect(timeline.computeMaxDate_(data)).toEqual(new Date(new Date(2013, 3, 1).getTime() + 1));
    });


    it('should allow margins to be overridden', function () {
        var marginOverrides = { "top": 100, "left": 120};
        var opts = {  "data" : [], "x": "date", "y": "count", "interval": charts.Timeline.DAY, "margin": marginOverrides};
        var timeline = new charts.Timeline('#chart', opts);
        var margin = timeline.margin;
        expect(margin.top).toEqual(marginOverrides.top);
        expect(margin.left).toEqual(marginOverrides.left);
        expect(margin.bottom).toEqual(charts.Timeline.DEFAULT_MARGIN_.bottom);
        expect(margin.right).toEqual(charts.Timeline.DEFAULT_MARGIN_.right);

        // make sure we didn't set the override value equal to the default value
        expect(margin.top).not.toEqual(charts.Timeline.DEFAULT_MARGIN_.top);
        expect(margin.left).not.toEqual(charts.Timeline.DEFAULT_MARGIN_.left);
    });

    it('should notify listeners of filter events', function () {

        var data = [
            {"date": new Date(2013, 0, 1), "events": 2},
            {"date": new Date(2013, 1, 1), "events": 4},
            {"date": new Date(2013, 2, 1), "events": 7},
            {"date": new Date(2013, 3, 1), "events": 1}
        ];
        var opts = {  "data" : data, "x": "date", "y": "events", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        timeline.onFilter(callback1);
        timeline.onFilter(callback2);

        var startDate = new Date(2013, 0, 1);
        var endDate = new Date(2013, 2, 1);
        timeline.notifyFilterListeners_(startDate, endDate);

        expect(callback1).toHaveBeenCalledWith(startDate, endDate);
        expect(callback2).toHaveBeenCalledWith(startDate, endDate);

        // remove the listeners and make sure they are not notified again
        timeline.removeFilterListeners();
        timeline.notifyFilterListeners_(startDate, endDate);

        expect(callback1.callCount).toEqual(1);
        expect(callback2.callCount).toEqual(1);

    });
});
