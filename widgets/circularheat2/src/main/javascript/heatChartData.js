
var HeatChartData = (function () {

	var _config = {};

	var _time;

	/**
	 * Constructs a data source for which data can be retrieved
	 * 
	 */
	var data = function(config, time) {

		_config = config || HeatChartConfig.get();

		_time = time || new HeatChartTime();

		/**
		 * We've got a sorted set of data from the server.  Restrict it to a specific range.
		 * Note, for data points that have a start time and and end time, this restricts based on 
		 * start time.
		 * @param data a set containing numbers (milliseconds into the epoch) 
		 * @param startTime optional time before which all data points should be dropped
		 * @param endTime optional time after which all data points should be dropped
		 */
		var restrictRange = function(data, startTime, endTime) {

			// Find the part of the dataset that is after the desired start time
			var startIndex = -1;
			if (typeof startTime == 'number') {
				// Note: A binary search may be quicker
				var index = 0;
				while ((startIndex < 0) && (index < data.length)) {
					if (data[index] >= startTime) {
						startIndex = index;
					}
					else {
						++index;
					}
				} 
				if (index == data.length) {
					// All the data occurs before the start time.
					startIndex = data.length;
				}
			}
			else {
				startIndex = 0;
			}

			// Find the part of the desired data set that is before the desired end time
			var endIndex = -1;
			if (typeof endTime == 'number') {
				// Note: A binary search may be quicker
				var index = data.length-1;
				while ((endIndex < 0) && (index >= startIndex)) {
					if (data[index] <= endTime) {
						endIndex = index;
					}
					else {
						--index;
					}
				} 
				if (index < startIndex) {
					// All the data occurs after the end time.
					endIndex = startIndex-1;
				}
			}
			else {
				endIndex = data.length;
			}

			// Now truncate the array to just be the desired range.
			if ((startIndex > 0) || (endIndex < data.length-1)) {
				if (endIndex < startIndex) {
					data = [];
				}
				else {
					data = data.slice(startIndex, endIndex+1);
				}
			}

			return data;
		}

		/**
		 * Groups the data into time chunks.  For a day chart, groups the data into buckets representing every minute in every hour.
		 * @param date {Date} a date to indicate where the chart should center.  Given the mode of the chart, will pick start and end times 
		 *		for the chart such that this date appears on the chart.
		 * @param mode {string} how much time is covered by the chart (e.g. year, month, hour,...)
		 * @param timelist {array} a list of times to chunk up.  times are integers (milliseconds in the epoch).
		 */
		var getTimeChunks = function(date, mode, timeList) {
			var _mode = _time.getMode(mode);

			var numPoints = _mode.columns * _mode.rows;
			var timeChunks = [];
			var rawData = [numPoints];
			var title = [numPoints];

			for (var i = 0; i < numPoints; i++) {
				rawData[i] = 0;
				title[i] = "";
			}

			var chartEnds = _time.computeChartEnds(date, mode);

			// This will map the number of raw feeds for a specific date to the correct heat chart "chunk"

			if (timeList) {


				var time, timeTitle, year, month, day, hour, minutes;

				for (var j = 0; j < timeList.length; j++) {
					time = new Date(parseInt(timeList[j]));

					year = time.getFullYear();
					month = time.getMonth();
					day = time.getDate();
					hour = time.getUTCHours();
					minutes = time.getMinutes();

					if ((time >= chartEnds[0]) && (time <= chartEnds[1])) {

						switch (mode) {

							case "hour":
								var ndx = minutes + (_mode.columns * time.getSeconds());
								timeTitle = time.toString();
								break;
							case "day":
								var ndx = hour + (_mode.columns * minutes);
								timeTitle = (new Date(year, month, day, hour, minutes, 0, 0)).toString();
								break;

							case "week":
								var ndx = time.getDay() + (_mode.columns * hour);
								timeTitle = (new Date(year, month, day, hour, 0, 0, 0)).toString();
								break;

							case "month":
								var ndx = (day - 1) + (_mode.columns * hour);
								timeTitle = (new Date(year, month, day, hour, 0, 0, 0)).toString();
								break;

							case "year":
								var ndx = month + (_mode.columns * (day - 1));
								timeTitle = (new Date(year, month, day, 0, 0, 0, 0)).toString();
								break;

							case "year5":
								var ndx = (year - chartEnds[0].getFullYear()) + (_mode.columns * month);
								timeTitle = (new Date(year, month, 1, 0, 0, 0, 0)).toString();
								break;

						}

						rawData[ndx] += 1;
						title[ndx] = timeTitle;

					}
				}
			}

			for (var k = 0; k < numPoints; k++) {
				timeChunks[k] = {
					title: title[k],
					value: rawData[k]
				};
			}

			return timeChunks;
		}



		return {

			/**
			 * Retrieve the desired data set from the server.
			 * @param options {map} set of modifiers to affect this fetch.  Possible options are:
			 *	feedType - (e.g. alpha-report, assertion, or rawfeed)
			 *  mode - the amount of time the chart is displaying (e.g. year, month, hour ...)
			 *	date - the starting date of the chart (or any date of interest, the time period displayed on the chart will include that date)
			 *	successCallback - the callback to use to pass data from a succesful fetch back to the caller.  The first argument
			 * 		is the data passed as an array of integers (milliseconds into the epoch)
			 *	errorCallback - the callback to use to indicate an unsuccesful fetch.  The first argument is a string describing the error encountered.
			 */
			fetch: function(options) {

				var url = _config.baseUrl;

				if (options.feedType && options.feedType.toLowerCase() === 'alpha-report') {
					url += '/alpha-report/dates';

				} else if (options.feedType && options.feedType.toLowerCase() === 'assertion') {
					url += '/assertion/dates';

				} else {
					url += '/rawfeed/dates';
				}

				var date = (typeof options.date == 'undefined') ? new Date() : new Date(options.date);
				var mode = (typeof options.mode == 'undefined') ? 'year' : options.mode;

				// Once we get the data back, we want to restrict it to the desired range
				var processData = function(data) {
						var chartEnds = _time.computeChartEnds(date, mode);
						data = restrictRange(data, chartEnds[0], chartEnds[1]);
						var chunks = getTimeChunks(date, mode, data);
						if (typeof options.successCallback == 'function') {
							options.successCallback(chunks);					
						}
				}
				this._GET(url, processData, options.errorCallback);
			},

			/**
			 * Makes the AJAX call to the server to retrieve the data.  In the Everest implementation, all data is retrieved in one
			 * lump call.
			 * @param url {string} the ajax URL to hit
			 * @param successCallback {function(object[])} the callback to use to pass data back to the caller asyncrhonously.  the first argument
			 * is the data passed as an array of integers (millisecond into the epoch) 
			 * @param errorCallback {function} the callback to use to report an error.  The first argument is the error message.  
			 */
			_GET: function(url, successCallback, errorCallback) {
				$.ajax({
					type: 'GET',
					url: url,
					dataType: _config.dataType,
					jsonpCallback: _config.jsonpCallback,
					success: successCallback,
					error: errorCallback
				});
			}

		}

	};

	return data;

}());