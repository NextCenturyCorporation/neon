'use strict';
/*
 * Copyright 2014 Next Century Corporation
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
charts = charts || {};
/**
 * This directive adds a timeline chart to the DOM using the D3 library.
 * The timeline will scale its x-axis labels based upon the amount of time covered in
 * the data to be plotted.  The timeline uses an area plot to show the change in values over time
 * on a broad scale.  It is meant for relative analysis and not detailed analysis.  To that end,
 * y-axis labels are not displayed by default.
 *
 * This class is modeled after portions of the <a href="http://bl.ocks.org/mbostock/1667367#index.html">Focus
 * + Context via Brushing</a> D3 JS example.
 *
 * @class charts.TimelineSelectorChart
 * @constructor
 */
charts.TimelineSelectorChart = function (element, configuration) {
    // Create a default data set when we have no records to display.  It defaults to a year from present day.
    var DEFAULT_DATA = [
        {
            date: new Date(Date.now()),
            value: 0
        },
        {
            date: new Date(Date.now() + 31536000000),
            value: 0
        }
    ];

    // Cache our element.
    this.element = element;
    this.brushHandler = undefined;

    var self = this; // for internal d3 functions

    /**
     * Initializes the internal attributes of the chart.  A configuration object can be provided to
     * override defaults.
     * @param {Object} configuration
     * @param {Number} configuration.height
     * @param {Object} configuration.margin Margin overrides for each side
     * @param {Number} configuration.margin.bottom
     * @param {Number} configuration.margin.left
     * @param {Number} configuration.margin.right
     * @param {Number} configuration.margin.top
     * @param {Number} configuration.width
     * @return charts.TimelineSelectorChart
     * @method configure
     */
    this.configure = function (configuration) {
        this.config = configuration || {};
        this.config.margin = this.config.margin || {top: 10, right: 15, bottom: 20, left: 15};
        this.config.width = this.config.width || 1000;
        this.config.height = this.config.height || 40;

        return this;
    }

    /**
     * Since the default brush handlers return no data, this will allow client code to assign a handler to the brush end event.
     * This function wraps that handler and injects the current brush extent into its arguments.
     * @param {d3.svg.brush} brush The brush to query for extents.
     * @param {function} handler A brush handler.  The extent date objects will be passed to the handler as an array in a single argument
     * @return {function}
     * @method wrapBrushHandler
     * @private
     */
    var wrapBrushHandler = function (brush, handler) {
        return function () {
            if (brush && handler) {
                handler(brush.extent());
            }
        }
    };

    /**
     * Adds a brush end handler to the timeline chart.
     * @param {function} handler A brush handler.  The extent date objects will be passed to the handler as an array in a single argument
     * @method addBrushHandler
     */
    this.addBrushHandler = function (handler) {
        if (typeof(handler) === 'function') {
            this.brushHandler = handler;
            if (this.brush) {
                this.brush.on("brushend", wrapBrushHandler(this.brush, handler));
            }
        }
    }

    /**
     * Removes the brush end handler from the timeline's brush.
     * @method removeBrushHandler
     */
    this.removeBrushHandler = function () {
        this.brushHandler = undefined;
        this.brush.on("brushend");
    }

    /**
     * Clears the brush from the timeline.
     * @method clearBrush
     */
    this.clearBrush = function () {
        this.brush.clear();
        d3.select(this.element).select('.brush').call(this.brush);
    }

    /**
     * Updates the positions of the east and west timeline masks for unselected areas.
     * @method updateMask
     */
    this.updateMask = function () {
        var brush = $(this);
        var xPos = brush.find('.extent').attr('x');
        var extentWidth = brush.find('.extent').attr('width');

        // If brush extent has been cleared, reset mask positions
        if (extentWidth == "0" || extentWidth === undefined) {
            brush.find('.mask-west').attr('x', -2050);
            brush.find('.mask-east').attr('x', brush[0].getBoundingClientRect().width);
        } else {
            // Otherwise, update mask positions to new extent location
            brush.find('.mask-west').attr('x', parseFloat(xPos) - 2000);
            brush.find('.mask-east').attr('x', parseFloat(xPos) + parseFloat(extentWidth));
        }
    }

    /**
     * This will re-render the control with the given values.  This is a costly method and calls to it should be minimized
     * where possible.  Also, it is destructive in that the entire chart and associated time selector brush are recreated.
     * Currently, this has the side effect of removing any brush handlers that were previously added.  Handlers should be
     * reattached after this renders.
     * @param {Array} values An array of objects that consiste of a date and value field
     * @param {Date} values.date A date which will make up a value for the x-axis
     * @param {Number} values.value A number which will be plotted on the y-axis
     * @method render
     */
    this.render = function (values) {
        var data = DEFAULT_DATA;

        if (values && values.length > 0) {
            data = values;
        }

        // Date formatters used by the xAxis and summary header.
        var summaryDateFormat = d3.time.format("%B %d, %Y");

        // Setup the axes and their scales.
        var x = d3.time.scale.utc().range([0, this.config.width]),
            y = d3.scale.linear().range([this.config.height, 0]);

        var xAxis = d3.svg.axis().scale(x).orient("bottom"),
            yAxis = d3.svg.axis().scale(y).orient("left").ticks(1);

        // Save the brush as an instance variable to allow interaction on it by client code.
        this.brush = d3.svg.brush().x(x).on("brush", this.updateMask);
        if (this.brushHandler) {
            this.brush.on("brushend", wrapBrushHandler(this.brush, this.brushHandler));
        }

        var area = d3.svg.area()
            .x(function (d) {
                return x(d.date);
            })
            .y0(this.config.height)
            .y1(function (d) {
                return y(d.value);
            });

        var height = y.range()[0];

        function resizePath(d) {
            var e = +(d == "e"),
                x = e ? 1 : -1,
                y = height / 3;
            return "M" + (.5 * x) + "," + y
                + "A6,6 0 0 " + e + " " + (6.5 * x) + "," + (y + 6)
                + "V" + (2 * y - 6)
                + "A6,6 0 0 " + e + " " + (.5 * x) + "," + (2 * y)
                + "Z"
                + "M" + (2.5 * x) + "," + (y + 8)
                + "V" + (2 * y - 8)
                + "M" + (4.5 * x) + "," + (y + 8)
                + "V" + (2 * y - 8);
        }

        var xMin = d3.min(data.map(function (d) {
            return d.date;
        }));
        var xMax = d3.max(data.map(function (d) {
            return d.date;
        }));
        var totalRecords = d3.sum(data.map(function (d) {
            return d.value
        }));

        x.domain(d3.extent(data.map(function (d) {
            return d.date;
        })));
        y.domain([0, d3.max(data.map(function (d) {
            return d.value;
        }))]);

        // Clear the old contents by replacing innerhtml.
        d3.select(this.element).html('');

        // Append our chart graphics
        this.svg = d3.select(this.element)
            .attr("class", "timeline-selector-chart")
            .append("svg")
            .attr("viewBox", "0 0 "
                + (this.config.width + this.config.margin.left + this.config.margin.right) + " "
                + (this.config.height + this.config.margin.bottom + this.config.margin.top));

        this.svg.append("defs").append("clipPath")
            .attr("id", "clip")
            .append("rect")
            .attr("width", this.config.width)
            .attr("height", this.config.height);

        var context = this.svg.append("g")
            .attr("class", "context")
            .attr("transform", "translate(" + this.config.margin.left + "," + this.config.margin.top + ")");

        context.append("path")
            .datum(data)
            .attr("class", "area")
            .attr("d", area);

        context.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + (this.config.height + 2) + ")")
            .call(xAxis);

        var tick = $('.x.axis').find('.tick.major').first();
        var transform = tick.attr('transform');
        var parts  = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(transform);
        var firstX = parts[1];

        if(firstX > (tick.width()/2))
            tick.find('text').css('text-anchor', 'start');

        var gBrush = context.append("g")
            .attr("class", "brush");

        gBrush.append("rect")
            .attr("x", this.config.width)
            .attr("y", -6)
            .attr("width", 2000)
            .attr("height", this.config.height + 7)
            .attr("class", "mask mask-east");

        gBrush.append("rect")
            .attr("x", -2050)
            .attr("y", -6)
            .attr("width", 2000)
            .attr("height", this.config.height + 7)
            .attr("class", "mask mask-west");

        gBrush.call(this.brush);

        gBrush.selectAll("rect")
            .attr("y", -6)
            .attr("height", this.config.height + 7);

        gBrush.selectAll(".e")
            .append("rect")
            .attr("y", -6)
            .attr("width", 1)
            .attr("height", this.config.height + 6)
            .attr("class", "resize-divider");

        gBrush.selectAll(".w")
            .append("rect")
            .attr("x", -1)
            .attr("y", -6)
            .attr("width", 1)
            .attr("height", this.config.height + 6)
            .attr("class", "resize-divider");

        gBrush.selectAll(".resize")
            .append("path")
            .attr("d", resizePath);

    };

    this.renderExtent = function (extent) {
        var brushElement = this.svg.select(".brush");
        brushElement.call(this.brush.extent(extent));
        this.updateMask.apply(brushElement[0][0]);
    }

    // initialization
    return this.configure(configuration);
}