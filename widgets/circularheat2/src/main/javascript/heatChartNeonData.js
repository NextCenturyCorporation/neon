
var HeatChartNeonData = (function () {

	/**
	 * Constructs a Neon data source for which data can be retrieved
	 * 
	 */
	var data = function(time) {

    	initialize();

    	/**
    	 * Callback to call if new data is recieved from backend server.
    	 */
    	var updateDataCallback;

    	/**
    	  * Callback to call if error occurs in asynchronous processing
    	  */
    	var errorCallback;

	    function initialize() {

	        neon.query.SERVER_URL = "https://localhost:9443/neon"/* TODO: $("#neon-server").val()*/;

	        neon.eventing.messaging.registerForNeonEvents({
	            activeDatasetChanged: onDatasetChanged,
	            filtersChanged: onFiltersChanged
	        });
	    }

		function onFiltersChanged(message) {
	        alert("Filters changed");
	    }

	    function onDatasetChanged(message) {
	    	alert("Dataset changed");
	    	fetch();
	        // databaseName = message.database;
	        // tableName = message.table;
	        // neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
	        //     filterKey = filterResponse;
	        // });
	        // neon.query.getFieldNames(databaseName, tableName, neon.widget.CIRCULAR_HEAT, populateFromColumns);
	    }



	    function translateData(queryResults) {
	        var rawData = queryResults.data;

	        var chunks = [];

	        _.each(rawData, function (element) {
	            chunks[(element.day-1) * 12 + element.month] = {
	            	title: 'Hi',
	            	value: element.count
	            }
	        });

	        // Any chunks that were left undefined need to be defined as 0 counts.
	        for(var i=0; i<12*31; ++i) {
	        	if (!chunks[i]) {
	        		chunks[i] = {
	        			title: '',
	        			value: 0
	        		};
	        	}
	        }
	        return chunks;
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

			    var clientId = neon.eventing.messaging.getInstanceId();
			    var stateObject = this._GET(null, null, updateDataCallback, errorCallback);
			    if (stateObject) {
		        	neon.query.saveState(clientId, stateObject);
		        }
			},

			/**
			 * Define the callback to use when the charts data is updated asynchronously.  Callback should update the chart with the updated data.
			 * @param {function} callback callback to call.  Will pass array of time chunk data as first argument.
			 */
			setUpdateCallback: function(callback) {
	        	// Need a function that translates Neon data into time chunks which is what heat map is expecting.
	        	updateDataCallback = function(queryResults) {
        			var chunks = translateData(queryResults);
		        	if (typeof callback === 'function') {
        				callback(chunks);
        			}
	        	};
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
		        var dateField = 'time';
		        var mode = 'year';
		        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, dateField, 'month');
		        var groupByDayClause = new neon.query.GroupByFunctionClause(neon.query.DAY, dateField, 'day');

		        var databaseName = "sampleDB";
		        var tableName = "sampleTable";
		        var query = new neon.query.Query()
		            .selectFrom(databaseName, tableName)
		            .groupBy(groupByMonthClause, groupByDayClause)
		            .where(dateField, '!=', null)
		            .aggregate(neon.query.COUNT, null, 'count');

		        var stateObject /* TODO: = buildStateObject(dateField, query)*/;

		        neon.query.executeQuery(query, successCallback, errorCallback);
		        return stateObject;
		    }

		}

	};

	return data;

}());