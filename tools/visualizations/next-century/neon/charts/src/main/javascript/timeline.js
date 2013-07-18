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

/**
 *
 * Creates a new timeline component
 * @namespace charts
 * @class Timeline
 * @param {String} chartSelector The selector for the component in which the timeline will be drawn*
 * @param {Object} opts A collection of key/value pairs used for configuration parameters:
 * <ul>
 *     <li>data (required) - An array of data with the specified x-y data values (note the `y` is optional - see the description of the `y` parameter).</li>
 *     <li>x (required) - The name of the x-attribute (time)</li>
 *     <li>y (optional) - The name of the y-attribute (count). If not specified, each item will contribute 1 to the current count./li>
 *     <li>interval (required) - The interval at which to group the data (e.g. day, month). See the constants in this class</li>
 *     <li>height (optional) - The height of the chart in pixels. If not specified, a preconfigured default value will be used.</li>
 *     <li>width (optional) - The width of the chart in pixels. This will be honored as closely as possible, while still allowing bar widths to be evenly drawn. If not specified, a preconfigured default value will be used.</li>
 *     <li>margin (optional) - An object with any of the elements `top`, `left`, `bottom` or `right`. These are pixel values to override the default margin. If not specified, a preconfigured default value will be used.</li>
 *     <li>step (optional) - The number of the specified intervals. If not specified, a preconfigured default value will be used based on the tick interval that was selected.</li>
 *     <li>tickFormat (optional) - The format of the tick labels. Use the formatting specified by d3 at <a href="https://github.com/mbostock/d3/wiki/Time-Formatting">Time Formatting</a>. If not specified, a preconfigured default value will be used.</li>
 *     <li>style (optional) - a mapping of a bar state to the different attributes to style for that attribute. The available bar states
 *     are active, inactive and hover. The attributes that can be toggled correspond to the underlying svg type used to render the
 *     bar. For example, to modify the the active/inactive bar states, but not do anything on hover this attribute would be
 *     `{ "active" : { "fill" : "blue" }, "inactive" : { "fill" : "red" } }`. The values for the attributes can also be functions
 *     to compute the values. The function takes 2 parameters - the current data for the bar and its index.</li>
 * </ul>
 *
 * @constructor
 *
 * @example
 *     var data = [
 *                 {"date": new Date(2013,2,4), "count": 2},
 *                 {"date": new Date(2013,4,8), "count": 4},
 *                 {"date": new Date(2013,1,1), "count": 7},
 *                 {"date": new Date(2013,1,7), "count": 1}
 *                ];
 *     var opts = { "data" : data, "x": "date", "y": "count", "interval" : charts.Timeline.MONTH};
 *     var timeline = new charts.Timeline('#chart', opts).draw();
 *
 */
charts.Timeline = function (chartSelector, opts) {
    var me = this;
    opts = opts || {};
    this.dateField_ = opts.x;
    var interval = opts.interval || charts.Timeline.DEFAULT_INTERVAL_;
    this.timeInterval_ = charts.Timeline.TIME_INTERVALS_[interval].interval;
    this.tickStep_ = opts.step || charts.Timeline.TIME_INTERVALS_[interval].step;
    var tickFormat = d3.time.format(opts.tickFormat || charts.Timeline.TIME_INTERVALS_[interval].tickFormat);


    // TODO: Extract methods
    charts.BarChart.call(this, chartSelector,
        $.extend({}, opts, {
                'init': this.init_,
                'x': function (item) {
                    return me.computeTimePeriodStart_(item[me.dateField_]);
                },
                'categories': this.computeTimePeriods_,
                'tickValues': this.computeTimeIntervalTicks_,
                'tickFormat': tickFormat,
                'xLabel': 'date',
                'dataKeyTransform': function (aggregated) {
                    // d3 will create a string for the date but we want a date object
                    return aggregated.map(function (d) {
                        d.key = new Date(d.key);
                        return d;
                    });
                }
            }
        ));
};

// TODO: NEON-73 Javascript inheritance library
charts.Timeline.prototype = Object.create(charts.BarChart.prototype);

(function () {
    // override the draw method to also draw the slider
    var oldDrawMethod = charts.Timeline.prototype.draw;
    charts.Timeline.prototype.draw = function () {
        oldDrawMethod.call(this);
        this.drawSlider_();
        return this;
    };
})();

/**
 * The time interval for charting by hour
 * @property HOUR
 * @type {String}
 */
charts.Timeline.HOUR = 'hour';


/**
 * The time interval for charting by day
 * @property DAY
 * @type {String}
 */
charts.Timeline.DAY = 'day';

/**
 * The time interval for charting by month
 * @property MONTH
 * @type {String}
 */
charts.Timeline.MONTH = 'month';

/**
 * The time interval for charting by year
 * @property YEAR
 * @type {String}
 */
charts.Timeline.YEAR = 'year';


charts.Timeline.DEFAULT_INTERVAL_ = charts.Timeline.MONTH;
charts.Timeline.TIME_INTERVALS_ = {};
charts.Timeline.SLIDER_DIV_NAME_ = 'slider';
charts.Timeline.ZERO_DATE_ = new Date(0);
charts.Timeline.FILTER_EVENT_TYPE_ = 'filter';
charts.Timeline.GRANULARITIES_ = [charts.Timeline.HOUR, charts.Timeline.DAY, charts.Timeline.MONTH, charts.Timeline.YEAR];

charts.Timeline.prototype.init_ = function (opts) {
    this.minDate_ = this.computeMinDate_(opts.data);
    this.maxDate_ = this.computeMaxDate_(opts.data);
};

/**
 * Adds a listener to be notified of filter events
 * @method onFilter
 * @param {Function} callback Notified when the filters change. It is called with 2 parameters - the start date
 * and end dates of the filters
 */
charts.Timeline.prototype.onFilter = function (callback) {

    $(this).on(charts.Timeline.FILTER_EVENT_TYPE_,
        function (event, filterStartDate, filterEndDate) {
            callback(filterStartDate, filterEndDate);
        });
};


charts.Timeline.prototype.computeMinDate_ = function (data) {
    var me = this;
    var minDate = d3.min(data, function (d) {
        return d[me.dateField_];
    });
    // minDate will be undefined if data is empty
    return minDate ? minDate : charts.Timeline.ZERO_DATE_;
};

charts.Timeline.prototype.computeMaxDate_ = function (data) {
    var me = this;
    var maxDate = d3.max(data, function (d) {
        return d[me.dateField_];
    });
    // maxDate will be undefined if data is empty
    // the +1 is so the max date is exclusive
    return maxDate ? new Date(maxDate.getTime() + 1) : charts.Timeline.ZERO_DATE_;
};


/**
 * Computes the start dates for each of the time periods in the chart
 * @method computeTimePeriods_
 * @param {Array} data The data being displayed in the chart
 * @return {Array}
 * @private
 */
charts.Timeline.prototype.computeTimePeriods_ = function (data) {
    var timePeriods = [];
    if (charts.Timeline.isValidDate_(this.minDate_) && charts.Timeline.isValidDate_(this.maxDate_)) {
        Array.prototype.push.apply(timePeriods, this.timeInterval_.range(this.timeInterval_(this.minDate_), this.maxDate_));
        // set the start time period to the true start. do this after the time periods are computed, otherwise the
        // timeInterval_.range method above will return the first time period after the start if the time period
        // does not start exactly at an even interval
        timePeriods[0] = this.computeTimePeriodStart_(timePeriods[0]);
    }
    return timePeriods;
};

charts.Timeline.isValidDate_ = function (date) {
    return date > charts.Timeline.ZERO_DATE_;
};

charts.Timeline.prototype.computeTimeIntervalTicks_ = function () {
    var tickValues = [];
    var currentTick = this.minDate_;
    while (currentTick < this.maxDate_) {
        tickValues.push(currentTick);
        currentTick = this.timeInterval_.offset(this.timeInterval_(currentTick), this.tickStep_);
    }
    return tickValues;
};

charts.Timeline.prototype.drawSlider_ = function () {
    var me = this;
    $(this.chartSelector_).append('<div id="' + charts.Timeline.SLIDER_DIV_NAME_ + '"/>');
    me.createSlider_();
};

charts.Timeline.prototype.createSlider_ = function () {
    var me = this;
    // note the slider uses pixel values since it can then use the d3 scales to map to the dates
    $('#' + charts.Timeline.SLIDER_DIV_NAME_).slider({
        range: true,
        min: 0,
        max: this.plotWidth_,
        step: this.x_.rangeBand(),
        values: [ 0, this.plotWidth_ ],
        change: $.proxy(charts.Timeline.prototype.doSliderChange_, me)
    });
    $('#' + charts.Timeline.SLIDER_DIV_NAME_).width(me.plotWidth_).css({'margin-left': me.margin.left + 'px', 'margin-right': me.margin.right + 'px'});
    if (me.data_.length === 0) {
        $('#' + charts.Timeline.SLIDER_DIV_NAME_).slider('disable');
    }
};

charts.Timeline.prototype.doSliderChange_ = function (event, slider) {
    // ordinal scales to not support inverting, so figure out the time period that was selected
    // Note: If we turn off snapping, we'll have to first find the nearest period boundary and use that value
    var filterStartDate = this.getDate_(slider.values[0]);
    var filterEndDate = this.getDate_(slider.values[1]);
    this.styleInactiveData_(filterStartDate, filterEndDate);
    this.notifyFilterListeners_(filterStartDate, filterEndDate);
};


/**
 * Removes the listener for filters
 * @method removeFilterListeners
 */
charts.Timeline.prototype.removeFilterListeners = function () {
    $(this).off(charts.Timeline.FILTER_EVENT_TYPE_);
};

charts.Timeline.prototype.styleInactiveData_ = function (filterStartDate, filterEndDate) {

    this.setInactive(function (item) {
        var date = item.key;
        return !(filterStartDate <= date && date < filterEndDate);
    });

};

charts.Timeline.prototype.notifyFilterListeners_ = function (filterStartDate, filterEndDate) {
    $(this).trigger(charts.Timeline.FILTER_EVENT_TYPE_, [filterStartDate, filterEndDate]);
};


charts.Timeline.prototype.getDate_ = function (pixelValue) {
    if (pixelValue === this.plotWidth_) {
        return this.maxDate_;
    }
    var index = pixelValue / this.x_.rangeBand();
    return this.xAxisCategories_[index];
};

charts.Timeline.prototype.computeTimePeriodStart_ = function (date) {
    var timePeriodStart = this.timeInterval_(date);
    // if the date is in the first time period, taking the floor to get the time period start
    // may result in a date before the true start. check for that case.
    if (timePeriodStart < this.minDate_) {
        timePeriodStart = this.minDate_;
    }
    return timePeriodStart;
};

/**
 *
 * Creates the information necessary to properly format time for the given interval
 * @param {String} interval The interval for which the metadata is being created
 * @param {String} tickFormat The format used to show the tick labels
 * @param {int} step The number of the particular interval units to place the ticks
 * @return {Object}
 * @method
 * @private
 */
charts.Timeline.createTimeIntervalMethods_ = function (interval, tickFormat, step) {
    return {
        'interval': interval,
        'tickFormat': tickFormat,
        'step': step || 1
    };
};

charts.Timeline.TIME_INTERVALS_[charts.Timeline.HOUR] = charts.Timeline.createTimeIntervalMethods_(d3.time.hour, '%d-%b %H:%M', 12);
charts.Timeline.TIME_INTERVALS_[charts.Timeline.DAY] = charts.Timeline.createTimeIntervalMethods_(d3.time.day, '%d-%b-%Y', 7);
charts.Timeline.TIME_INTERVALS_[charts.Timeline.MONTH] = charts.Timeline.createTimeIntervalMethods_(d3.time.month, '%b-%Y');
charts.Timeline.TIME_INTERVALS_[charts.Timeline.YEAR] = charts.Timeline.createTimeIntervalMethods_(d3.time.year, '%Y');