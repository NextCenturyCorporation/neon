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
    opts = opts || {};
    this.chartSelector_ = chartSelector;
    var interval = opts.interval || charts.Timeline.DEFAULT_INTERVAL_;
    this.timeInterval_ = charts.Timeline.TIME_INTERVALS_[interval].interval;
    this.tickFormat_ = d3.time.format(opts.tickFormat || charts.Timeline.TIME_INTERVALS_[interval].tickFormat);
    this.tickStep_ = opts.step || charts.Timeline.TIME_INTERVALS_[interval].step;
    this.height_ = opts.height || charts.Timeline.DEFAULT_HEIGHT_;
    this.width_ = opts.width || charts.Timeline.DEFAULT_WIDTH_;
    this.xAttribute_ = opts.x;
    this.yAttribute_ = opts.y;
    this.margin_ = $.extend({}, charts.Timeline.DEFAULT_MARGIN_, opts.margin || {});
    this.hMargin_ = this.margin_.left + this.margin_.right;
    this.vMargin_ = this.margin_.top + this.margin_.bottom;
    this.data_ = this.aggregateData_(opts.data);
    // use the raw data for min and max date so we can get the true values
    this.minDate_ = this.computeMinDate_(opts.data);
    this.maxDate_ = this.computeMaxDate_(opts.data);
    this.timePeriods_ = this.computeTimePeriods_();
    this.x_ = this.createXScale_();
    // set the width to be as close to the user specified size (but not larger) so the bars divide evenly into
    // the plot area
    this.plotWidth_ = this.computePlotWidth_();
    this.x_.rangeRoundBands([0, this.plotWidth_]);
    this.y_ = this.createYScale_();
    this.xAxis_ = this.createXAxis_();
    this.yAxis_ = this.createYAxis_();
};

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


charts.Timeline.DEFAULT_HEIGHT_ = 400;
charts.Timeline.DEFAULT_WIDTH_ = 600;
charts.Timeline.DEFAULT_INTERVAL_ = charts.Timeline.MONTH;
charts.Timeline.ACTIVE_BAR_CLASS_ = 'bar active';
charts.Timeline.INACTIVE_BAR_CLASS_ = 'bar inactive';
charts.Timeline.TIME_INTERVALS_ = {};
charts.Timeline.SLIDER_DIV_NAME_ = 'slider';
charts.Timeline.ZERO_DATE_ = new Date(0);
charts.Timeline.DEFAULT_MARGIN_ = {top: 20, bottom: 20, left: 30, right: 30};
charts.Timeline.FILTER_EVENT_TYPE_ = 'filter';
charts.Timeline.TOOLTIP_ID_ = 'tooltip';


/**
 * Draws the timeline in the component specified in the constructor
 * @method draw
 * @return {charts.Timeline} This timeline
 */
charts.Timeline.prototype.draw = function () {
    this.drawChart_();
    this.drawSlider_();
    return this;
};

charts.Timeline.prototype.computeTimePeriods_ = function () {
    var timePeriods = [];
    if (charts.Timeline.isValidDate_(this.minDate_) && charts.Timeline.isValidDate_(this.maxDate_)) {
        Array.prototype.push.apply(timePeriods, this.timeInterval_.range(this.timeInterval_(this.minDate_), this.maxDate_));
    }
    return timePeriods;
};

charts.Timeline.isValidDate_ = function (date) {
    return date > charts.Timeline.ZERO_DATE_;
};

charts.Timeline.prototype.computeMinDate_ = function (data) {
    var me = this;
    var minDate = d3.min(data, function (d) {
        return d[me.xAttribute_];
    });
    // minDate will be undefined if data is empty
    return minDate ? minDate : charts.Timeline.ZERO_DATE_;
};

charts.Timeline.prototype.computeMaxDate_ = function (data) {
    var me = this;
    var maxDate = d3.max(data, function (d) {
        return d[me.xAttribute_];
    });
    // maxDate will be undefined if data is empty
    // the +1 is so the max date is exclusive
    return maxDate ? new Date(maxDate.getTime() + 1) : charts.Timeline.ZERO_DATE_;
};

charts.Timeline.prototype.createXScale_ = function () {
    // use an ordinal scale since each bar represents a discrete time block
    return d3.scale.ordinal()
        .domain(this.timePeriods_)
        .rangeRoundBands([0, this.width_ - this.hMargin_]);
};

charts.Timeline.prototype.createYScale_ = function () {
    var maxCount = d3.max(this.data_, function (d) {
        return d.values;
    });
    // may be NaN if no data
    if (!maxCount) {
        maxCount = 0;
    }
    return d3.scale.linear()
        .domain([0, maxCount])
        .rangeRound([this.height_ - this.vMargin_, 0]);
};

charts.Timeline.prototype.computePlotWidth_ = function () {
    if (this.timePeriods_.length > 0) {
        return this.x_.rangeBand() * this.timePeriods_.length;
    }
    return this.width_;
};

charts.Timeline.prototype.createXAxis_ = function () {
    var tickValues = this.computeTickValues_();
    return d3.svg.axis()
        .scale(this.x_)
        .orient('bottom')
        .tickFormat(this.tickFormat_)
        .tickValues(tickValues);
};

charts.Timeline.prototype.computeTickValues_ = function () {
    var tickValues = [];
    var currentTick = this.minDate_;
    while (currentTick < this.maxDate_) {
        tickValues.push(currentTick);
        currentTick = this.timeInterval_.offset(this.timeInterval_.floor(currentTick), this.tickStep_);
    }
    return tickValues;
};

charts.Timeline.prototype.createYAxis_ = function () {
    return d3.svg.axis()
        .scale(this.y_)
        .orient('left')
        .tickFormat(d3.format('d'))
        .tickValues(this.y_.domain());
};

charts.Timeline.prototype.drawChart_ = function () {
    var chart = this.drawChartSVG_();
    this.bindData_(chart);
    this.drawXAxis_(chart);
    this.drawYAxis_(chart);
};

charts.Timeline.prototype.drawChartSVG_ = function () {
    var chart = d3.select(this.chartSelector_)
        .append('svg')
        .attr('id', 'plot')
        .attr('width', this.plotWidth_ + this.hMargin_)
        .attr('height', this.height_)
        .append('g')
        .attr('transform', 'translate(' + this.margin_.left + ',' + this.margin_.top + ')');
    return chart;
};

charts.Timeline.prototype.bindData_ = function (chart) {
    var me = this;
    chart.selectAll('rect')
        .data(this.data_)
        .enter().append('rect')
        .attr('class', charts.Timeline.ACTIVE_BAR_CLASS_)
        .attr('x', function (d) {
            return me.x_(me.timeInterval_(d.key));
        })
        .attr('y', function (d) {
            return me.y_(d.values);
        })
        .attr('width', this.x_.rangeBand())
        .attr('height', function (d) {
            return me.height_ - me.vMargin_ - me.y_(d.values);
        })
        .on('mouseover', function (d) {
            me.showTooltip_(d);
        })
        .on('mouseout', function () {
            me.hideTooltip_();
        });
};

/**
 * Aggregates the data based on the currently selected time period
 * @method aggregateData_
 * @param {Array} data The raw data to aggregate
 * @private
 * @return {Object} An array of objects whose keys are `key` and `values`, whose values are the time period start
 * and the number of items in that time period respectively
 */
charts.Timeline.prototype.aggregateData_ = function (data) {
    var me = this;
    return d3.nest().key(function (d) {
        return me.timeInterval_(d[me.xAttribute_]);
    }).rollup(function (d) {
            return d3.sum(d, function (el) {
                return me.yAttribute_ ? el[me.yAttribute_] : 1;
            });
        }).entries(data).map(function (d) {
            // d3 will create a string for the date but we want the data object
            d.key = new Date(d.key);
            return d;
        });
};

charts.Timeline.prototype.showTooltip_ = function (data) {
    var periodStartPixels = this.x_(this.timeInterval_(data.key));
    var tooltip = this.createTooltip_(data, this.getDate_(periodStartPixels));
    // initially hidden because it will fade in
    tooltip.hide();
    $(this.chartSelector_).append(tooltip);
    // must center after appending so its width can be properly computed
    this.centerTooltip_(tooltip, data, periodStartPixels);
    tooltip.fadeIn(500);

};

charts.Timeline.prototype.createTooltip_ = function (data, periodStartDate) {
    var tooltip = $('<div/>', {
        "class": "charttooltip",
        id: charts.Timeline.TOOLTIP_ID_
    });

    var xAttributeHtml = $('<div/>').html(this.xAttribute_ + ': ' + this.tickFormat_(periodStartDate));
    var yLabel = this.yAttribute_ ? this.yAttribute_ : "Count";
    var yAttributeHtml = $('<div/>').html(yLabel + ': ' + data.values);
    tooltip.append(xAttributeHtml);
    tooltip.append(yAttributeHtml);
    return tooltip;
};

charts.Timeline.prototype.centerTooltip_ = function (tooltip, data, periodStartPixels) {
    var centerPointX = periodStartPixels + this.x_.rangeBand() / 2;
    var centerPointY = this.y_(data.values);
    // center the tooltip on the selected bar
    tooltip.css({
        'margin-left': this.margin_.left + $(this.chartSelector_).position().left + 'px',
        'margin-top': this.margin_.top + $(this.chartSelector_).position().top + 'px',
        'top': (centerPointY - $('#' + charts.Timeline.TOOLTIP_ID_).innerHeight() / 2) + 'px',
        'left': (centerPointX - $('#' + charts.Timeline.TOOLTIP_ID_).innerWidth() / 2) + 'px'
    });
};

charts.Timeline.prototype.hideTooltip_ = function () {
    $('#' + charts.Timeline.TOOLTIP_ID_).remove();
};

charts.Timeline.prototype.drawXAxis_ = function (chart) {
    chart.append('g')
        .attr('class', 'x axis')
        .attr('transform', 'translate(0,' + (this.height_ - this.vMargin_) + ')')
        .call(this.xAxis_);
};

charts.Timeline.prototype.drawYAxis_ = function (chart) {
    chart.append('g')
        .attr('class', 'y axis')
        .call(this.yAxis_);
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
    $('#' + charts.Timeline.SLIDER_DIV_NAME_).width(me.plotWidth_).css({'margin-left': me.margin_.left + 'px', 'margin-right': me.margin_.right + 'px'});
    if (me.data_.length === 0) {
        $('#' + charts.Timeline.SLIDER_DIV_NAME_).slider('disable');
    }
};

charts.Timeline.prototype.doSliderChange_ = function (event, slider) {
    // ordinal scales to not support inverting, so figure out the time period that was selected
    // Note: If we turn off snapping, we'll have to first find the nearest period boundary and use that value
    var filterStartDate = this.getDate_(slider.values[0]);
    var filterEndDate = this.getDate_(slider.values[1]);
    this.hideInactiveData_(filterStartDate, filterEndDate);
    this.notifyFilterListeners_(filterStartDate, filterEndDate);
};

charts.Timeline.prototype.notifyFilterListeners_ = function (filterStartDate, filterEndDate) {
    $(this).trigger(charts.Timeline.FILTER_EVENT_TYPE_, [filterStartDate, filterEndDate]);
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

/**
 * Removes the listener for filters
 * @method removeFilterListeners
 */
charts.Timeline.prototype.removeFilterListeners = function () {
    $(this).off(charts.Timeline.FILTER_EVENT_TYPE_);
};

charts.Timeline.prototype.hideInactiveData_ = function (filterStartDate, filterEndDate) {
    var me = this;
    d3.selectAll('.bar').attr('class', function (d) {
        var date = d.key;
        return (filterStartDate <= date && date < filterEndDate) ? charts.Timeline.ACTIVE_BAR_CLASS_ : charts.Timeline.INACTIVE_BAR_CLASS_;
    });
};

charts.Timeline.prototype.getDate_ = function (pixelValue) {
    // the d3 chart uses even intervals, but if the minimum/maximum point is selected, return those true points
    if (pixelValue === 0) {
        return this.minDate_;
    }
    if (pixelValue === this.plotWidth_) {
        return this.maxDate_;
    }
    var index = pixelValue / this.x_.rangeBand();
    return this.timePeriods_[index];
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

charts.Timeline.TIME_INTERVALS_[charts.Timeline.DAY] = charts.Timeline.createTimeIntervalMethods_(d3.time.day, '%d-%b-%Y', 7);
charts.Timeline.TIME_INTERVALS_[charts.Timeline.MONTH] = charts.Timeline.createTimeIntervalMethods_(d3.time.month, '%b-%Y');
charts.Timeline.TIME_INTERVALS_[charts.Timeline.YEAR] = charts.Timeline.createTimeIntervalMethods_(d3.time.year, '%Y');
