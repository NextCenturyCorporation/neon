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
 * y-axis labels are not 
 * 
 * @class neonDemo.directives.barchart
 * @constructor
 */
charts.TimelineSelectorChart = function (element, configuration)
{
	// Create a default data set when we have no records to display.  It defaults to a year from present day.
    var DEFAULT_DATA = [{
        date: new Date(Date.now()),
        value: 0
    },{
        date: new Date(Date.now() + 31536000000),
        value: 0
    }];

    // Cache our element.
	this.element = element;
	this.brushHandler = undefined;

	var self = this; // for internal d3 functions
    
	this.configure = function(configuration)
	{
		this.config = configuration || {};
		this.config.margin = this.config.margin || {top: 10, right: 5, bottom: 20, left: 5};
		this.config.width = this.config.width  || 800;
		this.config.height = this.config.height || 40;

		return this;
	}

    var wrapBrushHandler = function(brush, handler) {
    	return function() {
	    	if (brush && handler) {
	    		handler(brush.extent());
	    	}
	    }
    };

    this.addBrushHandler = function(handler) {
    	if (typeof(handler) === 'function') {
    		this.brushHandler = handler;
    		if (this.brush) {
    			this.brush.on("brushed", wrapBrushHandler(this.brush, handler));
    		}
    	}
    }

    this.removeBrushHandler = function() {
    	this.brushHandler = undefined;
    	this.brush.on("brushed");
    }

    this.clearBrush = function() {
    	this.brush.clear();
    	d3.select(this.element).select('.brush').call(this.brush);
    }


    /**
     * This will re-render the control with the given values.  This is a costly method and calls to it should be minimized
     * where possible.  Also, it is destructive in that the entire chart and associated time selector brush are recreated.
     * Currently, this has the side effect of removing any brush handlers that were previously added.  Handlers should be
     * reattached after this renders.
     * TODO: Consider caching the handlers to automatically re-attach them after a render.
     */
	this.render = function(values)
	{
		var data = DEFAULT_DATA;

		if (values && values.length > 0) {
			data = values;
		}

		// Date formatters used by the xAxis and summary header.
		var parseDate = d3.time.format("%b %Y").parse;
        var summaryDateFormat = d3.time.format("%B %d, %Y");

        // Setup the axes and their scales.
		var x = d3.time.scale().range([0, this.config.width]),
		    y = d3.scale.linear().range([this.config.height, 0]);

		var xAxis = d3.svg.axis().scale(x).orient("bottom"),
		    yAxis = d3.svg.axis().scale(y).orient("left").ticks(1);

        // Save the brush as an instance variable to allow interaction on it by client code.
		this.brush = d3.svg.brush().x(x);

		if (this.brushHandler) {
		    this.brush.on("brushend", wrapBrushHandler(this.brush, this.brushHandler));
		}

		var area = d3.svg.area()
		    .x(function(d) { return x(d.date); })
		    .y0(this.config.height)
		    .y1(function(d) { return y(d.value); });

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
        
        var xMin = d3.min(data.map(function(d) { return d.date; }));
        var xMax = d3.max(data.map(function(d) { return d.date; }));
        var totalRecords = d3.sum(data.map(function(d) { return d.value }));

        x.domain(d3.extent(data.map(function(d) { return d.date; })));
		y.domain([0, d3.max(data.map(function(d) { return d.value; }))]);

        // Clear the old contents by replacing innerhtml with the summary header.
		d3.select(this.element).html('<h3><span class="count count-large text-primary">' 
			+ totalRecords
			+ '</span> records from '
			+ summaryDateFormat(xMin)
			+ ' to '
			+ summaryDateFormat(xMax)
			+ '</h3>');

        // Append our chart graphics
		var svg = d3.select(this.element)
		    .attr("class", "timeline-selector-chart")
		  .append("svg")
		    .attr("viewBox","0 0 "
		    	+ (this.config.width + this.config.margin.left + this.config.margin.right) + " "
		    	+ (this.config.height + this.config.margin.bottom + this.config.margin.top));

		svg.append("defs").append("clipPath")
		    .attr("id", "clip")
		  .append("rect")
		    .attr("width", this.config.width)
		    .attr("height", this.config.height);

		var context = svg.append("g")
		    .attr("class", "context")
		    .attr("transform", "translate(" + this.config.margin.left + "," + this.config.margin.top + ")");

		context.append("path")
		    .datum(data)
		    .attr("class", "area")
		    .attr("d", area);

		context.append("g")
		    .attr("class", "x axis")
		    .attr("transform", "translate(0," + (this.config.height+2) + ")")
		    .call(xAxis);

		//context.append("g")
		//	.attr("class", "y axis")
		//	.call(yAxis);

		var gBrush = context.append("g")
			.attr("class", "brush")
			.call(this.brush);

        gBrush.selectAll("rect")
		    .attr("y", -6)
		    .attr("height", this.config.height + 7);

        gBrush.selectAll(".resize")
        	.append("path")
        	.attr("d", resizePath);

		function brushed() {
			console.log("WE BRUSHED!");
		  //x.domain(brush.empty() ? x2.domain() : brush.extent());
		  //focus.select(".area").attr("d", area);
		  //focus.select(".x.axis").call(xAxis);
		}

		function type(d) {
		  d.date = parseDate(d.date);
		  d.value = +d.value;
		  return d;
		}
	};

	// initialization
	return this.configure(configuration);
}