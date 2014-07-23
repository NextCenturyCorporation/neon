/*
 * Copyright 2013 Next Century Corporation
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

charts.LineChart = function (rootElement, selector, opts) {
	opts = opts || {};
	this.chartSelector = selector;
	this.element = d3.select(rootElement).select(selector);

	this.xAttribute = opts.x;
	this.yAttribute = opts.y;
	this.margin = $.extend({}, charts.LineChart.DEFAULT_MARGIN, opts.margin || {});

	this.hiddenSeries = [];

	this.colors = [];
	this.colorRange = [
        '#39b54a',
        '#C23333',
        '#3662CC',
        "#ff7f0e",
        "#9467bd",
        "#8c564b",
        "#e377c2",
        "#7f7f7f",
        "#bcbd22",
        "#17becf",
        "#98df8a",
        "#ff9896",
        "#aec7e8",
        "#ffbb78",
        "#c5b0d5",
        "#c49c94",
        "#f7b6d2",
        "#c7c7c7",
        "#dbdb8d",
        "#9edae5"
    ];
    this.colorScale = d3.scale.ordinal().range(this.colorRange);

	this.categories = [];

	if (opts.responsive) {
		this.redrawOnResize();
	}
	return this;
};

charts.LineChart.DEFAULT_HEIGHT = 300;
charts.LineChart.DEFAULT_WIDTH = 600;
charts.LineChart.DEFAULT_MARGIN = {top: 20, bottom: 20, left: 0, right: 0};
charts.LineChart.DEFAULT_STYLE = {};


charts.LineChart.prototype.determineWidth = function (element) {
	if (this.userSetWidth) {
		return this.userSetWidth;
	}
	else if ($(element[0]).width() !== 0) {
		return $(element[0]).width();
	}
	return charts.LineChart.DEFAULT_WIDTH;
};

charts.LineChart.prototype.determineHeight = function (element) {
	if (this.userSetHeight) {
		return this.userSetHeight;
	}
	else if ($(element[0]).height() !== 0) {
		return $(element[0]).height();
	}
	return charts.LineChart.DEFAULT_HEIGHT;
};

charts.LineChart.prototype.categoryForItem = function (item) {
	var me = this;
	if (typeof this.xAttribute === 'function') {
		return this.xAttribute.call(this, item);
	}

	return item[this.xAttribute];
};

charts.LineChart.prototype.createCategories = function (data) {
	var me = this;
	return _.chain(data)
		.map(function (item) {
			return me.categoryForItem(item);
		})
		.unique()
		.filter(function (item) {
			return !_.isNull(item) && !_.isUndefined(item);
		})
		.sort(charts.LineChart.sortComparator)
		.value();
};

charts.LineChart.sortComparator = function (a, b) {
	if (a instanceof Date && b instanceof Date) {
		return charts.LineChart.compareValues(a.getTime(), b.getTime());
	}
	return charts.LineChart.compareValues(a, b);
};

charts.LineChart.compareValues = function (a, b) {
	if (a < b) {
		return -1;
	}
	if (a > b) {
		return 1;
	}
	return 0;
};

charts.LineChart.prototype.drawChart = function() {
	var me = this;

	$(this.element[0]).empty();

	me.height = me.determineHeight(me.element);
	me.width = me.determineWidth(me.element);

	me.svg = me.element.append("svg")
		.attr("width", me.width)
		.attr("height", me.height)
	.append("g")
		.attr("transform", "translate(" + me.margin.left + "," + me.margin.top + ")");
};

charts.LineChart.prototype.calculateColor = function (series, total) {

    var color = this.colorScale(series);
    var hidden = this.hiddenSeries.indexOf(series) >= 0 ? true : false;
    var index = -1;

    for (var i = this.colors.length - 1; i > -1; i--) {
    	if (this.colors[i].series === series)
        	index = i;
	}

    // store the color in the registry so we know the color/series mappings
    if(index >= 0){
    	this.colors[index].color = color;
    	this.colors[index].total = total;
    	this.colors[index].hidden = hidden;
    }else
    	this.colors.push({color: color, series: series, total: total, hidden: hidden});

    return color
};

charts.LineChart.prototype.getColorMappings = function () {
    var me = this;

    // convert to an array that is in alphabetical order for consistent iteration order
    // var sortedColors = [];
    // for (key in this.colors) {
    //     var color = me.colors[key];
    //     sortedColors.push({ 'color': color, 'series': key});
    // }

    return me.colors;
};

charts.LineChart.prototype.drawLine = function(opts) {
	var me = this;

	if(!($.isArray(opts))) {
		opts = [opts];
	}

	me.data = opts;

	var fullDataSet = [];
	//get list of all data
	for(var i = 0; i < opts.length; i++) {
		this.calculateColor(opts[i].series, opts[i].total);
		if(this.hiddenSeries.indexOf(opts[i].series) == -1)
			fullDataSet = fullDataSet.concat(opts[i].data);
	}

	me.x = d3.time.scale.utc()
	.range([0, (me.width - (me.margin.left + me.margin.right))],.25);

	var extent = d3.extent(fullDataSet.map(function (d) {
		return d[me.xAttribute];
	}));

	me.x.domain(d3.extent(fullDataSet, function(d) { return d[me.xAttribute]; }));

	var xAxis = d3.svg.axis()
		.scale(me.x)
		.orient("bottom")
		.ticks(Math.round(me.width/100));

	var xAxisElement = me.svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + (me.height - (me.margin.top + me.margin.bottom)) + ")")
		.call(xAxis);

	me.y = d3.scale.linear().range([(me.height - (me.margin.top + me.margin.bottom)), 0]);

	var yAxis = d3.svg.axis()
		.scale(me.y)
		.orient("right")
		.ticks(3);

	// Use lowest value or 0 for Y-axis domain, whichever is less (e.g. if negative)
	var minY = d3.min(fullDataSet, function(d) { return d[me.yAttribute]; });
	minY = minY < 0 ? minY : 0;
	me.y.domain([minY, d3.max(fullDataSet, function(d) { return d[me.yAttribute]; })]);

	var gridLines = me.svg.append("g").attr("class", "gridLines");

	gridLines.selectAll("line.horizontalGrid").data(me.y.ticks(3)).enter()
	    .append("line")
	        .attr(
	        {
	            "class":"horizontalGrid",
	            "x1" : me.margin.right,
	            "x2" : me.width,
	            "y1" : function(d){ return me.y(d);},
	            "y2" : function(d){ return me.y(d);}
	        });

	// Hover line. 
	var hoverLineGroup = me.svg.append("g")
		.attr("class", "hover-line");
	var hoverLine = hoverLineGroup
		.append("line")
			.attr("x1", 10).attr("x2", 10) 
			.attr("y1", 0).attr("y2", me.height); 
	var hoverDate = hoverLineGroup.append('text')
	   .attr("class", "hover-text hover-date")
	   .attr('y', me.height+20);

	// Hide hover line by default.
	hoverLineGroup.style("opacity", 1e-6);

	var cls;
	var data;
	var line;
	var hoverSeries = [];
	var hoverCircles = {};
	for(var i = (opts.length-1); i > -1; i--) {
		if(this.hiddenSeries.indexOf(opts[i].series) >= 0) continue;
		cls = (opts[i].series ? " " + opts[i].series : "");
		data = opts[i].data;

		hoverSeries.push(
			hoverLineGroup.append('text')
			   .attr("class", "hover-text")
			   .attr('y', me.height+20)
		);

		var color = this.calculateColor(opts[i].series, opts[i].total);

		me.x.ticks().map(function(bucket) {
			return _.find(data, {date: bucket}) || {date: bucket, value: 0};
		});

		data.forEach(function(d) {
			d.date = d[me.xAttribute];
		});

		data = data.sort(function(a,b) {
			if(a.date < b.date) {
				return -1;
			} else if(a.date === b.date) {
				return 0;
			} else {
				return 1;
			}
		});

		line = d3.svg.line()
		.x(function(d) { return me.x(d.date); })
		.y(function(d) { return me.y(d[me.yAttribute]); });

		me.svg.append("path")
			.datum(data)
			.attr("class", "line" + cls)
			.attr("d", line)
			.attr("stroke", color);

		if(data.length < 40){

			var func = function(d) { return me.x(d.date); };
			if(data.length == 1)
				func = me.width/2;

			// Hide circle if point is a 0
			var isZero = function(d) {
				if(d[me.yAttribute] == 0)
					return 0;
				else
					return 1;
			}
			
			me.svg.selectAll("dot")
	            .data(data)
	          .enter().append("circle")
	            .attr("class", "dot dot-empty")
	      		.attr("fill-opacity", isZero)
	      		.attr("stroke-opacity", isZero)
	            .attr("stroke", color)
	            .attr("r", 4)
	            .attr("cx", func)
	            .attr("cy", function(d) { return me.y(d[me.yAttribute]); });
	    }

	    hoverCircles[opts[i].series] = 
			me.svg.append("circle")
	            .attr("class", "dot dot-hover")
	            .attr("stroke", color)
	            .attr("fill", color)
	            .attr("stroke-opacity", 0)
	            .attr("fill-opacity", 0)
	            .attr("r", 4)
	            .attr("cx", 0)
	            .attr("cy", 0);
	}

	var yAxisElement = me.svg.append("g")
		.attr("class", "y axis")
		.call(yAxis);

	var tick = $('.linechart').find('.x.axis').find('.tick.major').first();
	if(tick.length != 0){
	    var transform = tick.attr('transform');
	    var parts  = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(transform);
	    var firstX = parseInt(parts[1]);
	    var threshold = (tick[0].getBBox().width/2);

	    if(firstX < threshold){
	        tick.find('text').css('text-anchor', 'start');
	    }

	   	tick = $('.linechart').find('.x.axis').find('.tick.major').last();
	    transform = tick.attr('transform');
	    parts  = /translate\(\s*([^\s,)]+)[ ,]([^\s,)]+)/.exec(transform);
	   	firstX = parseInt(parts[1]);
	    threshold = me.width - (tick[0].getBBox().width/2);

	    if(firstX > threshold){
	        tick.find('text').css('text-anchor', 'end');
	    }
	}

	// Add mouseover events.
	d3.select('.linechart').on("mouseover", function() { 
		//console.log('mouseover')
	}).on("mousemove", function(event) {
		if (opts && opts.length > 0) {
			var mouse_x = d3.mouse(this)[0];
			var mouse_y = d3.mouse(this)[1];
			var graph_y = me.y.invert(mouse_y);
			var graph_x = me.x.invert(mouse_x);
			var format = d3.time.format.utc('%e %B %Y');
			var numFormat = d3.format("0,000.00");
			var html = '';

			if(opts[0].data.length > 1){
				var bisect = d3.bisector(function(d) { return d[me.xAttribute]; }).right;
				var dataIndex = bisect(opts[0].data, graph_x);
				var dataDate = opts[0].data[dataIndex][me.xAttribute];
				var closerDate = dataDate;
				var closerIndex = dataIndex;

				if(dataIndex > 0){
					var dataIndexLeft = (dataIndex-1);
					var dataDateLeft = opts[0].data[dataIndexLeft][me.xAttribute];
					var compare = ((me.x(dataDate) - me.x(dataDateLeft))/2)+me.x(dataDateLeft);
					if(mouse_x < compare){
						closerDate = dataDateLeft;
						closerIndex = dataIndexLeft;
					}
				}
			}else{
				var closerIndex = 0;
				var closerDate = opts[0].data[closerIndex][me.xAttribute];
			}

			html = '<span class="tooltip-date">'+format(closerDate)+'</span>';

			for(var i = 0; i < opts.length; i++) {
				if(me.hiddenSeries.indexOf(opts[i].series) >= 0) continue;
				var color = me.calculateColor(opts[i].series, opts[i].total);
				var xPos = me.x(closerDate);
				if(opts[i].data.length == 1)
					xPos = me.width/2;

				hoverCircles[opts[i].series]
		            .attr("stroke-opacity", 1)
		            .attr("fill-opacity", 1)
		            .attr("cx", xPos)
		            .attr("cy", me.y(opts[i].data[closerIndex].value));

				html += '<span style="color: '+color+'">'+opts[i].series+": "+numFormat(Math.round(opts[i].data[closerIndex].value * 100) / 100)+'</span>';
			}

			if(opts[0].data.length == 1)
				hoverLine.attr("x1", me.width/2).attr("x2", me.width/2);
			else
				hoverLine.attr("x1", me.x(closerDate)).attr("x2", me.x(closerDate));

			hoverLineGroup.style("opacity", 1);

			$("#tooltip-container").html(html);
		    $("#tooltip-container").show();
		      
		    d3.select("#tooltip-container")
			    .style("top", (d3.event.pageY)  + "px")
			    .style("left", (d3.event.pageX + 15) + "px");
		}
	}).on("mouseout", function() {
		hoverLineGroup.style("opacity", 1e-6);
		me.svg.selectAll("circle.dot-hover")
			.attr("stroke-opacity", 0)
	        .attr("fill-opacity", 0);
		$("#tooltip-container").hide();
	});
};

charts.LineChart.prototype.toggleSeries = function(series) {
	var index = this.hiddenSeries.indexOf(series);
	var activity = '';
	if(index >= 0){
		this.hiddenSeries.splice(index, 1);
		activity = 'show_plot'
	}else{
		this.hiddenSeries.push(series);
		activity = 'hide_plot';
	}

	if(this.data && this.hiddenSeries.length >= this.data.length){
		this.hiddenSeries.splice(0);
	}

	this.redraw();

	return activity;
};

charts.LineChart.prototype.redraw = function() {
	var me = this;
	me.drawChart();
	if(me.data) {
		me.drawLine(me.data);
	}
};

charts.LineChart.prototype.redrawOnResize = function () {
	var me = this;

	function drawChart() {
		me.redraw();
	}

	// Debounce is needed because browser resizes fire this resize even multiple times.
	// Cache the handler so we can remove it from the window on destroy.
	me.resizeHandler = _.debounce(drawChart, 10);
	$(window).resize(me.resizeHandler);

};

charts.LineChart.prototype.destroy = function() {
	$(window).off('resize', this.resizeHandler);
	$(this.element[0]).empty();
};