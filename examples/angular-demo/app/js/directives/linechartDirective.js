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
		$scope.chart = undefined;

		var COUNT_FIELD_NAME = 'Count';

		var initialize = function() {

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged
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

			// if there is no active connection, try to make one.
			connectionService.connectToDataset(message.datastore, message.hostname, message.database);

			// Pull data.
			var connection = connectionService.getActiveConnection();
			if (connection) {
				$scope.queryForData();
			}
		};

		var query = function(comparator, comparisionValue, callback) {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "line-x-axis");
				xAxis = xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
				yAxis = yAxis.mapping;

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
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "line-x-axis");
				xAxis = xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
				yAxis = yAxis.mapping;

			query('>', 0, function(posResults) {
				//this prevents an error in older mongo caused when the xAxis value is invalid as it is not
				//included as a key in the response
				for(var i = 0; i < posResults.data.length; i++) {
					if(typeof(posResults.data[i][xAxis]) === 'undefined') {
						posResults.data[i][xAxis] = null;
					}
				};

				query('<', 0, function(negResults) {
					for(var i = 0; i < negResults.data.length; i++) {
						if(typeof(negResults.data[i][xAxis]) === 'undefined') {
							negResults.data[i][xAxis] = null;
						}
					};

					var minDate, maxDate;
					var posRange, negRange;

					if(posResults.data.length > 0 && negResults.data.length > 0) {
						posRange = d3.extent(posResults.data, function(d) { return new Date(d[xAxis])});
						negRange = d3.extent(negResults.data, function(d) { return new Date(d[xAxis])});

						minDate = new Date(Math.min(posRange[0], negRange[0]));
						maxDate = new Date(Math.max(posRange[1], negRange[1]));
					} else if(posResults.data.length > 0) {
						posRange = d3.extent(posResults.data, function(d) { return new Date(d[xAxis])});
						minDate = posRange[0];
						maxDate = posRange[1];
					} else if(negResults.data.length > 0) {
						negRange = d3.extent(negResults.data, function(d) { return new Date(d[xAxis])});
						minDate = negRange[0];
						maxDate = negRange[1];
					} else {
						minDate = new Date();//new Date().getTime() - (1000 * 60 * 60 * 24));
						maxDate = new Date();
					}

					posResults = zeroPadData(posResults, xAxis, yAxis, minDate, maxDate);
					negResults = zeroPadData(negResults, xAxis, yAxis, minDate, maxDate);

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

		var zeroPadData = function(data, xField, yField, minDate, maxDate) {
			data = data.data;

			var start = zeroOutDate(minDate);
			var end = zeroOutDate(maxDate);

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

			return resultData;
		}

		var drawChart = function() {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "line-x-axis");
			xAxis = xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
			yAxis = yAxis.mapping;
			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			}

			var opts = {"x": xAxis, "y": yAxis, responsive: true};

			// Destroy the old chart and rebuild it.
			if ($scope.chart) {
				$scope.chart.destroy();
			}
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
