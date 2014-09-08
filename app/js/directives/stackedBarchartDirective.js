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
 * This directive adds a barchart with stacked bars to the DOM and drives the visualization data from
 * whatever database and table are currently selected in neon.  This directive accomplishes that
 * by using getting a neon connection from a connection service and listening for
 * neon system events (e.g., data tables changed).  On these events, it requeries the active
 * connection for data and updates applies the change to its scope.  The contained
 * barchart will update as a result.
 * @class neonDemo.directives.stackedbarchart
 * @constructor
 */
var barchart = angular.module('stackedBarchartDirective', []);

barchart.directive('stackedbarchart', ['ConnectionService', function(connectionService) {
	var COUNT_FIELD_NAME = 'Count';

	var link = function($scope, el, attr) {
		el.addClass('barchartDirective');

		var messenger = new neon.eventing.Messenger();
		$scope.database = '';
		$scope.tableName = '';
		$scope.barType = /*$scope.barType ||*/'count'; //Changed because negative values break the display
		$scope.fields = [];
		$scope.xAxisSelect = $scope.fields[0] ? $scope.fields[0] : '';

		var COUNT_FIELD_NAME = 'Count';
		var clientId;

		var initialize = function() {

			drawBlankChart();

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
			$scope.$watch('barType', function(newValue, oldValue) {
				if($scope.databaseName && $scope.tableName) {
					$scope.queryForData();
				}
			});
		};

		var onFiltersChanged = function(message) {
			$scope.queryForData();
		};

		var onDatasetChanged = function(message) {
			$scope.databaseName = message.database;
			$scope.tableName = message.table;

            // if there is no active connection, try to make one.
			connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

			// Pull data.
			var connection = connectionService.getActiveConnection();
			if (connection) {
                connectionService.loadMetadata(function() {
                    $scope.queryForData();
                });
			}

        };

		var queryData = function(yRuleComparator, yRuleVal, next) {
			var xAxis = $scope.attrX || connectionService.getFieldMapping("x_axis");
			var yAxis = $scope.attrY || connectionService.getFieldMapping("y_axis");
			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			}
			var yMin = ($scope.barType ? COUNT_FIELD_NAME : yAxis) + "-min";

			var query = new neon.query.Query()
				.selectFrom($scope.databaseName, $scope.tableName)
				.where(xAxis,'!=', null)
				.where(yAxis, yRuleComparator, yRuleVal)
				.groupBy(xAxis);

			var queryType;
			$scope.barType = 'count';
			if($scope.barType === 'count') {
				queryType = neon.query.COUNT;
			} else if($scope.barType === 'sum') {
				queryType = neon.query.SUM;
			} else if($scope.barType === 'avg') {
				queryType = neon.query.AVG;
			}

			if(yAxis) {
				query.aggregate(queryType, yAxis, ($scope.barType ? COUNT_FIELD_NAME : yAxis));
			} else {
				query.aggregate(queryType, '*', ($scope.barType ? COUNT_FIELD_NAME : yAxis));
			}

			connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
				next(queryResults);
			});
		}

		$scope.queryForData = function() {
			var xAxis = $scope.attrX || connectionService.getFieldMapping("x_axis");
			var yAxis = $scope.attrY || connectionService.getFieldMapping("y_axis");
			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			}

			var yField = ($scope.barType ? COUNT_FIELD_NAME : yAxis);
			var yMin = yField + "-min";


			var results = {data:[]};

			queryData('>', 0, function(posResults) {
				for(var i = 0; i < posResults.data.length; i++) {
					posResults.data[i][yMin] = 0;
				}
				queryData('<', 0, function(negResults) {
					//sort both by x
					var pos = posResults.data.sort(function(a, b) {
						return a[xAxis].localeCompare(b[xAxis]);
					});
					var neg = negResults.data.sort(function(a, b) {
						return a[xAxis].localeCompare(b[xAxis]);
					});

					var i = 0;
					var j = 0;
					var total;

					var positiveClass = "positive-bar";
					var negativeClass = "negative-bar";

					while(i < pos.length || j < neg.length) {
						if(i < pos.length && j < neg.length) {
							if(pos[i][xAxis] === neg[j][xAxis]) {
								//total
								total = pos[i][yField] + neg[j][yField];
								//1 -> 0-1
								pos[i][yMin] = 0;
								//2 -> 1-2
								neg[j][yMin] = pos[i][yField]; //FIXME there is an error causing the second bar height to be wrong
								neg[j][yField] = total;

								pos[i].classString = positiveClass;
								results.data.push(pos[i]);
								i++;
								neg[j].classString = negativeClass;
								results.data.push(neg[j]);
								j++;
							} else {
								// push lesser x
								if(pos[i][xAxis].localeCompare(neg[j][xAxis]) < 0) {
									pos[i].classString = positiveClass;
									results.data.push(pos[i]);
									i++;
								} else {
									neg[j].classString = negativeClass;
									results.data.push(neg[j]);
									j++;
								}
							}
						} else {
							if(i < pos.length) {
								pos[i].classString = positiveClass;
								results.data.push(pos[i]);
								i++;
							} else {
								neg[j].classString = negativeClass;
								results.data.push(neg[j]);
								j++;
							}
						}
					}

					$scope.$apply(function() {
						doDrawChart(results);
					});
				});
			});
		};

		var drawBlankChart = function() {
			doDrawChart({data: []});
		};

		var doDrawChart = function(data) {
			charts.BarChart.destroy(el[0], '.barchart');

			var xAxis = connectionService.getFieldMapping("x_axis");
				xAxis = xAxis || $scope.attrX;
			var yAxis = connectionService.getFieldMapping("y_axis");
				yAxis = yAxis || $scope.attrY;
			if (!yAxis) {
				yAxis = COUNT_FIELD_NAME;
			}
			var yMin = ($scope.barType ? COUNT_FIELD_NAME : yAxis) + "-min";

			var opts = {
				data: data.data,
				x: xAxis,
				y: ($scope.barType ? COUNT_FIELD_NAME : yAxis),
				yMin: yMin,
				stacked: true,
				responsive: false
			};
			var chart = new charts.BarChart(el[0], '.barchart', opts).draw();
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
