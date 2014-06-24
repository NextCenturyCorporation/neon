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
 * This directive adds a barchart to the DOM and drives the visualization data from
 * whatever database and table are currently selected in neon.  This directive accomplishes that
 * by using getting a neon connection from a connection service and listening for
 * neon system events (e.g., data tables changed).  On these events, it requeries the active
 * connection for data and updates applies the change to its scope.  The contained
 * barchart will update as a result.
 * @class neonDemo.directives.barchart
 * @constructor
 */
var barchart = angular.module('barchartDirective', []);

barchart.directive('barchart', ['ConnectionService', '$timeout', function(connectionService, $timeout) {
	var COUNT_FIELD_NAME = 'Count';

	var link = function($scope, el, attr) {
		el.addClass('barchartDirective');

		var messenger = new neon.eventing.Messenger();
		$scope.database = '';
		$scope.tableName = '';
		$scope.barType = $scope.barType || 'count';
		$scope.fields = [];
		$scope.xAxisSelect = $scope.fields[0] ? $scope.fields[0] : '';
        $scope.initializing = false;
        $scope.chart = undefined;

		var COUNT_FIELD_NAME = 'Count';
		var clientId;

		var initialize = function() {

			drawBlankChart();

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged
			});

			$scope.$watch('attrX', function(newValue, oldValue) {
				if(!$scope.initializing && $scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('attrY', function(newValue, oldValue) {
				if(!$scope.initializing && $scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
			$scope.$watch('barType', function(newValue, oldValue) {
				if(!$scope.initializing && $scope.databaseName && $scope.tableName) {
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
					$scope.chart.draw();
				}
			});
		};

		var onFiltersChanged = function(message) {
			XDATA.activityLogger.logSystemActivity('BarChart - received neon filter changed event');
			$scope.queryForData();
		};

		var onDatasetChanged = function(message) {
			XDATA.activityLogger.logSystemActivity('BarChart - received neon dataset changed event');
            $scope.initializing = true;
			$scope.databaseName = message.database;
			$scope.tableName = message.table;

			// if there is no active connection, try to make one.
			connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

			// Pull data.
			var connection = connectionService.getActiveConnection();
            $timeout(function() {
                $scope.initializing = false;
                if (connection) {
                    connectionService.loadMetadata(function() {
                        $scope.queryForData();
                    });
                }
            });

		};

		$scope.queryForData = function() {
			var xAxis = $scope.attrX || connectionService.getFieldMapping("bar_x_axis");
			var yAxis = $scope.attrY || connectionService.getFieldMapping("y_axis")

            if (xAxis === undefined || xAxis === "" || yAxis === undefined || yAxis ==="") {
                drawBlankChart();
                return;
            }

			var query = new neon.query.Query()
				.selectFrom($scope.databaseName, $scope.tableName)
				.where(xAxis,'!=', null)
				.groupBy(xAxis);

			var queryType;
			if($scope.barType === 'count') {
				queryType = neon.query.COUNT;
			} else if($scope.barType === 'sum') {
				queryType = neon.query.SUM;
			} else if($scope.barType === 'avg') {
				queryType = neon.query.AVG;
			}

			if(yAxis) {
				query.aggregate(queryType, yAxis, COUNT_FIELD_NAME);
			} else {
				query.aggregate(queryType, '*', COUNT_FIELD_NAME);
			}

			XDATA.activityLogger.logSystemActivity('BarChart - query for data');
			connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
				$scope.$apply(function(){
					XDATA.activityLogger.logSystemActivity('BarChart - received query data');
					doDrawChart(queryResults);
					XDATA.activityLogger.logSystemActivity('BarChart - rendered results');
				});
			}, function() {
				XDATA.activityLogger.logSystemActivity('BarChart - query failed');
                drawBlankChart();
			});
		};

		var drawBlankChart = function() {
			doDrawChart({data: []});
		};

		var doDrawChart = function(data) {
			// Destroy the old chart and rebuild it.
			if ($scope.chart) {
				$scope.chart.destroy();	
			}

			var xAxis = $scope.attrX || connectionService.getFieldMapping("bar_x_axis");
			var yAxis = $scope.attrY || connectionService.getFieldMapping("y_axis");

			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			} else {
				yAxis = COUNT_FIELD_NAME;
			}

			var opts = { "data": data.data, "x": xAxis, "y": yAxis, responsive: true, height: 250};
			$scope.chart = new charts.BarChart(el[0], '.barchart', opts).draw();
		};

		neon.ready(function () {
			$scope.messenger = new neon.eventing.Messenger();
			initialize();
		});
	};

	return {
		templateUrl: 'partials/barchart.html',
		restrict: 'E',
		scope: {
			attrX: '=',
			attrY: '=',
			barType: '='
		},
		link: link
	};
}]);
