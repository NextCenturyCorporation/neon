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
    this.d3element = d3.select(element);
    this.brushHandler = undefined;
    this.data = DEFAULT_DATA;
    this.primarySeries = false;

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
        this.config.margin = this.config.margin || {top: 12, right: 15, bottom: 20, left: 15};
        this.redrawOnResize();

        return this;
    }

    this.determineWidth = function (element) {
        if (this.config.width) {
            return this.config.width;
        }
        else if ($(element[0]).width() !== 0) {
            return ($(element[0]).width());
        }
        return 1000;
    };

    this.determineHeight = function (element) {
        if (this.config.height) {
            return this.config.height;
        }
        else if ($(element[0]).height() !== 0) {
            return ($(element[0]).height());
        }
        return 40;
    };

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
            if (brush) {
                XDATA.activityLogger.logUserActivity('End temporal filter selection', 'end_temporal_selection',
                    XDATA.activityLogger.WF_GETDATA,
                    brush.extent());

                if (handler) {
                    handler(brush.extent());
                }
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
        var width = parseInt(brush.find('.mask-west').attr('width'));

        // If brush extent has been cleared, reset mask positions
        if (extentWidth == "0" || extentWidth === undefined) {
            brush.find('.mask-west').attr('x', (0 - (width + 50)));
            brush.find('.mask-east').attr('x', width + 50);
        } else {
            // Otherwise, update mask positions to new extent location
            brush.find('.mask-west').attr('x', parseFloat(xPos) - width);
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
        var width = this.determineWidth(this.d3element) - this.config.margin.left - this.config.margin.right;

        var baseHeight = 70;
        $(this.d3element[0]).css("height", (baseHeight * values.length) );
        var height = (this.determineHeight(this.d3element) - (this.config.margin.top) - this.config.margin.bottom);
        var chartHeight = baseHeight - this.config.margin.top - this.config.margin.bottom;

        var fullDataSet = [];
        if (values && values.length > 0) {
            this.data = values;
            // Get list of all data to calculate min/max and domain
            for(var i = 0; i < values.length; i++) {
                fullDataSet = fullDataSet.concat(values[i].data);
            }
        }

        // Date formatters used by the xAxis and summary header.
        var summaryDateFormat = d3.time.format("%B %d, %Y");

        // Setup the axes and their scales.
        var x = d3.time.scale.utc().range([0, width]),
            xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(Math.round(width/100));

        // Save the brush as an instance variable to allow interaction on it by client code.
        this.brush = d3.svg.brush().x(x).on("brush", this.updateMask);
        if (this.brushHandler) {
            this.brush.on("brushstart", function() {
                XDATA.activityLogger.logUserActivity('Begin temporal filter selection', 'start_temporal_selection',
                    XDATA.activityLogger.WF_GETDATA);
            });
            this.brush.on("brushend", wrapBrushHandler(this.brush, this.brushHandler));
        }

        //var heightRange = y.range()[0];
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

        var xMin = d3.min(fullDataSet.map(function (d) {
            return d.date;
        }));
        var xMax = d3.max(fullDataSet.map(function (d) {
            return d.date;
        }));
        // var totalRecords = d3.sum(this.data[0].map(function (d) {
        //     return d.value
        // }));

        x.domain(d3.extent(fullDataSet.map(function (d) {
            return d.date;
        })));

        // Clear the old contents by replacing innerhtml.
        d3.select(this.element).html('');

        // Append our chart graphics
        this.svg = d3.select(this.element)
            .attr("class", "timeline-selector-chart")
            .append("svg");

        this.svg.append("defs").append("clipPath")
            .attr("id", "clip")
            .append("rect")
            .attr("width", width)
            .attr("height", height);

        var context = this.svg.append("g")
            .attr("class", "context")
            .attr("transform", "translate(" + this.config.margin.left + "," + this.config.margin.top + ")");

        // Render a series
        var me = this;
        var seriesPos = 0;
        var createSeries = function(series){
            var container = context.append("g")
                .attr("class", series.name)
                .attr("transform", "translate(0," + ((chartHeight+me.config.margin.top+me.config.margin.bottom)*seriesPos) + ")");

            var y = d3.scale.linear().range([chartHeight, 0]),
            yAxis = d3.svg.axis().scale(y).orient("right").ticks(2);

            // Use lowest value or 0 for Y-axis domain, whichever is less (e.g. if negative)
            var minY = d3.min(series.data.map(function (d) {
                return d.value;
            }));
            minY = minY < 0 ? minY : 0;
            
            y.domain([minY, d3.max(series.data.map(function (d) {
                return d.value;
            }))]);

            var style = 'stroke:'+series.color+';'
            var chartType = '';
            // If type is line, render a line plot
            if(series.type == 'line'){
                chartType = d3.svg.line()
                    .x(function (d) {
                        return x(d.date);
                    })
                    .y(function (d) {
                        return y(d.value);
                    });
            }else{
                // Otherwise, default to area
                style += 'fill:'+series.color+';';
                chartType = d3.svg.area()
                    .x(function (d) {
                        return x(d.date);
                    })
                    .y0(chartHeight)
                    .y1(function (d) {
                        return y(d.value);
                    });
            }

            container.append("path")
                .datum(series.data)
                .attr("class", series.type)
                .attr("d", chartType)
                .attr("style", style);

            container.append("line")
                .attr({
                    "class":"mini-axis",
                    "x1" : 0,
                    "x2" : width,
                    "y1" : y(0),
                    "y2" : y(0)
                });

            charts.push({
                name: series.name,
                color: series.color,
                yAxis: yAxis,
                container: container,
                index: seriesPos                
            });

            seriesPos++;
        }
        
        var charts = [];
        // If set, render primary series first
        if(this.primarySeries)
            createSeries(this.primarySeries);
        // Render all series
        for(var i = 0; i < values.length; i++) {
            if(this.primarySeries && values[i].name == this.primarySeries.name) continue;
            createSeries(values[i]);
        }

        context.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + (height + 2) + ")")
            .call(xAxis);

        var tick = $('.timeline-selector-chart').find('.x.axis').find('.tick.major').first();
        if(tick.length != 0){
            var transform = tick.attr('transform');
            var parts  = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(transform);
            var firstX = parseInt(parts[1]);
            var threshold = (tick[0].getBBox().width/2);

            if(firstX < threshold){
                tick.find('text').css('text-anchor', 'start');
            }

            tick = $('.timeline-selector-chart').find('.x.axis').find('.tick.major').last();
            transform = tick.attr('transform');
            parts  = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(transform);
            firstX = parseInt(parts[1]);
            threshold = width - (tick[0].getBBox().width/2);

            if(firstX > threshold){
                tick.find('text').css('text-anchor', 'end');
            }
        }

        var gBrush = context.append("g")
            .attr("class", "brush");

        gBrush.append("rect")
            .attr("x", width + this.config.margin.right)
            .attr("y", -6)
            .attr("width", width)
            .attr("height", height + 7)
            .attr("class", "mask mask-east");

        gBrush.append("rect")
            .attr("x", (0 - (width + this.config.margin.left)))
            .attr("y", -6)
            .attr("width", width)
            .attr("height", height + 7)
            .attr("class", "mask mask-west");

        gBrush.call(this.brush);

        gBrush.selectAll("rect")
            .attr("y", -6)
            .attr("height", height + 7);

        gBrush.selectAll(".e")
            .append("rect")
            .attr("y", -6)
            .attr("width", 1)
            .attr("height", height + 6)
            .attr("class", "resize-divider");

        gBrush.selectAll(".w")
            .append("rect")
            .attr("x", -1)
            .attr("y", -6)
            .attr("width", 1)
            .attr("height", height + 6)
            .attr("class", "resize-divider");

        gBrush.selectAll(".resize")
            .append("path")
            .attr("d", resizePath);

        for(var i = 0; i < charts.length; i++) {
            context.append("g")
                .attr("class", "y axis series-y")
                .attr("transform", "translate(0," + ((chartHeight+this.config.margin.top+this.config.margin.bottom)*charts[i].index) + ")")
                .call(charts[i].yAxis);

            context.append("text")
                .attr("class","series-title")
                .attr("fill", charts[i].color)
                .attr("transform", "translate(0," + (((chartHeight+this.config.margin.top+this.config.margin.bottom)*charts[i].index)-5) + ")")
                .text(charts[i].name);
        }

    };

    this.updatePrimarySeries = function(series) {
        this.primarySeries = series;
    }

    this.redrawOnResize = function () {
        var me = this;

        function drawChart() {
            var extent = me.brush.extent();
            me.render(me.data);
            me.renderExtent(extent);
        }

        // Debounce is needed because browser resizes fire this resize even multiple times.
        // Cache the handler so we can remove it from the window on destroy.
        me.resizeHandler = _.debounce(drawChart, 10);
        $(window).resize(me.resizeHandler);

    };

    this.renderExtent = function (extent) {
        var brushElement = this.svg.select(".brush");
        brushElement.call(this.brush.extent(extent));
        this.updateMask.apply(brushElement[0][0]);
    }

    // initialization
    return this.configure(configuration);
}