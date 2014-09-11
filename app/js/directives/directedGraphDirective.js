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


angular.module('neon.directives', [])
.directive('directedGraph', ['ConnectionService', function (connectionService) {
	return {
		templateUrl: 'app/partials/directives/directedGraph.html',
		restrict: 'EA',
		scope: {

		},
		link: function ($scope, element, attr) {

			$scope.initialize = function () {
				$scope.messenger.events({
					activeDatasetChanged: onDatasetChanged,
					filtersChanged: onFiltersChanged
				});
			};

			var onFiltersChanged = function (message) {
				$scope.queryForChartData();
			};

			var onDatasetChanged = function (message) {
				$scope.databaseName = message.database;
				$scope.tableName = message.table;
				$scope.data = [];

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

			$scope.queryForData = function () {
				/*var query = new neon.query.Query()
					.selectFrom($scope.databaseName, $scope.tableName)
					.where($scope.dateField, '!=', null)
*/
				connectionService.getActiveConnection().executeQuery(query, function (queryResults) {
					$scope.$apply(function () {
						//$scope.updateChartData(queryResults);
					});
				}, function(error) {
					$scope.$apply(function () {
						//$scope.updateChartData([]);
					})
				});

			};

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
