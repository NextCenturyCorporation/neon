
var HeatChartNeonData = (function () {

	/**
	 * Constructs a Neon data source for which data can be retrieved
	 * 
	 */
	var data = function(time) {

    	initialize();

    	/** Database with time data. */
        // TODO: Don't hard code this
        var databaseName = "sampleDB";

        /** Database table/collection with time data. */
        // TODO: Don't hard code this
        var tableName = "sampleTable";

        /** Column/field with time data */
        // TODO: Don't hard code this
        var dateField = "time"; 

        /** The key to filter that this chart is applying to the global data set */
        var filterKey;

    	/** Object for doing time computations */
		var _time = new HeatChartTime();

    	/** Callback to call if new data is recieved from backend server. */
    	var updateDataCallback;

    	/** Callback to call if error occurs in asynchronous processing */
    	var errorCallback;

	    function initialize() {

	        neon.query.SERVER_URL = "https://localhost:9443/neon"/* TODO: $("#neon-server").val()*/;

	        neon.ready(function() {
		        neon.eventing.messaging.registerForNeonEvents({
		            activeDatasetChanged: onDatasetChanged,
		            filtersChanged: onFiltersChanged
		        });
		        // TODO: Shouldn't be able to do this next line until a dataset announces itself.
	        	neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
            		filterKey = filterResponse;
        		});
	        });
	    }

		function onFiltersChanged(message) {
			// TODO: Handle filter change
	    }

	    function onDatasetChanged(message) {
	    	// TODO: Handle dataset change
	    	fetch();
	        // databaseName = message.database;
	        // tableName = message.table;
	        // neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
	        //     filterKey = filterResponse;
	        // });
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
	            }
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
	     */
	    function filterTimeRange(start, end) {
	        var startClause = neon.query.where(dateField, ">=", start);
    	    var endClause = neon.query.where(dateField, "<=", end);
        	var filterClause = neon.query.and(startClause, endClause);
            var filter = new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);;
            //neon.eventing.publishing.replaceFilter(filterKey, filter);
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
				if (!filterKey) {
					var chartData = this;
					// Neon not setup yet.  Need to wait and reinitiate fetch once neon is setup.
					//neon.ready(function() {
					//	chartData.fetch(options);
					//});
				}
				else {
					// TODO: Filter to specific date range
					var date = options.date || new Date(2013, 0);
					var mode = options.mode || 'year';
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
			 */
			setUpdateCallback: function(callback) {
	        	updateDataCallback = callback;
			},

			/**
			 * Define the callback to use when an error occurs in asynchronous processing.
			 * @param {function} callback callback to call.  Will pass error message as first argument.
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
			 * Make the Neon calls to retrieve the desired data
			 * @param {Date} date a date to show on the chart.  date in combination with mode determines where the chart starts and ends. All that is
			 *		guaranteed is that date will be somewhere on the visible chart.
			 * @param {string} mode inidication of the scope of the chart (e.g. year, month, hour,...)
			 * @param {function} successCallback method to call when data is retrieved from server.  callback should expect query results as the 
			 * first argument
			 * @param {function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
			 * @return neon state object to be used to save the state
			 */
			_GET: function(date, mode, successCallback, errorCallback) {
	        	// TODO: Filter out data beyond date range
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

		        var query = new neon.query.Query()
		            .selectFrom(databaseName, tableName)
		            .groupBy(columnGrouping, rowGrouping)
		            .where(dateField, '!=', null)
		            .aggregate(neon.query.COUNT, null, 'count');

		        var stateObject /* TODO: = buildStateObject(dateField, query)*/;

		        // Post-processing requires the mode, so we setup a callback that passes the mode along with
		        // the query results.
		        successCallback = function(queryResults) {
        			var chunks = postProcessResults(queryResults, mode, date);
		        	if (typeof updateDataCallback === 'function') {
        				updateDataCallback(chunks);
        			}
		        };

		        neon.query.executeQuery(query, successCallback, errorCallback);
		        return stateObject;
		    }

		}

	};

	return data;

}());