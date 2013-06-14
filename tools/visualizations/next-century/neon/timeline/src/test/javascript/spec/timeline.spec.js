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

    it('should compute the time periods', function () {
        var data = [
            {"date": new Date(2013,2,7), "count": 2},
            {"date": new Date(2013,3,1), "count": 4},
            {"date": new Date(2013,0,8), "count": 7},
            {"date": new Date(2013,2,1), "count": 1},
            {"date": new Date(2013,4,1), "count": 9},
            {"date": new Date(2013,5,13), "count": 8}
        ];
        var opts = { "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', data, opts);
        var timePeriods = timeline.timePeriods_;

        // even though the data starts at 1/8, the time period begins at the beginning of the month (the user
        // only ever sees the date indicating the beginning of the data - internally it uses the beginning of the
        // time period date)
        var expected = [
            new Date(2013,0,1),
            new Date(2013,1,1),
            new Date(2013,2,1),
            new Date(2013,3,1),
            new Date(2013,4,1),
            new Date(2013,5,1)
        ];
        expect(timePeriods).toBeEqualArray(expected);
    });

    it('should not have any tick values when there is not data', function() {
        var data = [];
        var opts = { "x": "date", "y": "count", "interval": charts.Timeline.MONTH};
        var timeline = new charts.Timeline('#chart', data, opts);
        var tickValues = timeline.computeTickValues_();
        expect(tickValues).toBeEqualArray([]);
    });

    it('should compute tick intervals at regular intervals when charting by day', function() {
        var data = [
            {"date": new Date(2013,2,07), "count": 2},
            {"date": new Date(2013,3,15), "count": 4}
        ];
        var opts = { "x": "date", "y": "count", "interval": charts.Timeline.DAY};
        var timeline = new charts.Timeline('#chart', data, opts);
        var expected = [
            new Date(2013,2,7),
            new Date(2013,2,14),
            new Date(2013,2,21),
            new Date(2013,2,28),
            new Date(2013,3,4),
            new Date(2013,3,11)
        ];

        var tickValues = timeline.computeTickValues_();
        expect(tickValues).toBeEqualArray(expected);

    });

});