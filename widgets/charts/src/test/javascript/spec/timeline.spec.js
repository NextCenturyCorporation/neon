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




describe('timeline', function () {


    it('should aggregate the data values based on the chart time period', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 0, 7, 11, 30, 5, 125)), "count": 2},
            {"date": new Date(Date.UTC(2013, 1, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7},
            {"date": new Date(Date.UTC(2013, 2, 1)), "count": 1},
            {"date": new Date(Date.UTC(2013, 1, 1)), "count": 9},
            {"date": new Date(Date.UTC(2013, 4, 13)), "count": 8}
        ];
        var opts = { "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        var expected = [];

        // the first value will use the true start date
        expected.push({ "key": new Date(Date.UTC(2013, 0, 7, 11, 30, 5, 125)), "values": 9});
        expected.push({ "key": new Date(Date.UTC(2013, 1)), "values": 13});
        expected.push({ "key": new Date(Date.UTC(2013, 2)), "values": 1});
        expected.push({ "key": new Date(Date.UTC(2013, 4)), "values": 8});

        expect(timeline.data_).toBeEqualArray(expected);
    });

    it('should use the count if there is no y-attribute specified', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 0, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 1, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 3},
            {"date": new Date(Date.UTC(2013, 2, 1)), "count": 1},
            {"date": new Date(Date.UTC(2013, 1, 1)), "count": 9},
            {"date": new Date(Date.UTC(2013, 4, 13)), "count": 8}
        ];
        var opts = { "data": data, "x": "date", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        var expected = [];

        // the first value will use the true start date
        expected.push({ "key": new Date(Date.UTC(2013, 0, 7)), "values": 3});
        expected.push({ "key": new Date(Date.UTC(2013, 1)), "values": 2});
        expected.push({ "key": new Date(Date.UTC(2013, 2)), "values": 1});
        expected.push({ "key": new Date(Date.UTC(2013, 4)), "values": 1});

        expect(timeline.data_).toBeEqualArray(expected);
    });

    it('should compute the time periods', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 1, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7},
            {"date": new Date(Date.UTC(2013, 2, 1)), "count": 1},
            {"date": new Date(Date.UTC(2013, 4, 1)), "count": 9},
            {"date": new Date(Date.UTC(2013, 5, 13)), "count": 8}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        var timePeriods = timeline.categories;

        // the first time period uses the true date
        var expected = [
            new Date(Date.UTC(2013, 0, 8)),
            new Date(Date.UTC(2013, 1, 1)),
            new Date(Date.UTC(2013, 2, 1)),
            new Date(Date.UTC(2013, 3, 1)),
            new Date(Date.UTC(2013, 4, 1)),
            new Date(Date.UTC(2013, 5, 1))
        ];
        expect(timePeriods).toBeEqualArray(expected);
    });

    it('should not have any tick values when there is no data', function () {
        var opts = {  "data": [], "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        expect(timeline.tickValues_).toBeEqualArray([]);
    });

    it('should compute tick intervals at weekly intervals when charting by day', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 2, 07)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 15)), "count": 4}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.DAY};
        var timeline = new charts.Timeline('#chart', opts);
        var expected = [
            new Date(Date.UTC(2013, 2, 7)),
            new Date(Date.UTC(2013, 2, 14)),
            new Date(Date.UTC(2013, 2, 21)),
            new Date(Date.UTC(2013, 2, 28)),
            new Date(Date.UTC(2013, 3, 4)),
            new Date(Date.UTC(2013, 3, 11))
        ];
        expect(timeline.tickValues_).toBeEqualArray(expected);

    });

    it('should compute the minimum date', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 1, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        expect(timeline.computeMinDate_(data)).toEqual(new Date(Date.UTC(2013, 0, 8)));
    });

    it('should compute the maximum date', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 1, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);
        // the max date is exclusive so it is 1ms higher than the end of the chart
        expect(timeline.computeMaxDate_(data)).toEqual(new Date(Date.UTC(2013, 3, 1) + 1));
    });

    it('should compute the default margins', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 1, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        expect(timeline.vMargin_).toEqual(40);
        expect(timeline.hMargin_).toEqual(70);
    });

    it('should compute the default rotated margins', function () {
        var data = [
            {"date": new Date(Date.UTC(2013, 1, 7)), "count": 2},
            {"date": new Date(Date.UTC(2013, 3, 1)), "count": 4},
            {"date": new Date(Date.UTC(2013, 0, 8)), "count": 7}
        ];
        var opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.YEAR, width:100};
        var timeline = new charts.Timeline('#chart', opts);

        expect(timeline.vMargin_).toEqual(40);
        expect(timeline.hMargin_).toEqual(70);

        opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.MONTH, width:100};
        timeline = new charts.Timeline('#chart', opts);

        expect(timeline.vMargin_).toEqual(80);
        expect(timeline.hMargin_).toEqual(70);

        opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.DAY, width:100};
        timeline = new charts.Timeline('#chart', opts);

        expect(timeline.vMargin_).toEqual(95);
        expect(timeline.hMargin_).toEqual(70);

        opts = {  "data": data, "x": "date", "y": "count", "interval": charts.Timeline.HOUR, width:100};
        timeline = new charts.Timeline('#chart', opts);

        expect(timeline.vMargin_).toEqual(105);
        expect(timeline.hMargin_).toEqual(70);
    });


    it('should notify listeners of filter events', function () {

        var data = [
            {"date": new Date(Date.UTC(2013, 0, 4)), "events": 2},
            {"date": new Date(Date.UTC(2013, 1, 1)), "events": 4},
            {"date": new Date(Date.UTC(2013, 2, 1)), "events": 7},
            {"date": new Date(Date.UTC(2013, 3, 1)), "events": 1}
        ];
        var opts = {  "data": data, "x": "date", "y": "events", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', opts);

        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        timeline.onFilter(callback1);
        timeline.onFilter(callback2);

        var startDate = new Date(Date.UTC(2013, 0, 1));
        var endDate = new Date(Date.UTC(2013, 2, 1));
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
