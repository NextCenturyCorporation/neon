charts = charts || {};
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
	var self = this; // for internal d3 functions
    
	this.configure = function(configuration)
	{
		this.config = configuration || {};
		this.config.margin = this.config.margin || {top: 10, right: 5, bottom: 20, left: 5};
		this.config.width = this.config.width  || 800;
		this.config.height = this.config.height || 40;

		return this;
	}

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

		var brush = d3.svg.brush()
			.x(x)
		    .on("brushend", brushed);

		var line = d3.svg.line()
		    .x(function(d) { return x(d.date); })
		    .y(function(d) { return y(d.value); });

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
		    .attr("class", "line")
		    .attr("d", line);

		context.append("path")
		    .datum(data)
		    .attr("class", "area")
		    .attr("d", area);

		context.append("g")
		    .attr("class", "x axis")
		    .attr("transform", "translate(0," + this.config.height + ")")
		    .call(xAxis);

		//context.append("g")
		//	.attr("class", "y axis")
		//	.call(yAxis);

		var gBrush = context.append("g")
			.attr("class", "brush")
			.call(brush);

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