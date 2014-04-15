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
		this.config.margin = this.config.margin || {top: 10, right: 0, bottom: 20, left: 40};
		this.config.width = this.config.width  || 800;
		this.config.height = this.config.height || 50;

		return this;
	}

	this.render = function(values)
	{
		var data = DEFAULT_DATA;

		if (values && values.length > 0) {
			data = values;
		}

		var parseDate = d3.time.format("%b %Y").parse;

		var x = d3.time.scale().range([0, this.config.width]),
		    y = d3.scale.linear().range([this.config.height, 0]);

		var xAxis = d3.svg.axis().scale(x).orient("bottom"),
		    yAxis = d3.svg.axis().scale(y).orient("left").ticks(1);

		var brush = d3.svg.brush()
		    .x(x)
		    .on("brushend", brushed);

		var area = d3.svg.area()
		    .interpolate("monotone")
		    .x(function(d) { return x(d.date); })
		    .y0(this.config.height)
		    .y1(function(d) { return y(d.value); });
        
        // var yMin = d3.min(data.map(function(d) { return d.value; }));
        // var yMax = d3.max(data.map(function(d) { return d.value; }));

        x.domain(d3.extent(data.map(function(d) { return d.date; })));
		y.domain([0, d3.max(data.map(function(d) { return d.value; }))]);

        // Clear the old contents by replacing innerhtml with the summary header.
		d3.select(this.element).html('<h3><span class="count count-large text-primary">' 
			+ data.length
			+ '</span> records from February 1, 2014 to March 31, 2014</h3>');

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
		    .attr("transform", "translate(0," + this.config.height + ")")
		    .call(xAxis);

		context.append("g")
			.attr("class", "y axis")
			.call(yAxis);

		context.append("g")
		    .attr("class", "x brush")
		    .call(brush)
		  .selectAll("rect")
		    .attr("y", -6)
		    .attr("height", this.config.height + 7);

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