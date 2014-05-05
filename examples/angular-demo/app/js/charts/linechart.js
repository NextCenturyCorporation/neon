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
	//this.xLabel = opts.xLabel || this.determineXLabel();
	this.yAttribute = opts.y;
	//this.yLabel = opts.yLabel || this.determineYLabel();
	this.margin = $.extend({}, charts.LineChart.DEFAULT_MARGIN, opts.margin || {});

	this.categories = [];

	/*this.viewboxXMin = 0;
	this.viewboxYMin = 0;
	this.viewboxXMax = 618;
	this.viewboxYMax = 270;*/

	//this.style = $.extend({}, charts.LineChart.DEFAULT_STYLE, opts.style);

	/*if (opts.responsive) {
		this.redrawOnResize();
	}*/
	return this;
};

charts.LineChart.DEFAULT_HEIGHT = 300;
charts.LineChart.DEFAULT_WIDTH = 600;
charts.LineChart.DEFAULT_MARGIN = {top: 20, bottom: 30, left: 50, right: 20};
charts.LineChart.DEFAULT_STYLE = {};


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
		.sort(charts.BarChart.sortComparator)
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

	me.height = charts.LineChart.DEFAULT_HEIGHT;
	me.width = charts.LineChart.DEFAULT_WIDTH;

	//me.x = d3.time.scale().range([0, me.width]);

	me.svg = me.element.append("svg")
		.attr("width", me.width + me.margin.left + me.margin.right)
		.attr("height", me.height + me.margin.top + me.margin.bottom)
	.append("g")
		.attr("transform", "translate(" + me.margin.left + "," + me.margin.top + ")");
};

charts.LineChart.prototype.drawLine = function(opts) {
	var me = this;

	if(!($.isArray(opts))) {
		opts = [opts];
	}

	var fullDataSet = [];
	//get list of all data
	for(var i = 0; i < opts.length; i++) {
		fullDataSet = fullDataSet.concat(opts[i].data);
	}

	me.categories = me.createCategories(fullDataSet);

	me.x =  d3.scale.ordinal()
		.domain(me.categories)
		.rangePoints([0, me.width],.25);

	var xAxis = d3.svg.axis()
		.scale(me.x)
		.orient("bottom");

	me.svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + me.height + ")")
		.call(xAxis);

	me.y = d3.scale.linear().range([me.height, 0]);

	var yAxis = d3.svg.axis()
		.scale(me.y)
		.orient("left");

	me.y.domain([0, d3.max(fullDataSet, function(d) { return d[me.yAttribute]; })]);

	me.svg.append("g")
		.attr("class", "y axis")
		.call(yAxis)
	.append("text")
		.attr("transform", "rotate(-90)")
		.attr("y", 6)
		.attr("dy", ".71em")
		.style("text-anchor", "end")
		.text(me.yAttribute);

	var cls;
	var data;
	var line;
	for(var i = 0; i < opts.length; i++) {
		cls = "line" + (opts[i].classString ? " " + opts[i].classString : "");
		data = opts[i].data;

		data = data.sort(function(a,b) {
			if(a[me.xAttribute] < b[me.xAttribute]) {
				return -1;
			} else if(a[me.xAttribute] === b[me.xAttribute]) {
				return 0;
			} else {
				return 1;
			}
		});

		line = d3.svg.line()
		.x(function(d) {
			return me.x(d[me.xAttribute]); })
		.y(function(d) { return me.y(d[me.yAttribute]); });

		me.svg.append("path")
		.datum(data)
		.attr("class", cls)
		.attr("d", line);
	}
};

charts.LineChart.destroy = function(el, selector) {
	var element = d3.select(el).select(selector);

	$(element[0]).empty();
};