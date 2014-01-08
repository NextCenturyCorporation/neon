var HeatChartApp = (function () {


	/**
	 * Creates a circular heat chart widget
	 * @const
	 * @param {string} startingMode the units of time to display on the chart (e.g. day, month, year).  For a comprehensive list
	 * refer to the name field of the objects in the _MODES list in heatChartTime.js.
	 * @param {Date} date optional, where to point the chart at.  Default is current time.
	 * @param {HeatChartData} dataInject optional object to retrieve data.  this allows for plugging in of different data sources.
	 * Default is to create a HeatChartData.
	 */
	var app = function(startingMode, date, dataInject) {
		$('#nowButton').click(function() {
			updateNow();
		});

		$('#hourButton').click(function() {
			execute("hour");
		});
		$('#dayButton').click(function() {
			execute("day");
		});
		$('#weekButton').click(function() {
			execute("week");
		});
		$('#monthButton').click(function() {
			execute("month");
		});
		$('#yearButton').click(function() {
			execute("year");
		});
		$('#year5Button').click(function() {
			execute("year5");
		});

		var chartTime = new HeatChartTime(date);
		var chartWidget = new HeatChartWidget();
		var chartData = (dataInject === undefined) ? new HeatChartData() : dataInject;
		chartData.setUpdateCallback(update);
		chartData.setErrorCallback(reportError);


		var CHART_MODE = {};

		var baseDate = date || new Date();

		var chart = {};

		/** 
		 * A callback to call when the chart data is updated.  Useful if containing object wants to react or monitor data.
		 * Callback should expect an array of time chunk data as first argument.
		 * @property {function} updateListener
		 */
		var updateListener;

		execute(startingMode);

		/**
		 * Changes the mode of a given chart.
		 * @method execute
		 * @param mode {string} new mode (e.g. year, day, ...)
		 * @param updateCallback optional callback on what to do once new data is collected.  Default is to call update(), but
		 * if further processing of data must be done before the chart should be updated, this is the extension point to do that.
		 */
		function execute(mode) {
			updateMode(mode);
			updateModeButtons(mode);
			fetch();
		}

		function fetch() {
			chartData.fetch({
				feedType: 'rawfeed',
				date: baseDate,
				mode: CHART_MODE.name,
				successCallback: update,
				errorCallback: reportError
			});
		}

		function update(chunks) {
			d3.select("#chart").selectAll("svg").data([]).exit().remove();
			createHeatChart(chunks);
			if (typeof updateListener === 'function') {
				updateListener(chunks);
			}
		}

		function reportError(errMessage) {
			console.error("An error occurred trying to retrieve the data: " + errMessage);
		}

		function updateNow() {
			baseDate = new Date();
			update();
			updateModeButtons(CHART_MODE.name);
		}

		function updateMode(newMode) {
			CHART_MODE = chartTime.getMode(newMode);
		}

		function updateModeButtons() {

			var baseYear = baseDate.getFullYear();
			var baseMonth = baseDate.getMonth();
			var baseDay = baseDate.getDate();
			var baseDayOfWeek = baseDate.getDay();
			var baseHour = baseDate.getHours();

			d3.select("#hourButton").text('Hour');
			d3.select("#dayButton").text('Day');
			d3.select("#weekButton").text('Week');
			d3.select("#monthButton").text('Month');
			d3.select("#yearButton").text('Year');
			d3.select("#year5Button").text('5-Year');

			var monthLabel = chartTime.getMonthLabel(baseMonth);

			var dayOfWeekLabel = chartTime.getDayOfWeekLabel(baseDayOfWeek);

			var dayLabel = baseDay;
			if (dayLabel < 10) {
				dayLabel = '0' + dayLabel;
			}

			var hourLabel = baseHour;
			if (hourLabel < 10) {
				hourLabel = '0' + hourLabel + '00';
			} else {
				hourLabel = hourLabel + '00';
			}

			switch (CHART_MODE.name) {

				case "hour":
					d3.select("#yearButton").text(baseYear);
					d3.select("#monthButton").text(monthLabel);
					d3.select("#weekButton").text(dayOfWeekLabel);
					d3.select("#dayButton").text(dayLabel);
					d3.select("#hourButton").text(hourLabel);
					break;

				case "day":
					d3.select("#yearButton").text(baseYear);
					d3.select("#monthButton").text(monthLabel);
					d3.select("#weekButton").text(dayOfWeekLabel);
					d3.select("#dayButton").text(dayLabel);
					break;

				case "week":
					d3.select("#yearButton").text(baseYear);
					d3.select("#monthButton").text(monthLabel);
					d3.select("#weekButton").text(dayOfWeekLabel);
					break;

				case "month":
					d3.select("#yearButton").text(baseYear);
					d3.select("#monthButton").text(monthLabel);
					break;

				case "year":
					d3.select("#yearButton").text(baseYear);
					break;

				case "year5":
					d3.select("#yearButton").text(baseYear);
					break;

			}

			d3.select("#baseDate").text("Context Date: " + baseDate.toString());
			chartWidget.publishDateRange(CHART_MODE.name, baseDate);

		}

		function createHeatChart(time_chunks) {
			var me = this;

			var chartWidth = 300;
			try {
				var cw = d3.select("#chart").style('width');
				chartWidth = cw.split('px')[0];
			} catch (err) {
				console.error(err);
			}

			// CHART_MODE.rows + 1 due to the size of the innerRadius
			var segHeight = chartWidth / (CHART_MODE.rows + 1);

			var innerRadius = segHeight < 10 ? 10 : segHeight;

			chart = circularHeatChart()
				.range(["white", CHART_MODE.color])
				.radialLabels(CHART_MODE.rowLabels)
				.segmentLabels(CHART_MODE.columnLabels)
				.segmentHeight(segHeight)
				.numSegments(CHART_MODE.columns)
				.innerRadius(innerRadius);

			chart.accessor(function(d) {
				return d.value;
			});

			d3.select("#chart")
				.selectAll('svg')
				.data([time_chunks])
				.enter()
				.append('svg')
				.call(chart);

			var tooltip = d3.select("body")
				.append("div")
				.style("position", "absolute")
				.style("z-index", "10")
				.style("visibility", "hidden")
				.text("a simple tooltip");

			d3.selectAll("#chart path")
				.on('mouseover', function() {
					var d = d3.select(this).data()[0];
					if (0 !== d.value) {
						return tooltip.style("visibility", "visible").text(d.value + ' added at  ' + d.title);
					}
				})
				.on("mousemove", function(event) {
					return tooltip.style("top", (event.pageY - 10) + "px").style("left", (event.pageX + 10) + "px");
				})
				.on("mouseout", function() {
					return tooltip.style("visibility", "hidden");
				});

			d3.selectAll("#chart svg").on('mouseout', function() {
				//var d = d3.select(this).data()[0];
				d3.select("#info").text('');
			});

			d3.selectAll("#chart path").on('click', function() {
				tooltip.style("visibility", "hidden");
				var d = d3.select(this).data()[0];
				if (0 !== d.value) {
					handleDrillDown(d.title);
				}
			});

		}

		function handleDrillDown(cellDate) {
			baseDate = new Date(cellDate);

			switch (CHART_MODE.name) {
				//Announce Channel: com.nextcentury.everest.heatchart
				//Each time the heat chart is drilled down, announce the new date range
				//that appears in the chart.
				case "hour":
					break;

				case "day":
					execute("hour");
					break;

				case "week":
					execute("day");
					break;

				case "month":
					execute("day");
					break;

				case "year":
					execute("month");
					break;

				case "year5":
					execute("year");
					break;
			}

			chartWidget.publishDateRange(CHART_MODE.name, baseDate);
		}

		return {

			/**
			 * Registers a callback to be called when chart data changes (whether initiated externally or internally).
			 * Useful if containing object wants to react or monitor data.
			 * Can register only one callback.  Subsequent calls will replace callback from previous calls.
			 * Callback should expect first argument to be an array of objects of the form
			 * {
			 *		title: human readable representation the date of this segment of the chart
			 *		value: value to be represented in this segment of the chart
			 * }
			 * @method setUpdateListener
			 */
			setUpdateListener: function(callback) {
				updateListener = callback;
			},

			/**
			 * Resets the chart to display a certain range of the data.  Will query the server for data if necessary.
			 * @param date {integer} start of the chart in milliseconds
			 * @param mode {string} time range to cover
			 * @method setChartRange
			 */
			setChartRange: function(date, mode) {
				if (date) {
					baseDate = new Date(date);
				}

				execute(mode);				
			}
		};

	};

	return app;

}());