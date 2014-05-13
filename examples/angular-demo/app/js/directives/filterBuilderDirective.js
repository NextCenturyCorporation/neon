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
 * This Angular JS directive adds a basic Neon filter builder pane to a page.  The pane allows as user
 * to associate basic operators (e.g., >, <, =) and comparison values with table fields on any 
 * open database connection.
 * 
 * @example
 *    &lt;filter-builder&gt;&lt;/filter-builder&gt;<br>
 *    &lt;div filter-builder&gt;&lt;/div&gt;
 * 
 * @class neonDemo.directives.filterBuilder
 * @constructor
 */
angular.module('filterBuilderDirective', []).directive('filterBuilder', ['ConnectionService', 'FilterCountService',
	function(connectionService, filterCountService) {

	return {
		templateUrl: 'partials/filterBuilder.html',
		restrict: 'EA',
        controller: 'neonDemoController',
		scope: {
		},
		link: function($scope, el, attr) {

			/**
			 * Event handler for connection changed events issued over Neon's messaging channels.
			 * @param {Object} message A Neon filter changed message.
			 * @method onConnectionChanged
			 * @private
			 */ 
			var onConnectionChanged = function(message) {
				$scope.filterTable.clearFilterState();
			};

			/**
			 * Event handler for dataset changed events issued over Neon's messaging channels.
			 * @param {Object} message A Neon dataset changed message.
			 * @param {String} message.database The database that was selected.
			 * @param {String} message.table The table within the database that was selected.
			 * @method onDatasetChanged
			 * @private
			 */ 
			var onDatasetChanged = function(message) {
				// Clear the filter table.
				$scope.filterTable.clearFilterState();

				// Save the new database and table name; Fetch the new table fields.
				$scope.databaseName = message.database;
				$scope.tableName = message.table;

				// if there is no active connection, try to make one.
				connectionService.connectToDataset(message.datastore, message.hostname, message.database);

				// Query for data only if we have an active connection.
				var connection = connectionService.getActiveConnection();
				if (connection) {
                	connection.getFieldNames($scope.tableName, function(results) {
					    $scope.$apply(function() {
					        populateFieldNames(results);
					        $scope.selectedField = results[0];
					    });
					});
                }
			};

			/** 
			 * Initializes the name of the date field used to query the current dataset
			 * and the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
				$scope.fields = [];
				$scope.messenger = new neon.eventing.Messenger();
				$scope.filterTable = new neon.query.FilterTable();
				// Use a single session based filter key for this directive instance to allow multiple
				// filter sets to be used in the same application.
				$scope.filterTable.setFilterKey(neon.widget.getInstanceId("filterBuilder"));
				$scope.selectedField = "Select Field";
				$scope.andClauses = true;
				$scope.selectedOperator = $scope.filterTable.operatorOptions[0] || '=';

				$scope.messenger.events({
					activeConnectionChanged: onConnectionChanged,
					activeDatasetChanged: onDatasetChanged
				});
			};

			/**
			 * Adds a filter row to the table of filter clauses from the current selections.
			 * @method addFilterRow
			 */
			$scope.addFilterRow = function() {
				var row = new neon.query.FilterRow($scope.selectedField, $scope.selectedOperator, $scope.selectedValue);
				$scope.filterTable.addFilterRow(row);

				var filter = $scope.filterTable.buildFilterFromData($scope.databaseName, $scope.tableName, $scope.andClauses);
				$scope.messenger.replaceFilter($scope.filterTable.filterKey, filter, function(){
					// On succesful filter, reset the user input on the add filter row so it's obvious which rows
					// are filters and which is the primary Add Filter row.
					$scope.$apply(function() {
						$scope.selectedField = $scope.fields[0];
						$scope.selectedOperator = $scope.filterTable.operatorOptions[0];
						$scope.selectedValue = "";
					});
		        }, function() {
		        	$scope.$apply(function() {
			        	// Error handler:  the addition to the filter failed.  Remove it.
			        	$scope.filterTable.removeFilterRow($scope.filterTable.filterState.data.length - 1);

			        	// TODO: Notify the user.
			        });
		        });
			};

			/**
			 * Removes a filter row from the table of filter clauses.
			 * @param {Number} index The row to remove.
			 * @method updateFilterRow
			 */
			$scope.removeFilterRow = function(index) {

				var row = $scope.filterTable.removeFilterRow(index);
				// TODO: Make the neon call to remove it from the server.

				var filter = $scope.filterTable.buildFilterFromData($scope.databaseName, $scope.tableName, $scope.andClauses);
				$scope.messenger.replaceFilter($scope.filterTable.filterKey, filter, function(){
					//$scope.$apply();
		        }, function() {
		        	$scope.$apply(function() {
			        	// Error handler:  the addition to the filter failed.  Remove it.
			        	$scope.filterTable.setFilterRow(row, index);

			        	// TODO: Notify the user.
			        });
		        });
			}

			/**
			 * Updates a filter row from current visible values and resets the filters on the server.
			 * @param {Number} index The row to update and push to the server.
			 * @method updateFilterRow
			 */
			$scope.updateFilterRow = function(index) {
				var row = $scope.filterTable.getFilterRow(index);

        		var filter = $scope.filterTable.buildFilterFromData($scope.databaseName, $scope.tableName, $scope.andClauses);
        		$scope.messenger.replaceFilter($scope.filterTable.filterKey, filter, function(){
					// No action required at present.
		        }, function() {
		        	$scope.$apply(function() {
			        	// Error handler:  If the new query failed, reset the previous value of the AND / OR field.
			        	$scope.filterTable.filterState = oldVal;

			        	// TODO: Notify the user of the error.
			        });
		        });
			}

			/**
			 * Resets the current filter. 
			 * @method resetFilters
			 */
			$scope.resetFilters = function() {
				$scope.messenger.removeFilter($scope.filterTable.filterKey, function(){
					$scope.$apply(function() {
						// Remove the visible filter list.
						$scope.filterTable.clearFilterState();
					});
		        }, function() {
		        	// TODO: Notify the user of the error.
		        });
			}

			/**
			 * Helper method for setting the fields available for filter clauses.
			 * @param {Array} fields An array of field name strings.
			 * @method populateFieldNames
			 * @private
			 */
			var populateFieldNames = function(fields) {
				$scope.fields = fields;
			};

			// Adjust the filters whenever the user toggles AND/OR clauses.
            $scope.$watch('andClauses', function(newVal, oldVal) {
            	if (newVal != oldVal) {
            		var filter = $scope.filterTable.buildFilterFromData($scope.databaseName, $scope.tableName, $scope.andClauses);
            		$scope.messenger.replaceFilter($scope.filterTable.filterKey, filter, function(){
						// No action required at present.
			        }, function() {
			        	$scope.$apply(function() {
				        	// Error handler:  If the new query failed, reset the previous value of the AND / OR field.
				        	$scope.andClauses = !$scope.andClauses;

				        	// TODO: Notify the user of the error.
				        });
			        });
            	}
            });

            $scope.$watch('filterTable', function(newVal, oldVal) {
            	if (newVal != oldVal) {
            		$(el).find('.tray-mirror.filter-tray .inner').height($('#filter-tray > .container').outerHeight(true));
            	}
            }, true);

            $scope.$watch('filterTable.filterState.data', function(rows) {
                filterCountService.setCount(rows.length);
            },true);

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});
		}
	}
}]);
