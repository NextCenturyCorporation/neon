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


var HeatChartNeonData = (function () {

	/**
	 * Constructs a Neon data source for which data can be retrieved
	 * @const
	 * 
	 */
	var data = function(time) {

		/** Database with time data. 
		 * @property {string} databaseName 
		 */
		var databaseName;

		/** Database table/collection with time data. 
		 * @property {string} tableName
		 */
		var tableName;

		/** Column/field with time data 
		 * @property {string} dateField
		 */
		var dateField; 

		/** The key to filter that this chart is applying to the global data set 
		 * @property filterKey
		 */
		var filterKey;

		/** Object for doing time computations 
		 * @property {HeatChartTime} _time
		 */
		var _time = new HeatChartTime();

		/** Callback to call if new data is recieved from backend server.
		 * @property {function} updateDataCallback 
		 */
		var updateDataCallback;

		/** Callback to call if error occurs in asynchronous processing 
		 * @property {function} errorCallback
		 */
		var errorCallback;

		/** The date used the last time data was fetched.
		 * @property {Date} lastDate
		 */
		var lastDate;

		/** The mode used the last time data was fetched.
		 * @property {string} lastMode
		 */
		var lastMode;


        var connectionId;

		initialize();

		function initialize() {

			neon.query.SERVER_URL = "https://localhost:9443/neon"/* TODO: $("#neon-server").val()*/;

			neon.ready(function() {
				neon.eventing.messaging.registerForNeonEvents({
					activeDatasetChanged: onDatasetChanged,
                    activeConnectionChanged: onConnectionChanged,
					filtersChanged: onFiltersChanged
				});
			});
		}

        function onConnectionChanged(id){
            connectionId = id;
        }

		function onFiltersChanged(message) {
			// TODO: Handle filter change
			pub._GET(lastDate, lastMode, updateDataCallback, errorCallback);
		}

		function onDatasetChanged(message) {
			filterKey = null;
			databaseName = message.database;
			tableName = message.table;
			neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
				filterKey = filterResponse;
				ready = true;
			});

			// TODO: Handle getting field name from dropdown
			window.setTimeout(function() {
				pub.setDateField('time');
			}, 2000);
			// neon.query.getFieldNames(databaseName, tableName, neon.widget.CIRCULAR_HEAT, populateFromColumns);
		}



		function postProcessResults(queryResults, mode, baseDate) {

			var modeInfo = _time.getMode(mode);

			var rawData = queryResults.data;

			var chunks = [];

			_.each(rawData, function (element) {
				// TODO: Handle more than just 'year' mode
				var chunk;
				var timeTitle;
				switch (mode) {
					case "hour":
						chunk = element.minute + modeInfo.columns * element.second;
						timeTitle = (new Date(baseDate.getFullYear(), baseDate.getMonth(), baseDate.getDate(), 
							baseDate.getHours(), element.minute, element.second, 0)).toString();
						break;
					case "day":
						chunk = element.hour + modeInfo.columns * element.minute;
						timeTitle = (new Date(baseDate.getFullYear(), baseDate.getMonth(), baseDate.getDate(), 
							element.hour, element.minute, 0, 0)).toString();
						break;
					case "week":
						chunk = (element.day - 1) + modeInfo.columns * element.hour;
						timeTitle = (new Date(baseDate.getFullYear(), baseDate.getMonth(), element.day, 
							element.hour, 0, 0, 0)).toString();
						break;
					case "month":
						chunk = (element.day - 1) + modeInfo.columns * element.hour;
						timeTitle = (new Date(baseDate.getFullYear(), baseDate.getMonth(), element.day, 
							element.hour, 0, 0, 0)).toString();
						break;
					case "year":
						chunk = (element.month-1) + modeInfo.columns * (element.day-1);
						timeTitle = (new Date(baseDate.getFullYear(), element.month-1, element.day, 0, 0, 0, 0)).toString();
						break;
					case "year5":
						chunk = (element.year-baseDate.getFullYear()+2) + modeInfo.columns * (element.month-1);
						timeTitle = (new Date(element.year, element.month-1, 1, 0, 0, 0, 0)).toString();
						break;

				}
				chunks[chunk] = {
					title: timeTitle,
					value: element.count
				};
			});

			// Any chunks that were left undefined need to be defined as 0 counts.
			var numChunks = modeInfo.columns * modeInfo.rows;
			for(var i=0; i<numChunks; ++i) {
				if (!chunks[i]) {
					chunks[i] = {
						title: '',
						value: 0
					};
				}
			}
			return chunks;
		}

		/**
		 * Apply a filter to the global data set to restrict data to data that occurrred within the
		 * time of the chart
		 * @param {Date} start the starting time of the chart
		 * @param {Date} end the ending time of the chart
		 * @method filterTimeRange
		 */
		function filterTimeRange(start, end) {
			var startClause = neon.query.where(dateField, ">=", start.toJSON());
			var endClause = neon.query.where(dateField, "<=", end.toJSON());
			var filterClause = neon.query.and(startClause, endClause);
			var filter = new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
			neon.eventing.publishing.replaceFilter(filterKey, filter);
		}

		var pub = {

			/**
			 * Retrieve the desired data set from the server.
			 * @param {map} [options] set of modifiers to affect this fetch.  Possible options are:
			 *	feedType - (e.g. alpha-report, assertion, or rawfeed)
			 *  mode - the amount of time the chart is displaying (e.g. year, month, hour ...)
			 *	date - the starting date of the chart (or any date of interest, the time period displayed on the chart will include that date)
			 *	successCallback - the callback to use to pass data from a succesful fetch back to the caller.  The first argument
			 *		is the data passed as an array of integers (milliseconds into the epoch)
			 *	errorCallback - the callback to use to indicate an unsuccesful fetch.  The first argument is a string describing the error encountered.
			 * @method fetch
			 */
			fetch: function(options) {
				options = options || {};
				var successCallback = options.successCallback || updateDataCallback;
				var failCallback = options.errorCallback || errorCallback;
				var date = options.date || lastDate;
				lastDate = date;
				var mode = options.mode || lastMode;
				lastMode = mode;

				if (filterKey) {
					var endpoints = _time.computeChartEnds(date, mode);
					filterTimeRange(endpoints[0], endpoints[1]);
					var clientId = neon.eventing.messaging.getInstanceId();
					var stateObject = this._GET(date, mode, updateDataCallback, errorCallback);
					if (stateObject) {
						neon.query.saveState(clientId, stateObject);
					}
				}
			},

			/**
			 * Define the callback to use when the charts data is updated asynchronously.  Callback should update the chart with the updated data.
			 * @param {function} callback callback to call.  Will pass array of time chunk data as first argument.
			 * @method setUpdateCallback
			 */
			setUpdateCallback: function(callback) {
				updateDataCallback = callback;
			},

			/**
			 * Define the callback to use when an error occurs in asynchronous processing.
			 * @param {function} callback callback to call.  Will pass error message as first argument.
			 * @method setErrorCallback
			 */
			setErrorCallback: function(callback) {
				// Neon reports errors as three arguments.  Need a function that translates that into one
				// error message and passes that to error callback
				errorCallback = function(xhr, shortMsg, longMsg) {
					if (typeof callback === 'function') {
						callback(shortMsg + ":\n" + longMsg);
					}
				};
			},

			/**
			 * Define the field in the database table that holds the date.  Once set, this will populate the chart.
			 * @param {string} column name of date
			 * @method setDateField
			 */
			setDateField: function(inDateField) {
				dateField = inDateField;
				this._GET(lastDate, lastMode, updateDataCallback, errorCallback);
			},

			/**
			 * Make the Neon calls to retrieve the desired data
			 * @param {Date} date a date to show on the chart.  date in combination with mode determines where the chart starts and ends. All that is
			 *		guaranteed is that date will be somewhere on the visible chart.
			 * @param {string} mode inidication of the scope of the chart (e.g. year, month, hour,...)
			 * @param {function} successCallback method to call when data is retrieved from server.  callback should expect query results as the 
			 * first argument
			 * @param {function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
			 * @return neon state object to be used to save the state
			 * @method _GET
			 * @private
			 */
			_GET: function(date, mode, successCallback, errorCallback) {
				var stateObject = null;
				if (filterKey) {
					var columnGrouping;
					var rowGrouping;
					switch (mode) {
						case 'hour':
							columnGrouping = new neon.query.GroupByFunctionClause(neon.query.MINUTE, dateField, 'minute');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.SECOND, dateField, 'second');
							break;
						case 'day':
							columnGrouping = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'hour');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.MINUTE, dateField, 'minute');
							break;
						case 'week':
							columnGrouping = new neon.query.GroupByFunctionClause('dayOfWeek', dateField, 'day');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'hour');
							break;
						case 'month':
							columnGrouping = new neon.query.GroupByFunctionClause(neon.query.DAY, dateField, 'day');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'hour');
							break;
						case 'year':
							columnGrouping = new neon.query.GroupByFunctionClause(neon.query.MONTH, dateField, 'month');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.DAY, dateField, 'day');
							break;
						case 'year5':
							columnGrouping = new neon.query.GroupByFunctionClause(neon.query.YEAR, dateField, 'year');
							rowGrouping = new neon.query.GroupByFunctionClause(neon.query.MONTH, dateField, 'month');
							break;
					}

					var endpoints = _time.computeChartEnds(date, mode);
					var startClause = neon.query.where(dateField, ">=", endpoints[0].toJSON());
					var endClause = neon.query.where(dateField, "<=", endpoints[1].toJSON());
					var filterClause = neon.query.and(startClause, endClause);

					var query = new neon.query.Query()
						.selectFrom(databaseName, tableName)
						.groupBy(columnGrouping, rowGrouping)
						.where(filterClause)
						.aggregate(neon.query.COUNT, '*', 'count');

					// TODO: Implement state
					// stateObject = buildStateObject(dateField, query);

					// Post-processing requires the mode, so we setup a callback that passes the mode along with
					// the query results.
					successCallback = function(queryResults) {
						var chunks = postProcessResults(queryResults, mode, date);
						if (typeof updateDataCallback === 'function') {
							updateDataCallback(chunks);
						}
					};

					neon.query.executeQuery(connectionId, query, successCallback, errorCallback);
				}
				return stateObject;
			}

		};

		return pub;

	};

	return data;

}());