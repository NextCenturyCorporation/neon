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
	var COUNT_FIELD_NAME = 'Count';

	var link = function($scope, el, attr) {
		el.addClass('linechartDirective');

		var messenger = new neon.eventing.Messenger();
		$scope.database = '';
		$scope.tableName = '';
		$scope.totalType = /*$scope.totalType ||*/ 'count';
		$scope.fields = [];
		$scope.xAxisSelect = $scope.fields[0] ? $scope.fields[0] : '';

		var COUNT_FIELD_NAME = 'Count';

		var initialize = function() {

			//drawChart();

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged
				//selectionChanged: onSelectionChanged
			});

			$scope.$watch('attrX', function(newValue, oldValue) {
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('attrY', function(newValue, oldValue) {
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('totalType', function(newValue, oldValue) {
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
		};

		/**
		 * Event handler for selection changed events issued over Neon's messaging channels.
		 * @param {Object} message A Neon selection changed message.
		 * @method onSelectionChanged
		 * @private
		 */
		var onSelectionChanged = function(message) {
			$scope.queryForData();
		};

		var onFiltersChanged = function(message) {
			$scope.queryForData();
		};

		var onDatasetChanged = function(message) {
			$scope.databaseName = message.database;
			$scope.tableName = message.table;
		};

		var query = function(comparator, comparisionValue, callback) {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "x-axis");
				xAxis = $scope.attrX || xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
				yAxis = $scope.attrY || yAxis.mapping;

			var query = new neon.query.Query()
				.selectFrom($scope.databaseName, $scope.tableName)
				.where(xAxis,'!=', null)
				.where(yAxis, comparator, comparisionValue)
				//.selectionOnly()
				.groupBy(xAxis);

			var queryType = neon.query.COUNT;

			if(yAxis) {
				query.aggregate(queryType, yAxis, yAxis);
			} else {
				query.aggregate(queryType, '*', COUNT_FIELD_NAME);
			}

			connectionService.getActiveConnection().executeQuery(query, callback);
		};

		$scope.queryForData = function() {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "x-axis");
				xAxis = $scope.attrX || xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
				yAxis = $scope.attrY || yAxis.mapping;

			query('>', 0, function(posResults) {
				posResults = zeroPadData(posResults, xAxis, yAxis);

				query('<', 0, function(negResults) {
					negResults = zeroPadData(negResults, xAxis, yAxis);

					var data = [{
						data: posResults,
						classString: "positiveLine"
					},{
						data: negResults,
						classString: "negativeLine"
					}];

					$scope.$apply(function(){
						drawChart();
						drawLine(data);
					});
				});
			});
		};

		var zeroPadData = function(data, xField, yField) {
			data = data.data;

			var sortedData = data.sort(function(a,b) {
				if(a[xField] < b[xField]) {
					return -1;
				} else if(a[xField] === b[xField]) {
					return 0;
				} else {
					return 1;
				}
			});

			var start = zeroOutDate(sortedData[0][xField]);
			var end = zeroOutDate(sortedData[sortedData.length - 1][xField]);

			var dayMillis = (1000 * 60 * 60 * 24);
			var numBuckets = Math.ceil(Math.abs(end - start) / dayMillis) + 1;

			var startTime = start.getTime();

			// Initialize our time buckets.
			var resultData = [];
			for(var i = 0; i < numBuckets; i++) {
				var bucketGraphDate = new Date(startTime + (dayMillis * i));
				resultData[i] = {};
				resultData[i][xField] = bucketGraphDate;
				resultData[i][yField] = 0;
			}

			var indexDate;

			for (i = 0; i < data.length; i++) {
				indexDate = new Date(data[i][xField]);

				resultData[Math.floor(Math.abs(indexDate - start) / dayMillis)][yField] = data[i][yField];
			}

			console.table(resultData);

			return resultData;
		}

		var drawChart = function() {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "x-axis");
			xAxis = $scope.attrX || xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
			yAxis = $scope.attrY || yAxis.mapping;
			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			}

			var opts = {"x": xAxis, "y": yAxis, responsive: true};
			charts.LineChart.destroy(el[0], '.linechart');
			$scope.chart = new charts.LineChart(el[0], '.linechart', opts);
			$scope.chart.drawChart();
		};

		var drawLine = function(data) {
			$scope.chart.drawLine(data);
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
			attrX: '=',
			attrY: '=',
			totalType: '='
		},
		link: link
	};
}]);
