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
 * This Angular JS directive adds a map to a page and has controls for selecting which data available through
 * a Neon connection should be plotted.
 * 
 * @example
 *    &lt;timeline-selector&gt;&lt;/map&gt;<br>
 *    &lt;div map&gt;&lt;/div&gt;
 * 
 * @class neonDemo.directives.map
 * @constructor
 */
angular.module('heatMapDirective', []).directive('heatMap', ['ConnectionService',
	function(connectionService) {

	return {
		templateUrl: 'partials/heatMap.html',
		restrict: 'EA',
		scope: {

		},
		controller: function($scope) {

		},
		link: function($scope, element, attr) {

			element.addClass('heat-map');

			/** 
			 * Initializes the name of the directive's scope variables 
			 * and the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
				// Defaulting the expected date field to 'date'.
				$scope.databaseName = '';
				$scope.tableName = '';
				$scope.fields = [];
				$scope.latitudeField = '';
				$scope.longitudeField = '';
				$scope.sizeByField = '';
				$scope.colorByField = '';

				// Default our time data to an empty array.
				$scope.data = [];

				// Setup our map.
				$scope.mapId = uuid();
				element.append('<div id="' + $scope.mapId + '" class="map"></div>');
		        $scope.map = new coreMap.Map($scope.mapId, {
		        	height: 500,
		        	width: "100%",
		        	responsive: false
		        });
		        // installOptionsPanels();
		        // setMapMappingFunctions();
		        // setLayerChangeListener();
		        // setApplyFiltersListener();
		        $scope.map.draw();
		        $scope.map.register("moveend", this, function() {
		        	console.log("map moved");
		        });

	            // Setup our messenger.
				$scope.messenger = new neon.eventing.Messenger();

				$scope.messenger.events({
					activeDatasetChanged: onDatasetChanged,
					filtersChanged: onFiltersChanged
				});
			};

			/**
			 * Event handler for filter changed events issued over Neon's messaging channels.
			 * @param {Object} message A Neon filter changed message.
			 * @method onFiltersChanged
			 * @private
			 */ 
			var onFiltersChanged = function(message) {
				// Clear our filters against the last table and filter before requesting data.
				$scope.queryForMapData();
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
				$scope.databaseName = message.database;
				$scope.tableName = message.table;

				// Repopulate the field selectors and get the default values.
				connectionService.getActiveConnection().getFieldNames($scope.tableName, function(results) {
				    $scope.$apply(function() {
				        populateFieldNames(results);
				        $scope.latitudeField = connectionService.getFieldMapping($scope.database, $scope.tableName, "latitude");
				        $scope.latitudeField = $scope.latitudeField.mapping || $scope.fields[0];
				        $scope.longitudeField = connectionService.getFieldMapping($scope.database, $scope.tableName, "longitude");
				        $scope.longitudeField = $scope.longitudeField.mapping || $scope.fields[0];
				        $scope.colorByField = connectionService.getFieldMapping($scope.database, $scope.tableName, "color-by");
				        $scope.colorByField = $scope.colorByField.mapping || $scope.fields[0];
				        $scope.sizeByField = connectionService.getFieldMapping($scope.database, $scope.tableName, "size-by");
				        $scope.sizeByField = $scope.sizeByField.mapping || $scope.fields[0];
				    });
				});
			};

			/**
			 * Helper method for setting the fields available for filter clauses.
			 * @param {Array} fields An array of field name strings.
			 * @method populateFieldNames
			 * @private
			 */
			var populateFieldNames = function(fields) {
				$scope.fields = fields;
			};

			/**
			 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
			 * @method queryForMapData
			 */
			$scope.queryForMapData = function() {
				$scope.dateField = connectionService.getFieldMapping($scope.database, $scope.tableName, "date");
				$scope.dateField = $scope.dateField.mapping || 'date';

				var query = new neon.query.Query()
				    .selectFrom($scope.databaseName, $scope.tableName)

				connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
					$scope.$apply(function(){
						$scope.brush = [];
						$scope.updateMapData(queryResults);
					});
				});
			};

			/**
			 * Updates the data bound to the map managed by this directive.  This will trigger a change in 
			 * the chart's visualization.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method updateMapData
			 */
			$scope.updateMapData = function(queryResults) {
				$scope.data = $scope.createMapData(queryResults);
			};

			/**
			 * Creates a new data array used to populate our map.  This function is used
			 * as or by Neon query handlers.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method createMapData
			 */
			$scope.createMapData = function(queryResults){
				var rawData = queryResults.data;
				var data = [];
				var i = 0;

				return data;
			};

			// Update the millis multipler when the granularity is changed.
			$scope.$watch('sizeByField', function(newVal, oldVal) {
				if (newVal && newVal !== oldVal) {
					$scope.queryForMapData();
				}
			});

			// Update the millis multipler when the granularity is changed.
			$scope.$watch('colorByField', function(newVal, oldVal) {
				if (newVal && newVal !== oldVal) {
					$scope.queryForMapData();
				}
			});

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
