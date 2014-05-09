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

	this.categories = [];

	if (opts.responsive) {
		this.redrawOnResize();
	}
	return this;
};

charts.LineChart.DEFAULT_HEIGHT = 300;
charts.LineChart.DEFAULT_WIDTH = 600;
charts.LineChart.DEFAULT_MARGIN = {top: 20, bottom: 30, left: 35, right: 20};
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

charts.LineChart.prototype.drawLine = function(opts) {
	var me = this;

	if(!($.isArray(opts))) {
		opts = [opts];
	}

	me.data = opts;

	var fullDataSet = [];
	//get list of all data
	for(var i = 0; i < opts.length; i++) {
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
		//.tickFormat(d3.time.format("%b %d, %Y"))
		// .ticks(d3.time.days, 1);

	var xAxisElement = me.svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + (me.height - (me.margin.top + me.margin.bottom)) + ")")
		.call(xAxis);

	// xAxisElement.selectAll("text")
	// .style("text-anchor", "end")
	// .attr("dx", "-.8em")
	// .attr("dy", ".15em")
	// .attr("transform", function(d) {
	// 	return "rotate(-60)";
	// });

	$(this.element[0]).children('svg').height(280 + $(this.element[0]).find('g.x')[0].getBoundingClientRect().height);

	me.y = d3.scale.linear().range([(me.height - (me.margin.top + me.margin.bottom)), 0]);

	var yAxis = d3.svg.axis()
		.scale(me.y)
		.orient("left");

	me.y.domain([0, d3.max(fullDataSet, function(d) { return d[me.yAttribute]; })]);

	me.svg.append("g")
		.attr("class", "y axis")
		.call(yAxis);
	// .append("text")
	// 	.attr("transform", "rotate(-90)")
	// 	.attr("y", 6)
	// 	.attr("dy", ".71em")
	// 	.style("text-anchor", "end")
	// 	.text(me.yAttribute);

	var cls;
	var data;
	var line;
	for(var i = 0; i < opts.length; i++) {
		cls = (opts[i].classString ? " " + opts[i].classString : "");
		data = opts[i].data;

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
		.attr("d", line);

		if(data.length < 40){
			me.svg.selectAll("dot")
	            .data(data)
	          .enter().append("circle")
	            .attr("class", "dot" + cls)
	            .attr("r", 4)
	            .attr("cx", function(d) { return me.x(d.date); })
	            .attr("cy", function(d) { return me.y(d[me.yAttribute]); });
	    }
	}
};

charts.LineChart.prototype.redrawOnResize = function () {
	var me = this;

	function drawChart() {
		me.drawChart();
		if(me.data) {
			me.drawLine(me.data);
		}
	}

	// Debounce is needed because browser resizes fire this resize even multiple times.
	// Cache the handler so we can remove it from the window on destroy.
	me.resizeHandler = _.debounce(drawChart, 10);
	$(window).resize(me.resizeHandler);

};

charts.LineChart.destroy = function(el, selector) {
	var element = d3.select(el).select(selector);
	$(window).off('resize', this.resizeHandler);
	$(element[0]).empty();
};