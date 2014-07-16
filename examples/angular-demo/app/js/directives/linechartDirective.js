'use strict';

/*
 * Copyright 2014 Next Century Corporation
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

/**
 * This directive adds a linechart to the DOM and drives the visualization data from
 * whatever database and table are currently selected in neon.  This directive accomplishes that
 * by using getting a neon connection from a connection service and listening for
 * neon system events (e.g., data tables changed).  On these events, it requeries the active
 * connection for data and updates applies the change to its scope.  The contained
 * barchart will update as a result.
 * @class neonDemo.directives.linechart
 * @constructor
 */
var linechart = angular.module('linechartDirective', []);

linechart.directive('linechart', ['ConnectionService', function(connectionService) {
	var COUNT_FIELD_NAME = 'value';

	var link = function($scope, el, attr) {
		el.addClass('linechartDirective');

		var messenger = new neon.eventing.Messenger();
		$scope.databaseName = '';
		$scope.tableName = '';
		$scope.totalType = 'count';
		$scope.fields = [];
		$scope.chart = undefined;
		$scope.attrX = '';
		$scope.attrY = '';
		$scope.categoryField = '';
		$scope.aggregation = 'count';
		$scope.seriesLimit = 10;

		var COUNT_FIELD_NAME = 'value';

		var initialize = function() {

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged
			});

			$scope.$watch('attrY', function(newValue, oldValue) {
				onFieldChange('attrY', newValue, oldValue);
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('categoryField', function(newValue, oldValue) {
				onFieldChange('categoryField', newValue, oldValue);
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('aggregation', function(newValue, oldValue) {
				onFieldChange('aggregation', newValue, oldValue);
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});

			// Detect if anything in the digest cycle altered our visibility and redraw our chart if necessary.
			// Note, this supports the angular hide method and would need to be augmented to catch
			// any other hiding mechanisms.
			$scope.$watch(function() {
				return $(el).hasClass('ng-hide');
			}, function(hidden) {
				if (!hidden && $scope.chart) {
					$scope.chart.redraw();
				}
			});
		};

		var onFieldChange = function(field, newVal, oldVal) {
			XDATA.activityLogger.logUserActivity('LineChart - user changed a field selection', 'define_axes',
                XDATA.activityLogger.WF_CREATE,
                {
                	"field": field,
                    "to": newVal,
                    "from": oldVal
                });
		};

		/**
		 * Event handler for selection changed events issued over Neon's messaging channels.
		 * @param {Object} message A Neon selection changed message.
		 * @method onSelectionChanged
		 * @private
		 */
		var onSelectionChanged = function(message) {
			XDATA.activityLogger.logSystemActivity('LineChart - received neon selection changed event');
			$scope.queryForData();
		};

		var onFiltersChanged = function(message) {
			XDATA.activityLogger.logSystemActivity('LineChart - received neon filter changed event');
			$scope.queryForData();
		};

		var onDatasetChanged = function(message) {
			XDATA.activityLogger.logSystemActivity('LineChart - received neon dataset changed event');
			$scope.databaseName = message.database;
			$scope.tableName = message.table;

			// if there is no active connection, try to make one.
			connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

			// Pull data.
			var connection = connectionService.getActiveConnection();
			if (connection) {
                connectionService.loadMetadata(function() {
                	connection.getFieldNames($scope.tableName, function(results) {
	                	XDATA.activityLogger.logSystemActivity('LineChart - query for available fields');
						$scope.$apply(function() {
							$scope.fields = results;
							XDATA.activityLogger.logSystemActivity('LineChart - received available fields');
						});
					});
					$scope.attrX = connectionService.getFieldMapping("date");
					$scope.attrY = connectionService.getFieldMapping("y_axis");
					$scope.categoryField = connectionService.getFieldMapping("line_category");
					$scope.aggregation = 'count';
                    $scope.queryForData();
                });
			}
		};

		var query = function(callback) {

			var yearGroupClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, $scope.attrX, 'year');
            var monthGroupClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, $scope.attrX, 'month');
            var dayGroupClause = new neon.query.GroupByFunctionClause(neon.query.DAY, $scope.attrX, 'day');

            var groupByClause = [yearGroupClause, monthGroupClause, dayGroupClause];
            if($scope.categoryField)
            	groupByClause.push($scope.categoryField);

            var query = new neon.query.Query()
				.selectFrom($scope.databaseName, $scope.tableName)
				.where($scope.attrX,'!=', null);

            query.groupBy.apply(query, groupByClause);

			if($scope.aggregation === 'sum') {
				query.aggregate(neon.query.SUM, $scope.attrY, COUNT_FIELD_NAME);
			} else if($scope.aggregation === 'avg') {
				query.aggregate(neon.query.AVG, $scope.attrY, COUNT_FIELD_NAME);
			} else {
				query.aggregate(neon.query.COUNT, '*', COUNT_FIELD_NAME);
			}

			query.aggregate(neon.query.MIN, $scope.attrX, 'date')
				.sortBy('date', neon.query.ASCENDING);

			connectionService.getActiveConnection().executeQuery(query, callback, function() {
				XDATA.activityLogger.logSystemActivity('LineChart - query failed');
			});
		};

		$scope.queryForData = function() {
			XDATA.activityLogger.logSystemActivity('LineChart - query for data');
			query(function(results) {
				//this prevents an error in older mongo caused when the xAxis value is invalid as it is not
				//included as a key in the response
				for(var i = 0; i < results.data.length; i++) {
					if(typeof(results.data[i][$scope.attrX]) === 'undefined') {
						results.data[i][$scope.attrX] = null;
					}
				};

				var minDate, maxDate;
				var range;

				if(results.data.length > 0) {
					range = d3.extent(results.data, function(d) { return new Date(d.date)});
					minDate = range[0];
					maxDate = range[1];
				} else {
					minDate = new Date();
					maxDate = new Date();
				}

				var data = [];
				var series = []
				var zeroedData = zeroPadData(results, minDate, maxDate);

				// Convert results to array
				for (series in zeroedData){
					data.push(zeroedData[series]);
				}

				// Sort by series total
				data.sort(function(a, b){
					if(a.total < b.total) return 1;
					if(a.total > b.total) return -1;
					return 0;
				});

				// Calculate Other series
				var otherTotal = 0;
				var otherData = [];
				if($scope.aggregation != 'avg'){
					for(var i = $scope.seriesLimit; i < data.length; i++) {
						otherTotal += data[i].total;
						for(var d = 0; d < data[i].data.length; d++) {
							if(otherData[d])
								otherData[d].value += data[i].data[d].value;
							else
								otherData[d] = {
									date: data[i].data[d].date,
									value: data[i].data[d].value
								};
						}
					}
				}

				// Trim data to only top results
				data = data.splice(0, $scope.seriesLimit);

				// Add Other series
				if(otherTotal > 0)
					data.push({
						series: "Other",
						total: otherTotal,
						data: otherData
					})

				// Render chart and series lines
				XDATA.activityLogger.logSystemActivity('LineChart - query data received');
				$scope.$apply(function(){
					drawChart();
					drawLine(data);
					XDATA.activityLogger.logSystemActivity('LineChart - query data rendered');
				});
			});
		};

		$scope.toggleSeries = function(series) {
			var activity = $scope.chart.toggleSeries(series);
			XDATA.activityLogger.logUserActivity('LineChart - user toggled series', activity,
                XDATA.activityLogger.WF_CREATE,
                {
                	"plot": series
                });
		};

		var zeroPadData = function(data, minDate, maxDate) {
			data = data.data;

			var start = zeroOutDate(minDate);
			var end = zeroOutDate(maxDate);

			var dayMillis = (1000 * 60 * 60 * 24);
			var numBuckets = Math.ceil(Math.abs(end - start) / dayMillis) + 1;

			var startTime = start.getTime();

			var resultData = {};

			var series = 'Total';
			if($scope.aggregation == 'avg')
				series = 'Average '+$scope.attrY;
			else if($scope.aggregation == 'sum')
				series = $scope.attrY;

			// Scrape data for unique series
			for(var i = 0; i < data.length; i++) {
				if($scope.categoryField)
					series = data[i][$scope.categoryField] != '' ? data[i][$scope.categoryField] : 'Unknown';

				if(!resultData[series]){
					resultData[series] = {
						series: series,
						total: 0,
						data: []
					};
				}
			}

			// Initialize our time buckets.
			for(var i = 0; i < numBuckets; i++) {
				var bucketGraphDate = new Date(startTime + (dayMillis * i));
				for (series in resultData){
					resultData[series].data.push({date: bucketGraphDate, value: 0});
				}
			}

			// Populate series with data
			var indexDate;
			for (i = 0; i < data.length; i++) {
				indexDate = new Date(data[i].date);

				if($scope.categoryField)
					series = data[i][$scope.categoryField] != '' ? data[i][$scope.categoryField] : 'Unknown';

				resultData[series].data[Math.floor(Math.abs(indexDate - start) / dayMillis)].value = data[i].value;
				resultData[series]['total'] += data[i].value;
			}

			return resultData;
		}

		var drawChart = function() {
			var opts = {"x": "date", "y": "value", responsive: true};

			// Destroy the old chart and rebuild it.
			if ($scope.chart) {
				$scope.chart.destroy();
			}
			$scope.chart = new charts.LineChart(el[0], '.linechart', opts);
			$scope.chart.drawChart();
		};

		var drawLine = function(data) {
			$scope.chart.drawLine(data);
			$scope.colorMappings = $scope.chart.getColorMappings();
		};

		/**
		 * Sets the minutes, seconds and millis to 0. If the granularity of the date is day, then the hours are also zeroed
		 * @param date
		 * @returns {Date}
		 */
		var zeroOutDate = function (date) {
			var zeroed = new Date(date);
			zeroed.setUTCMinutes(0);
			zeroed.setUTCSeconds(0);
			zeroed.setUTCMilliseconds(0);
			zeroed.setUTCHours(0);
			return zeroed;
		};

		neon.ready(function () {
			$scope.messenger = new neon.eventing.Messenger();
			initialize();
		});
	};

	return {
		templateUrl: 'partials/linechart.html',
		restrict: 'E',
		scope: {
			colorMappings: '&',
			chartType: '='
		},
		link: link
	};
}]);
