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
				$scope.showPoints = true;
				$scope.cacheMap = false;
				$scope.error = '';

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
		        // setMapMappingFunctions();
		        // setLayerChangeListener();

		        $scope.map.draw();
		        $scope.map.register("moveend", this, onMoved);

	            // Setup our messenger.
				$scope.messenger = new neon.eventing.Messenger();

				$scope.messenger.events({
					activeDatasetChanged: onDatasetChanged,
					filtersChanged: onFiltersChanged,
					selectionChanged: onSelectionChanged
				});

				// Enable the tooltips.
				$(element).find('label.btn-default').tooltip();

			};

            var onMoved = function(message) {
            	// Commenting out for now until we decide whether or not to filter data queries based on visible area.
            	// May need to make this a user-toggleable capability.
            	// $scope.queryForMapData();
            };

			/**
			 * Event handler for selection changed events issued over Neon's messaging channels.
			 * @param {Object} message A Neon selection changed message.
			 * @method onSelectionChanged
			 * @private
			 */ 
			var onSelectionChanged = function(message) {
				$scope.queryForMapData();
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
				$scope.latitudeField = "";
		        $scope.latitudeField = "";
		        $scope.longitudeField = "";
		        $scope.longitudeField = "";
		        $scope.colorByField = "";
		        $scope.colorByField = "";
		        $scope.sizeByField = "";
		        $scope.sizeByField = "";

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
				        $scope.queryForMapData();
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

				if ($scope.latitudeField !== "" && $scope.longitudeField !== "") {
					var query = $scope.buildQuery();

					connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
						$scope.$apply(function(){
							$scope.updateMapData(queryResults);
						});
					});
				}
			};

			/**
			 * Updates the data bound to the map managed by this directive.  This will trigger a change in 
			 * the chart's visualization.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method updateMapData
			 */
			$scope.updateMapData = function(queryResults) {
				var result = $scope.map.setData(queryResults.data);
				$scope.error = result.message;
				$scope.map.draw();
			};

            $scope.buildQuery = function() {
            	// Commenting out the use of the where clause for now.  Filtering on visible area requires
            	// re-issuing the query whenever a pan/zoom is performed which can greatly slow map movements.
            	// var where = $scope.createFilterFromExtent();
		        var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName);
		        var groupByFields = [$scope.latitudeField, $scope.longitudeField];

		        if ($scope.colorByField) {
		            groupByFields.push($scope.colorByField);
		            query = query.groupBy($scope.latitudeField, $scope.longitudeField, $scope.colorByField).selectionOnly();
		        }
		        else {
		        	query = query.groupBy($scope.latitudeField, $scope.longitudeField).selectionOnly();
		        }

		        if ($scope.sizeByField) {
		            query.aggregate(neon.query.SUM, $scope.sizeByField, $scope.sizeByField);
		        }
		        else {
		            query.aggregate(neon.query.COUNT, '*', coreMap.Map.DEFAULT_SIZE_MAPPING);
		        }

		        return query;
		    }

			/**
			 * Create a Neon query to pull data limited to the current extent of the map.
			 * @method createFilterFromExtent
			 */
			$scope.createFilterFromExtent = function() {
		        var extent = $scope.map.getExtent();

		        var leftClause = neon.query.where($scope.longitudeField, ">=", extent.minimumLongitude);
		        var rightClause = neon.query.where($scope.longitudeField, "<=", extent.maximumLongitude);
		        var bottomClause = neon.query.where($scope.latitudeField, ">=", extent.minimumLatitude);
		        var topClause = neon.query.where($scope.latitudeField, "<=", extent.maximumLatitude);
		        var filterClause = neon.query.and(leftClause, rightClause, bottomClause, topClause);

		        //Deal with different dateline crossing scenarios.
		        if (extent.minimumLongitude < -180 && extent.maximumLongitude > 180) {
		            filterClause = neon.query.and(topClause, bottomClause);
		        }
		        else if (extent.minimumLongitude < -180) {
		            leftClause = neon.query.where($scope.longitudeField, ">=", extent.minimumLongitude + 360);
		            var leftDateLine = neon.query.where($scope.longitudeField, "<=", 180);
		            var rightDateLine = neon.query.where($scope.longitudeField, ">=", -180);
		            var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
		            filterClause = neon.query.and(topClause, bottomClause, datelineClause);
		        }
		        else if (extent.maximumLongitude > 180) {
		            rightClause = neon.query.where($scope.longitudeField, "<=", extent.maximumLongitude - 360);
		            var rightDateLine = neon.query.where($scope.longitudeField, ">=", -180);
		            var leftDateLine = neon.query.where($scope.longitudeField, "<=", 180);
		            var datelineClause = neon.query.or(neon.query.and(leftClause, leftDateLine), neon.query.and(rightClause, rightDateLine));
		            filterClause = neon.query.and(topClause, bottomClause, datelineClause);
		        }
		        return filterClause;
		       // return new neon.query.Filter().selectFrom(databaseName, tableName).where(filterClause);
		    }

			// Update the latitude field used by the map.
			$scope.$watch('latitudeField', function(newVal, oldVal) {
				if (newVal && newVal !== oldVal) {
					$scope.map.latitudeMapping = newVal;
					$scope.map.draw();
				}
			});

			// Update the longitude field used by the map.
			$scope.$watch('longitudeField', function(newVal, oldVal) {
				if (newVal && newVal !== oldVal) {
					$scope.map.longitudeMapping = newVal;
					$scope.map.draw();
				}
			});

			// Update the sizing field used by the map.
			$scope.$watch('sizeByField', function(newVal, oldVal) {
				if (newVal !== oldVal) {
					if (newVal) {
						$scope.map.sizeMapping = newVal;
					} 
					else {
						$scope.map.sizeMapping = coreMap.Map.DEFAULT_SIZE_MAPPING;
					}
					$scope.queryForMapData();
				}
			});

			// Update the coloring field used by the map.
			$scope.$watch('colorByField', function(newVal, oldVal) {
				if (newVal !== oldVal) {
					if (newVal) {
						$scope.map.categoryMapping = newVal;
					} 
					else {
						$scope.map.categoryMapping = undefined;
					}
					$scope.queryForMapData();
				}
			});

			// Toggle the points and clusters view when the user toggles between them.
			$scope.$watch('showPoints', function(newVal, oldVal) {
				if (newVal !== oldVal) {
					$scope.map.toggleLayers();
				}
			});

			// Handle toggling map caching.
			$scope.$watch('cacheMap', function(newVal, oldVal) {
				if (newVal !== oldVal) {
					if (newVal) {
						$scope.map.clearCache();
						$scope.map.toggleCaching();
					}
					else {
						$scope.map.toggleCaching();
					}
				}
			})

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
