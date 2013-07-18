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
 * Creates a new bar chart component
 * @namespace charts
 * @class BarChart
 * @param {String} chartSelector The selector for the component in which the chart will be drawn
 * @param {Object} opts A collection of key/value pairs used for configuration parameters:
 * <ul>
 *     <li>data (required) - An array of data with the specified x-y data values (note the `y` is optional - see the description of the `y` parameter).</li>
 *     <li>x (required) - The name of the x-attribute</li>
 *     <li>y (optional) - The name of the y-attribute. If not specified, each item will contribute 1 to the current count./li>
 *     <li>height (optional) - The height of the chart in pixels. If not specified, a preconfigured default value will be used.</li>
 *     <li>width (optional) - The width of the chart in pixels. This will be honored as closely as possible, while still allowing bar widths to be evenly drawn. If not specified, a preconfigured default value will be used.</li>
 *     <li>margin (optional) - An object with any of the elements `top`, `left`, `bottom` or `right`. These are pixel values to override the default margin. If not specified, a preconfigured default value will be used.</li>
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
 *     var opts = { "data" : data, "x": "date", "y": "count", "interval" : charts.BarChart.MONTH};
 *     var barchart = new charts.BarChart('#chart', opts).draw();
 *
 */
charts.BarChart = function (chartSelector, opts) {
    opts = opts || {};
    this.chartSelector_ = chartSelector;
    this.height = opts.height || charts.BarChart.DEFAULT_HEIGHT_;
    this.width = opts.width || charts.BarChart.DEFAULT_WIDTH_;
    this.xAttribute_ = opts.x;
    this.xLabel_ = opts.xLabel || this.determineXLabel_();
    this.yAttribute_ = opts.y;
    this.margin = $.extend({}, charts.BarChart.DEFAULT_MARGIN_, opts.margin || {});
    this.hMargin_ = this.margin.left + this.margin.right;
    this.vMargin_ = this.margin.top + this.margin.bottom;
    this.dataKeyTransform_ = opts.dataKeyTransform;

    if (opts.init) {
        opts.init.call(this, opts);
    }

    // tick formatting/values may be undefined in which case d3's default will be used
    this.tickFormat_ = opts.tickFormat;
    this.tickValues_ = this.computeTickValues_(opts.tickValues);
    this.xAxisCategories_ = this.createCategories_(opts.categories ? opts.categories : this.createXAxisCategoriesFromUniqueValues_, opts.data);
    this.data_ = this.aggregateData_(opts.data);

    this.x_ = this.createXScale_();
    // set the width to be as close to the user specified size (but not larger) so the bars divide evenly into
    // the plot area
    this.plotWidth_ = this.computePlotWidth_();
    this.x_.rangeRoundBands([0, this.plotWidth_]);
    this.y_ = this.createYScale_();
    this.xAxis_ = this.createXAxis_();
    this.yAxis_ = this.createYAxis_();
    this.style_ = $.extend({}, charts.BarChart.DEFAULT_STYLE_, opts.style);
};

charts.BarChart.DEFAULT_HEIGHT_ = 400;
charts.BarChart.DEFAULT_WIDTH_ = 600;
charts.BarChart.DEFAULT_MARGIN_ = {top: 20, bottom: 20, left: 40, right: 30};
charts.BarChart.TOOLTIP_ID_ = 'tooltip';
charts.BarChart.SVG_ELEMENT_ = 'rect';
charts.BarChart.ACTIVE_STYLE_KEY_ = 'active';
charts.BarChart.INACTIVE_STYLE_KEY_ = 'inactive';
charts.BarChart.HOVER_STYLE_KEY_ = 'hover';

// the bar classes are not used for styling directly through the CSS but as
// selectors to indicate which style functions to apply. this is because the styles are
// applied by functions and not by straight CSS
charts.BarChart.BAR_CLASS_ = 'bar';

// the active/inactive/hover classes are additional classes appended to the bars but just use the same name
// as the bar class concatenated with the state, so an active bar would have the classes 'bar active-bar'
charts.BarChart.ACTIVE_BAR_CLASS_ = charts.BarChart.ACTIVE_STYLE_KEY_ + '-' + charts.BarChart.BAR_CLASS_;
charts.BarChart.INACTIVE_BAR_CLASS_ = charts.BarChart.INACTIVE_STYLE_KEY_ + '-' + charts.BarChart.BAR_CLASS_;
charts.BarChart.HOVER_BAR_CLASS_ = charts.BarChart.HOVER_STYLE_KEY_ + '-' + charts.BarChart.BAR_CLASS_;

charts.BarChart.DEFAULT_ACTIVE_BAR_FILL_COLOR_ = 'steelblue';
charts.BarChart.DEFAULT_INACTIVE_BAR_FILL_COLOR_ = 'lightgrey';
charts.BarChart.defaultActiveBarStyle_ = { 'fill': charts.BarChart.DEFAULT_ACTIVE_BAR_FILL_COLOR_ };
charts.BarChart.defaultInactiveBarStyle_ = { 'fill': charts.BarChart.DEFAULT_INACTIVE_BAR_FILL_COLOR_ };
charts.BarChart.defaultHoverBarStyle_ = {};

charts.BarChart.DEFAULT_STYLE_ = {};
charts.BarChart.DEFAULT_STYLE_[charts.BarChart.ACTIVE_STYLE_KEY_] = charts.BarChart.defaultActiveBarStyle_;
charts.BarChart.DEFAULT_STYLE_[charts.BarChart.INACTIVE_STYLE_KEY_] = charts.BarChart.defaultInactiveBarStyle_;
charts.BarChart.DEFAULT_STYLE_[charts.BarChart.HOVER_STYLE_KEY_] = charts.BarChart.defaultHoverBarStyle_;

charts.BarChart.prototype.categoryForItem_ = function (item) {
    if (typeof this.xAttribute_ === 'function') {
        return this.xAttribute_.call(this, item);
    }
    return item[this.xAttribute_];
};

charts.BarChart.prototype.determineXLabel_ = function () {
    if (typeof this.xAttribute_ === 'string') {
        return this.xAttribute_;
    }
    return 'x';
};

charts.BarChart.prototype.createCategories_ = function (categories, data) {
    if (typeof categories === 'function') {
        return categories.call(this, data);
    }
    return categories;
};

charts.BarChart.prototype.computeTickValues_ = function (tickValues) {
    if (typeof tickValues === 'function') {
        return tickValues.call(this);
    }
    return tickValues;
};

charts.BarChart.prototype.createXAxisCategoriesFromUniqueValues_ = function (data) {
    var me = this;
    return _.chain(data).map(function (item) {
        return me.categoryForItem_(item);
    }).unique().compact().sort().value();
};

charts.BarChart.prototype.createXScale_ = function () {
    return d3.scale.ordinal()
        .domain(this.xAxisCategories_)
        .rangeRoundBands([0, this.width - this.hMargin_]);
};

charts.BarChart.prototype.createYScale_ = function () {
    var maxCount = d3.max(this.data_, function (d) {
        return d.values;
    });

    // may be NaN if no data
    if (!maxCount) {
        maxCount = 0;
    }
    return d3.scale.linear()
        .domain([0, maxCount])
        .rangeRound([this.height - this.vMargin_, 0]);
};

charts.BarChart.prototype.computePlotWidth_ = function () {
    if (this.xAxisCategories_.length > 0) {
        return this.x_.rangeBand() * this.xAxisCategories_.length;
    }
    return this.width;
};

charts.BarChart.prototype.createXAxis_ = function () {
    var xAxis = d3.svg.axis()
        .scale(this.x_)
        .orient('bottom');

    if (this.tickFormat_) {
        xAxis = xAxis.tickFormat(this.tickFormat_);
    }
    if (this.tickValues_) {
        xAxis = xAxis.tickValues(this.tickValues_);
    }
    return xAxis;
};

charts.BarChart.prototype.createYAxis_ = function () {
    return d3.svg.axis()
        .scale(this.y_)
        .orient('left')
        .tickFormat(charts.BarChart.createYAxisTickFormat_())
        .tickValues(this.y_.domain());
};

charts.BarChart.createYAxisTickFormat_ = function () {
    return function (val) {
        return val === 0 ? val : d3.format('.2s')(val);
    };
};

/**
 * Draws the bar chart in the component specified in the constructor
 * @method draw
 * @return {charts.BarChart} This bar chart
 */
charts.BarChart.prototype.draw = function () {
    var chart = this.drawChartSVG_();
    this.bindData_(chart);
    this.drawXAxis_(chart);
    this.drawYAxis_(chart);
    return this;
};

charts.BarChart.prototype.drawChartSVG_ = function () {
    var chart = d3.select(this.chartSelector_)
        .append('svg')
        .attr('id', 'plot')
        .attr('width', this.plotWidth_ + this.hMargin_)
        .attr('height', this.height)
        .append('g')
        .attr('transform', 'translate(' + this.margin.left + ',' + this.margin.top + ')');
    return chart;
};

charts.BarChart.prototype.bindData_ = function (chart) {
    var me = this;
    var bars = chart.selectAll(charts.BarChart.SVG_ELEMENT_)
        .data(this.data_)
        .enter().append(charts.BarChart.SVG_ELEMENT_)
        .attr('class', charts.BarChart.BAR_CLASS_ + ' ' + charts.BarChart.ACTIVE_BAR_CLASS_)
        .attr('x', function (d) {
            return me.x_(d.key);
        })
        .attr('y', function (d) {
            return me.y_(d.values);
        })
        .attr('width', this.x_.rangeBand())
        .attr('height', function (d) {
            return me.height - me.vMargin_ - me.y_(d.values);
        })
        .on('mouseover', function (d) {
            me.toggleHoverStyle_(d3.select(this), true);
            me.showTooltip_(d);
        })
        .on('mouseout', function () {
            me.toggleHoverStyle_(d3.select(this), false);
            me.hideTooltip_();
        });
    // initially all bars active, so just apply the active style
    this.applyStyle_(bars, charts.BarChart.ACTIVE_STYLE_KEY_);
};

charts.BarChart.prototype.toggleHoverStyle_ = function (selection, hover) {
    selection.classed(charts.BarChart.HOVER_BAR_CLASS_, hover);

    // when hovering, apply the hover style, otherwise revert the style based on the current class
    var style;
    if (hover) {
        style = charts.BarChart.HOVER_STYLE_KEY_;
    }
    else {
        style = selection.classed(charts.BarChart.ACTIVE_BAR_CLASS_) ?
            charts.BarChart.ACTIVE_STYLE_KEY_ : charts.BarChart.INACTIVE_STYLE_KEY_;
    }

    this.applyStyle_(selection, style);
};

charts.BarChart.prototype.applyStyle_ = function (selection, styleKey) {
    var attrMap = this.style_[styleKey];
    Object.keys(attrMap).forEach(function (key) {
        var attrVal = attrMap[key];
        selection.attr(key, attrVal);
    });
};

charts.BarChart.prototype.setInactive = function (predicate) {
    var allBars = d3.selectAll('.' + charts.BarChart.BAR_CLASS_);

    // remove existing active/inactive classes then toggle on the correct one. this allows us to keep any other
    // classes (rather than just replacing the class with inactive/active)
    allBars.classed(charts.BarChart.INACTIVE_BAR_CLASS_, false);
    allBars.classed(charts.BarChart.ACTIVE_BAR_CLASS_, false);

    // set any matching the predicate to inactive
    allBars.classed(charts.BarChart.INACTIVE_BAR_CLASS_, predicate);

    // those that are not inactive are set to active
    d3.selectAll('.' + charts.BarChart.BAR_CLASS_ + ':not(.' + charts.BarChart.INACTIVE_BAR_CLASS_ + ')')
        .classed(charts.BarChart.ACTIVE_BAR_CLASS_, true);

    // update the rendered bars
    this.applyStyle_(d3.selectAll('.' + charts.BarChart.ACTIVE_BAR_CLASS_), charts.BarChart.ACTIVE_STYLE_KEY_);
    this.applyStyle_(d3.selectAll('.' + charts.BarChart.INACTIVE_BAR_CLASS_), charts.BarChart.INACTIVE_STYLE_KEY_);
};

charts.BarChart.prototype.showTooltip_ = function (item) {
    var periodStartPixels = this.x_(item.key);
    var tooltip = this.createTooltip_(item);
    // initially hidden because it will fade in
    tooltip.hide();
    $(this.chartSelector_).append(tooltip);
    // must center after appending so its width can be properly computed
    this.centerTooltip_(tooltip, item, periodStartPixels);
    tooltip.fadeIn(500);

};

charts.BarChart.prototype.createTooltip_ = function (item) {
    var tooltip = $('<div/>', {
        "class": "charttooltip",
        id: charts.BarChart.TOOLTIP_ID_
    });

    // if a tick format was specified, use that to format the tooltip for a consistent look
    var xValue = this.tickFormat_ ? this.tickFormat_(item.key) : item.key;
    var xAttributeHtml = $('<div/>').html(this.xLabel_ + ': ' + xValue);
    var yLabel = this.yAttribute_ ? this.yAttribute_ : "Count";
    var yAttributeHtml = $('<div/>').html(yLabel + ': ' + item.values);
    tooltip.append(xAttributeHtml);
    tooltip.append(yAttributeHtml);
    return tooltip;
};

charts.BarChart.prototype.centerTooltip_ = function (tooltip, data, periodStartPixels) {
    var centerPointX = periodStartPixels + this.x_.rangeBand() / 2;
    var centerPointY = this.y_(data.values);
    // center the tooltip on the selected bar
    tooltip.css({
        'margin-left': this.margin.left + $(this.chartSelector_).position().left + 'px',
        'margin-top': this.margin.top + $(this.chartSelector_).position().top + 'px',
        'top': (centerPointY - $('#' + charts.BarChart.TOOLTIP_ID_).outerHeight() / 2) + 'px',
        'left': (centerPointX - $('#' + charts.BarChart.TOOLTIP_ID_).outerWidth() / 2) + 'px'
    });
};

charts.BarChart.prototype.hideTooltip_ = function () {
    $('#' + charts.BarChart.TOOLTIP_ID_).remove();
};


charts.BarChart.prototype.drawXAxis_ = function (chart) {
    chart.append('g')
        .attr('class', 'x axis')
        .attr('transform', 'translate(0,' + (this.height - this.vMargin_) + ')')
        .call(this.xAxis_);
};

charts.BarChart.prototype.drawYAxis_ = function (chart) {
    chart.append('g')
        .attr('class', 'y axis')
        .call(this.yAxis_);
};

/**
 * Aggregates the data by category
 * @method aggregateData_
 * @param {Array} data The raw data to aggregate
 * @private
 * @return {Object} An array of objects whose keys are `key` and `values`, whose values are the x-category
 * and the number of items in that category period respectively
 */
charts.BarChart.prototype.aggregateData_ = function (data) {
    // TODO: Extract methods
    var me = this;
    var aggregated = d3.nest().key(function (d) {
        return me.categoryForItem_(d);
    }).rollup(function (d) {
            return d3.sum(d, function (el) {
                return me.yAttribute_ ? el[me.yAttribute_] : 1;
            });
        }).entries(data);


    // The keys here will be a string, but some charts may want it in another form
    if (this.dataKeyTransform_) {
        aggregated = this.dataKeyTransform_.call(this, aggregated);
    }

    // TODO: Extract methods
    // if there is data in non-existent categories, remove it
    aggregated = _.reject(aggregated, function (item) {
        var key = item.key;
        return _.isUndefined(_.find(me.xAxisCategories_, function (category) {
            // dates won't compare with === since they are different object, so compare using the time values
            if (key instanceof Date && category instanceof Date) {
                return category.getTime() === key.getTime();
            }
            return category === key;
        }));
    });

    return aggregated;
};