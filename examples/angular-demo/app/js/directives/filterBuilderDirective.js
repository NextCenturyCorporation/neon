angular.module('filterBuilderDirective', []).directive('filterBuilder', ['ConnectionService', 
	function(connectionService) {

	return {
		templateUrl: 'partials/filterBuilder.html',
		restrict: 'EA',
		scope: {

		},
		link: function($scope, el, attr) {
			$scope.filterTable = new neon.query.FilterTable();
			// Use a single session based filter key for this directive instance to allow multiple
			// filter sets to be used in the same application.
			$scope.filterTable.setFilterKey(neon.widget.getInstanceId());
			$scope.selectedField = "Select Field";
			$scope.andClauses = true;
			$scope.selectedOperator = $scope.filterTable.operatorOptions[0] || '=';

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

				connectionService.getActiveConnection().getFieldNames($scope.tableName, function(results) {
				    $scope.$apply(function() {
				        populateFieldNames(results);
				        $scope.selectedField = results[0];
				    });
				});

				// Clear our filters.
				$scope.messenger.removeFilter($scope.filterTable.getFilterKey());

			};

			/** 
			 * Initializes the name of the date field used to query the current dataset
			 * and the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
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
					//$scope.$apply();
		        }, function() {
		        	$scope.$apply(function() {
			        	// Error handler:  the addition to the filter failed.  Remove it.
			        	$scope.filterTable.removeFilterRow(filterTable.filterState.data.length - 1);

			        	// TODO: Notify the user.
			        });
		        });
			};

			/**
			 * Removes a filter row from the table of filter clauses.
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
			        	$scope.filterTable.insertFilterRow(row, index);

			        	// TODO: Notify the user.
			        });
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

            $scope.$watch('filterTable.filterState', function(newVal, oldVal) {
            	if (newVal != oldVal) {
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
            }, true);

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.messenger = new neon.eventing.Messenger();
				$scope.initialize();
			});
		}
	}
}]);
