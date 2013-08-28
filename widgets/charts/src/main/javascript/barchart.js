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
 *     <li>data (required) - An array of data with the specified x-y data values (note the `y` is optional - see the
 *     description of the `y` parameter).</li>
 *     <li>x (required) - The name of the x-attribute or a function that takes 1 parameter (the current item)
 *     and returns the x value from the item. Note that all x-values must be of the same data type</li>
 *     <li>y (optional) - The name of the y-attribute. If not specified, each item will contribute 1 to the current count.</li>
 *     <li>xLabel (optional) - The label to show for the x-attribute (e.g. on tooltips). If not specified, this will
 *     default to using the name of the attribute specified in x (if x is a function, then the value "x" will be used).
 *     This is useful if the x-attribute name is not the same as how it should be displayed to users.</li>
 *     <li>yLabel (optional) - The label to show for the y-attribute (e.g. on tooltips). If not specified, this will
 *     default to using the name of the attribute specified in y (if no y value is specified, then the value "Count" will be used).
 *     This is useful if the y-attribute name is not the same as how it should be displayed to users.</li>
 *     <li>height (optional) - The height of the chart in pixels. If not specified, a preconfigured default value will be used.</li>
 *     <li>width (optional) - The width of the chart in pixels. This will be honored as closely as possible, while still allowing bar widths to be evenly drawn. If not specified, a preconfigured default value will be used.</li>
 *     <li>margin (optional) - An object with any of the elements `top`, `left`, `bottom` or `right`. These are pixel values to override the default margin. If not specified, a preconfigured default value will be used.</li>
 *     <li>style (optional) - a mapping of a bar state to the different attributes to style for that attribute. The available bar states
 *     are active (default bar state), inactive (a visual state to indicate to the user that the bar should be seen
 *     as inactive - the meaning of this is chart specified - see {{#crossLink "charts.BarChart/setInactive"}}{{/crossLink}}),
 *     and hover. The attributes that can be toggled correspond
 *     to the underlying svg type used to render the bar. For example, to modify the the active/inactive bar states,
 *     but not do anything on hover this attribute would be
 *     `{ "active" : { "fill" : "blue" }, "inactive" : { "fill" : "red" } }`. The values for the attributes can also be functions
 *     to compute the values. The function takes 2 parameters - the current data for the bar and its index.</li>
 *     <li>tickFormat (optional) - The format of the tick labels on the x-axis. Use the formatting specified by d3 at
 *     <a href="https://github.com/mbostock/d3/wiki/API-Reference">D3 API reference</a>. The actual d3 format object is
 *     required, not just the string to format it, such as `d3.format('04d')`. The type of formatting used
 *     will vary based on the axis values. If not specified, a preconfigured default value will be used.</li>
 *     <li>tickValues (optional) - A list of tick values to show on the chart. If not specified, all bars will be labeled</li>
 *     <li>categories (optional) - A list of values to use as the x-axis categories (bins). This can also be a function
 *     that takes 1 parameter (the data) and will compute the categories. If not specified, all unique values from the
 *     x-attribute will used as the category values</li>
 *     <li>init (optional) - An optional method for the bar chart to invoke before aggregating the data, but after setting
 *     up the x/y attributes. This allows callers to use the {{#crossLink "charts.BarChart/categoryForItem"}}{{/crossLink}})
 *     method to perform any preprocessing. This is useful because the bar chart will take the appropriate action to
 *     resolve the x attribute, which can be a string or a function.
 *     The init method is called with a single parameter containing the options passed into the bar chart.</li>

 * </ul>
 *
 * @constructor
 *
 * @example
 *    var data = [
 *    { "country": "US", "events": 9},
 *    { "country": "Japan", "events": 8},
 *    { "country": "China", "events": 2},
 *    { "country": "Japan", "events": 3},
 *    { "country": "US", "events": 1},
 *    { "country": "Canada", "events": 7}
 *    ];
 *    var opts = { "data": data, "x": "country", "y" : "events"};
 *    var barchart = new charts.BarChart('#chart', opts).draw();
 *
 */
charts.BarChart = function (chartSelector, opts) {
    opts = opts || {};
    this.chartSelector_ = chartSelector;

    if(!opts.responsive) {
        this.userSetWidth_ = opts.width;
        this.userSetHeight_ = opts.height;
    }

    this.xAttribute_ = opts.x;
    this.xLabel_ = opts.xLabel || this.determineXLabel_();
    this.yAttribute_ = opts.y;
    this.yLabel_ = opts.yLabel || this.determineYLabel_();
    this.margin = $.extend({}, charts.BarChart.DEFAULT_MARGIN_, opts.margin || {});

    if (opts.init) {
        opts.init.call(this, opts);
    }

    // tick formatting/values may be undefined in which case d3's default will be used
    this.tickFormat_ = opts.tickFormat;
    this.tickValues_ = this.computeTickValues_(opts.tickValues);
    this.categories = this.createCategories_(opts.categories ? opts.categories : this.createCategoriesFromUniqueValues_, opts.data);
    this.data_ = this.aggregateData_(opts.data);

    this.preparePropertiesForDrawing_();
    this.style_ = $.extend({}, charts.BarChart.DEFAULT_STYLE_, opts.style);

    if(opts.responsive) {
        this.handleResponsive_();
    }
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

// d3 maps keys as strings but this prevents us from tying it back to the original data, since the original data
// may have different types
charts.BarChart.NUMERIC_KEY_ = 'numeric';
charts.BarChart.DATE_KEY_ = 'date';
charts.BarChart.BOOLEAN_KEY_ = 'boolean';
charts.BarChart.STRING_KEY_ = 'string';

/**
 * Gets the label for the category (bin on the x-axis) for this item.
 * @param {Object} item
 * @return {Object} The x-value for this item
 * @method categoryForItem
 * @protected
 */
charts.BarChart.prototype.categoryForItem = function (item) {
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

charts.BarChart.prototype.determineYLabel_ = function () {
    return this.yAttribute_ ? this.yAttribute_ : "Count";
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

charts.BarChart.prototype.createCategoriesFromUniqueValues_ = function (data) {
    var me = this;
    return _.chain(data).map(function (item) {
        return me.categoryForItem(item);
    }).unique().filter(function (item) {
            return !_.isNull(item) && !_.isUndefined(item);
        }).sort(charts.BarChart.sortComparator_).value();
};

charts.BarChart.sortComparator_ = function (a, b) {
    if (a instanceof Date && b instanceof Date) {
        return charts.BarChart.compareValues_(a.getTime(), b.getTime());
    }
    return charts.BarChart.compareValues_(a, b);
};

charts.BarChart.compareValues_ = function (a, b) {
    if (a < b) {
        return -1;
    }
    if (a > b) {
        return 1;
    }
    return 0;
};

charts.BarChart.prototype.createXScale_ = function () {
    return d3.scale.ordinal()
        .domain(this.categories)
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
    if (this.categories.length > 0) {
        return this.x.rangeBand() * this.categories.length;
    }
    return this.width;
};

charts.BarChart.prototype.createXAxis_ = function () {
    var xAxis = d3.svg.axis()
        .scale(this.x)
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
        .scale(this.y)
        .orient('left')
        .tickFormat(charts.BarChart.createYAxisTickFormat_())
        .tickValues(this.y.domain());
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

    this.preparePropertiesForDrawing_({});
    $(this.chartSelector_).empty();
    var chart = this.drawChartSVG_();
    this.bindData_(chart);
    this.drawXAxis_(chart);
    this.drawYAxis_(chart);
    return this;
};

charts.BarChart.prototype.preparePropertiesForDrawing_ = function(){

    this.width = this.determineWidth_(this.chartSelector_);
    this.height = this.determineHeight_(this.chartSelector_);
    this.setMarginsBasedOnTicks_();
    this.x = this.createXScale_();
    // set the width to be as close to the user specified size (but not larger) so the bars divide evenly into
    // the plot area
    this.plotWidth = this.computePlotWidth_();
    this.x.rangeRoundBands([0, this.plotWidth]);
    this.y = this.createYScale_();
    this.xAxis_ = this.createXAxis_();
    this.yAxis_ = this.createYAxis_();
};

charts.BarChart.prototype.drawChartSVG_ = function () {
    var chart = d3.select(this.chartSelector_)
        .append('svg')
        .attr('id', 'plot')
        .attr('width', this.plotWidth + this.hMargin_)
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
            return me.x(d.key);
        })
        .attr('y', function (d) {
            return me.y(d.values);
        })
        .attr('width', this.x.rangeBand())
        .attr('height', function (d) {
            return me.height - me.vMargin_ - me.y(d.values);
        })
        // using the same color for the border of the bars as the svg background gives separation for adjacent bars
        .attr('stroke', $('#plot').css('background-color'))
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

/**
 * Sets all data to the inactive state that matches the specified predicate. All other data is marked as active.
 * @param {Function} predicate A function that takes an item as a parameter and returns `true` if it should be inactive,
 * `false` if it should be active
 * @method setInactive
 */
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
    var periodStartPixels = this.x(item.key);
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
    var yAttributeHtml = $('<div/>').html(this.yLabel_ + ': ' + item.values);
    tooltip.append(xAttributeHtml);
    tooltip.append(yAttributeHtml);
    return tooltip;
};

charts.BarChart.prototype.centerTooltip_ = function (tooltip, data, periodStartPixels) {
    var centerPointX = periodStartPixels + this.x.rangeBand() / 2;
    var centerPointY = this.y(data.values);
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
    var axis = chart.append('g')
        .attr('class', 'x axis')
        .attr('transform', 'translate(0,' + (this.height - this.vMargin_) + ')')
        .call(this.xAxis_);
    return axis;
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
    var aggregated = this.rollupDataByCategory_(data);
    return this.removeDataWithNoMatchingCategory_(aggregated);
};

/**
 * Takes the raw data and aggregates it by category. This is one step in the data aggregation process.
 * @param data
 * @method rollupDataByCategory_
 * @private
 */
charts.BarChart.prototype.rollupDataByCategory_ = function (data) {
    var me = this;

    // if the attributes are non-strings, they must be converted because d3 rolls them up as strings, so
    // check for those cases
    var keyTypes;

    var aggregated = d3.nest().key(function (d) {
        var category = me.categoryForItem(d);
        if (keyTypes !== charts.BarChart.STRING_KEY_) {
            var keyType = charts.BarChart.keyType_(category);
            // the first time we see a value, set that as the key type
            if (!keyTypes) {
                keyTypes = keyType;
            }
            // if the key type has changed across values, just treat everything as strings
            else if (keyType !== keyTypes) {
                keyTypes = charts.BarChart.STRING_KEY_;
            }
            // d3 will convert the date to a string, which loses any milliseconds. so convert it to a time. it will get
            // converted back after the rollup is done
            if (category instanceof Date) {
                category = category.getTime();
            }
        }
        return category;
    }).rollup(function (d) {
            return d3.sum(d, function (el) {
                return me.yAttribute_ ? el[me.yAttribute_] : 1;
            });
        }).entries(data);

    return charts.BarChart.transformByKeyTypes_(aggregated, keyTypes);
};

charts.BarChart.keyType_ = function (value) {
    if (_.isNumber(value)) {
        return charts.BarChart.NUMERIC_KEY_;
    }

    if (_.isDate(value)) {
        return charts.BarChart.DATE_KEY_;
    }

    if (_.isBoolean(value)) {
        return charts.BarChart.BOOLEAN_KEY_;
    }

    // treat everything else as strings. if the user passes an object, results will be unpredictable
    return charts.BarChart.STRING_KEY_;

};

/**
 * d3 stores all keys as strings in the aggregated data. this converts them to the original data type
 * @param aggregatedData
 * @param keyTypes
 * @return {Object} The original data with the keys transformed
 * @private
 * @method transformByKeyTypes_
 */
charts.BarChart.transformByKeyTypes_ = function (aggregatedData, keyTypes) {
    if (keyTypes === charts.BarChart.DATE_KEY_) {
        return charts.BarChart.mapKeysToDates_(aggregatedData);
    }

    if (keyTypes === charts.BarChart.NUMERIC_KEY_) {
        return charts.BarChart.mapKeysToNumbers_(aggregatedData);
    }

    if (keyTypes === charts.BarChart.BOOLEAN_KEY_) {
        return charts.BarChart.mapKeysToBooleans_(aggregatedData);
    }

    return aggregatedData;
};


charts.BarChart.mapKeysToDates_ = function (aggregatedData) {
    return aggregatedData.map(function (d) {
        d.key = new Date(+d.key);
        return d;
    });
};

charts.BarChart.mapKeysToNumbers_ = function (aggregatedData) {
    return aggregatedData.map(function (d) {
        d.key = +d.key;
        return d;
    });
};

charts.BarChart.mapKeysToBooleans_ = function (aggregatedData) {
    return aggregatedData.map(function (d) {
        d.key = (d.key.toLowerCase() === 'true');
        return d;
    });
};

charts.BarChart.prototype.setMarginsBasedOnTicks_ = function(){
    this.hMargin_ = this.margin.left + this.margin.right;
    this.vMargin_ = this.margin.top + this.margin.bottom;
};

/**
 * Removes any data from the aggregate for which there is a key that has no corresponding category. This can
 * happen if the categories are set explicitly rather than pulling them from the data values
 * @param aggregatedData
 * @private
 * @method removeDataWithNoMatchingCategory_
 */
charts.BarChart.prototype.removeDataWithNoMatchingCategory_ = function (aggregatedData) {
    var me = this;
    return _.reject(aggregatedData, function (item) {
        var key = item.key;

        return _.isUndefined(_.find(me.categories, function (category) {
            // dates won't compare with === since they are different object, so compare using the time values
            if (key instanceof Date && category instanceof Date) {
                return category.getTime() === key.getTime();
            }
            return category === key;
        }));
    });
};

charts.BarChart.prototype.determineWidth_ = function (chartSelector) {
    if(this.userSetWidth_) {
        return this.userSetWidth_;
    }
    else if ($(chartSelector).width() !== 0){
        return $(chartSelector).width();
    }
    return charts.BarChart.DEFAULT_WIDTH_;
};

charts.BarChart.prototype.determineHeight_ = function (chartSelector) {
    if(this.userSetHeight_) {
        return this.userSetHeight_;
    }
    else if ($(chartSelector).height() !== 0){
        return $(chartSelector).height();
    }
    return charts.BarChart.DEFAULT_HEIGHT_;
};

charts.BarChart.prototype.handleResponsive_ = function(){
    var me = this;
    function drawChart(){
        $(me.chartSelector_).empty();
        me.draw();
    }

    $(window).resize(_.debounce(drawChart, 50));

};