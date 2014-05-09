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

			//drawChart();

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged,
				selectionChanged: onSelectionChanged
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
			query('>', 0, function(posResults) {
				query('<', 0, function(negResults) {
					var data = [{
						data: posResults.data,
						classString: "positiveLine"
					},{
						data: negResults.data,
						classString: "negativeLine"
					}];

					$scope.$apply(function(){
						drawChart();
						drawLine(data);
					});
				});
			});
		};

		var drawChart = function() {
			var xAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "x-axis");
			xAxis = $scope.attrX || xAxis.mapping;
			var yAxis = connectionService.getFieldMapping($scope.database, $scope.tableName, "y-axis")
			yAxis = $scope.attrY || yAxis.mapping;
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
