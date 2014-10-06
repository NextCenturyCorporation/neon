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
 *    &lt;heat-map&gt;&lt;/heat-map&gt;<br>
 *    &lt;div heat-map&gt;&lt;/div&gt;
 *
 * @class neonDemo.directives.heatMap
 * @constructor
 */
angular.module('heatMapDirective', []).directive('heatMap', ['ConnectionService', '$timeout',
    function (connectionService, $timeout) {

        return {
            templateUrl: 'partials/heatMap.html',
            restrict: 'EA',
            scope: {
                // map of categories to colors used for the legend
                colorMappings: '&'
            },
            controller: function ($scope) {

            },
            link: function ($scope, element, attr) {

                element.addClass('heat-map');

                // Setup scope variables.
                $scope.databaseName = '';
                $scope.tableName = '';
                $scope.fields = [];
                $scope.latitudeField = '';
                $scope.longitudeField = '';
                $scope.sizeByField = '';
                $scope.colorByField = '';
                $scope.showPoints = false;  // Default to the heatmap view.
                $scope.cacheMap = false;
                $scope.initializing = true;
                $scope.filterKey = neon.widget.getInstanceId("map");
                $scope.showFilter = false;
                $scope.dataBounds = undefined;
                $scope.limit = 1000;  // Max points to pull into the map.

                /**
                 * Initializes the name of the directive's scope variables
                 * and the Neon Messenger used to monitor data change events.
                 * @method initialize
                 */
                $scope.initialize = function () {

                    // optionsDisplayed is used merely to track the display of the options menu
                    // for usability and workflow analysis.
                    $scope.optionsDisplayed = false;

                    // Setup our map.
                    $scope.mapId = uuid();
                    element.append('<div id="' + $scope.mapId + '" class="map"></div>');
                    $scope.map = new coreMap.Map($scope.mapId, {
                        height: 375,
                        width: "100%",
                        responsive: false,
                        onZoomRect: onZoomChanged
                    });

                    $scope.draw();
                    $scope.map.register("movestart", this, onMapEvent);
                    $scope.map.register("moveend", this, onMapEvent);
                    $scope.map.register("zoom", this, onMapEvent);
                    $scope.map.register("zoomend", this, onMapEvent);

                    // Setup our messenger.
                    $scope.messenger = new neon.eventing.Messenger();

                    $scope.messenger.events({
                        activeDatasetChanged: onDatasetChanged,
                        filtersChanged: onFiltersChanged
                    });

                    // Enable the tooltips.
                    $(element).find('label.btn-default').tooltip();

                    // Setup the control watches.
                    // Update the latitude field used by the map.
                    $scope.$watch('latitudeField', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - selected latitude field', 'set_map_layer_properties',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                from: oldVal,
                                to: newVal
                            });
                        if (newVal && newVal !== oldVal) {
                            $scope.map.latitudeMapping = newVal;
                            $scope.draw();
                        }
                    });

                    // Update the longitude field used by the map.
                    $scope.$watch('longitudeField', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - selected longitude field', 'set_map_layer_properties',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                from: oldVal,
                                to: newVal
                            });
                        if (newVal && newVal !== oldVal) {
                            $scope.map.longitudeMapping = newVal;
                            $scope.draw();
                        }
                    });

                    // Update the sizing field used by the map.
                    $scope.$watch('sizeByField', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - selected sizeBy field', 'set_map_layer_properties',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                from: oldVal,
                                to: newVal
                            });
                        if (newVal !== oldVal) {
                            // Set the size by field if we are on a point layer.
                            if ($scope.showPoints) {
                                $scope.setMapSizeMapping(newVal);
                                $scope.draw();
                            }
                            //$scope.queryForMapData();
                        }
                    });

                    // Update the coloring field used by the map.
                    $scope.$watch('colorByField', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - selected colorBy field', 'set_map_layer_properties',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                from: oldVal,
                                to: newVal
                            });
                        $scope.map.resetColorMappings();
                        if (newVal !== oldVal) {
                            $scope.setMapCategoryMapping(newVal);
                            $scope.draw();
                            //$scope.queryForMapData();
                        }
                    });

                    // Toggle the points and clusters view when the user toggles between them.
                    $scope.$watch('showPoints', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - user toggled visible map layer', 'select_map_layer_type',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                points: newVal,
                                clusters: !newVal
                            });
                        if (newVal !== oldVal) {
                            if ($scope.showPoints) {
                                $scope.setMapSizeMapping($scope.sizeByField);
                            }
                            else {
                                $scope.setMapSizeMapping('');
                            }
                            $scope.map.draw();
                            $scope.map.toggleLayers();
                        }
                    });

                    // Handle toggling map caching.
                    $scope.$watch('cacheMap', function (newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - user enabled/disabled map caching', 'toggle_map_caching',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                pointsLayer: newVal,
                                heatMapLayer: !newVal
                            });
                        if (newVal !== oldVal) {
                            if (newVal) {
                                $scope.map.clearCache();
                                $scope.map.toggleCaching();
                            }
                            else {
                                $scope.map.toggleCaching();
                            }
                        }
                    });

                    // Log whenever the user toggles the options display.
                    $scope.$watch('optionsDisplayed', function(newVal, oldVal) {
                        var action = (newVal === true) ? 'show_options' : 'hide_options';
                        XDATA.activityLogger.logUserActivity('HeatMap - user toggled options display', action,
                            XDATA.activityLogger.WF_CREATE,
                            {
                                from: oldVal,
                                to: newVal
                            });
                    });

                    $scope.$watch('limit', function(newVal, oldVal) {
                        XDATA.activityLogger.logUserActivity('HeatMap - user change number of displayed points', 'set_map_layer_properties',
                            XDATA.activityLogger.WF_GETDATA,
                            {
                                from: oldVal,
                                to: newVal
                            });
                        $scope.queryForMapData();
                    })

                };

                var onMapEvent = function (message) {
                    var type = message.type;
                    type = type.replace("move", "pan");
                    type = type.replace("start", "_start");
                    type = type.replace("end", "_end");
                    XDATA.activityLogger.logUserActivity('HeatMap - user interacted with map', type,
                            XDATA.activityLogger.WF_EXPLORE);
                };

                /**
                 * Event handler for filter changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon filter changed message.
                 * @method onFiltersChanged
                 * @private
                 */
                var onFiltersChanged = function (message) {
                    XDATA.activityLogger.logSystemActivity('HeatMap - received neon filter changed event');
                    $scope.queryForMapData();
                };

                var onZoomChanged = function (bounds) {
                    var extent = boundsToExtent(bounds);
                    var filter = $scope.createFilterFromExtent(extent);

                    XDATA.activityLogger.logUserActivity('HeatMap - user defined geographic filter area', 'execute_visual_filter',
                        XDATA.activityLogger.WF_GETDATA, extent);
                    XDATA.activityLogger.logSystemActivity('HeatMap - applying neon filter based on users geographic selection');
                    $scope.messenger.replaceFilter($scope.filterKey, filter, function () {
                        XDATA.activityLogger.logSystemActivity('HeatMap - applied neon filter');
                        $scope.$apply(function () {
                            $scope.queryForMapData();
                            drawZoomRect({
                                left: extent.minimumLongitude, 
                                bottom:extent.minimumLatitude, 
                                right: extent.maximumLongitude, 
                                top: extent.maximumLatitude
                            });

                            // Show the Clear Filter button.
                            $scope.showFilter = true;
                            $scope.error = "";
                            XDATA.activityLogger.logSystemActivity('HeatMap - rendered filter graphic');
                        });
                    }, function () {
                        XDATA.activityLogger.logSystemActivity('HeatMap - Failed to apply neon filter');
                        // Notify the user of the error.
                        $scope.error = "Error: Failed to create filter.";
                    });
                };

                /**
                 * Event handler for dataset changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon dataset changed message.
                 * @param {String} message.database The database that was selected.
                 * @param {String} message.table The table within the database that was selected.
                 * @method onDatasetChanged
                 * @private
                 */
                var onDatasetChanged = function (message) {
                    XDATA.activityLogger.logSystemActivity('HeatMap - received neon dataset changed event');
                    $scope.initializing = true;
                    $scope.databaseName = message.database;
                    $scope.tableName = message.table;
                    $scope.latitudeField = "";
                    $scope.longitudeField = "";
                    $scope.colorByField = "";
                    $scope.sizeByField = "";

                    // Clear the zoom Rect from the map before reinitializing it.
                    clearZoomRect();

                    $scope.dataBounds = undefined;
                    $scope.hideClearFilterButton();

                    // if there is no active connection, try to make one.
                    connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

                    // Query for data only if we have an active connection.
                    var connection = connectionService.getActiveConnection();
                    if (connection) {
                        connectionService.loadMetadata(function() {
                            // Repopulate the field selectors and get the default values.
                            XDATA.activityLogger.logSystemActivity('HeatMap - query for data field names');
                            connection.getFieldNames($scope.tableName, function (results) {
                                $scope.$apply(function () {
                                    XDATA.activityLogger.logSystemActivity('HeatMap - data field names received');
                                    populateFieldNames(results);
                                    $scope.latitudeField = connectionService.getFieldMapping("latitude");
                                    $scope.longitudeField = connectionService.getFieldMapping("longitude");
                                    $scope.colorByField = connectionService.getFieldMapping("color_by");
                                    $scope.sizeByField = connectionService.getFieldMapping("size_by");
                                    XDATA.activityLogger.logSystemActivity('HeatMap - field selectors updated');

                                    $timeout(function () {
                                        $scope.initializing = false;
                                        $scope.queryForMapData();
                                    });
                                });
                            });
                        });
                    }
                };

                /**
                 * Helper method for setting the fields available for filter clauses.
                 * @param {Array} fields An array of field name strings.
                 * @method populateFieldNames
                 * @private
                 */
                var populateFieldNames = function (fields) {
                    $scope.fields = fields;
                };

                var boundsToExtent = function (bounds) {
                    var llPoint = new OpenLayers.LonLat(bounds.left, bounds.bottom);
                    var urPoint = new OpenLayers.LonLat(bounds.right, bounds.top);

                    var minLon = Math.min(llPoint.lon, urPoint.lon);
                    var maxLon = Math.max(llPoint.lon, urPoint.lon);

                    var minLat = Math.min(llPoint.lat, urPoint.lat);
                    var maxLat = Math.max(llPoint.lat, urPoint.lat);

                    return {
                        minimumLatitude: minLat,
                        minimumLongitude: minLon,
                        maximumLatitude: maxLat,
                        maximumLongitude: maxLon
                    };

                };

                var clearZoomRect = function () {
                    if ($scope.zoomRectId !== undefined) {
                        $scope.map.removeBox($scope.zoomRectId);
                        $scope.zoomRectId = undefined;
                    }
                };

                var drawZoomRect = function(rect) {
                    // Clear the old rect.
                    clearZoomRect();

                    // Draw the new rect
                    if (rect !== undefined) {
                        $scope.zoomRectId = $scope.map.drawBox(rect);
                    }
                };

                /**
                 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
                 * @method queryForMapData
                 */
                $scope.queryForMapData = function () {
                    if (!$scope.initializing && $scope.latitudeField !== "" && $scope.longitudeField !== "") {
                        var query = $scope.buildPointQuery();
                        XDATA.activityLogger.logSystemActivity('HeatMap - query for map data');
                        connectionService.getActiveConnection().executeQuery(query, function (queryResults) {
                            $scope.$apply(function () {
                                XDATA.activityLogger.logSystemActivity('HeatMap - map data received');
                                $scope.updateMapData(queryResults);
                                XDATA.activityLogger.logSystemActivity('HeatMap - rendered map data');
                            }, function() {
                                XDATA.activityLogger.logSystemActivity('HeatMap - Failed to receive map data');
                            });
                        });
                    }
                };

                /**
                 * Redraws the map
                 */
                $scope.draw = function () {
                    if (!$scope.initializing) {
                        $scope.map.draw();
                    }

                    // color mappings need to be updated after drawing since they are set during drawing
                    $scope.colorMappings = $scope.map.getColorMappings();
                };

                /**
                 * Updates the data bound to the map managed by this directive.  This will trigger a change in
                 * the chart's visualization.
                 * @param {Object} queryResults Results returned from a Neon query.
                 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
                 * @method updateMapData
                 */
                $scope.updateMapData = function (queryResults) {
                    var data = queryResults.data;
                    $scope.map.setData(data);
                    $scope.draw();
                    if ($scope.dataBounds === undefined) {
                        $scope.dataBounds = $scope.computeDataBounds(data);
                        $scope.zoomToDataBounds();
                    } 
                };

                /**
                 * Zooms the map to the current data bounds
                 */
                $scope.zoomToDataBounds = function () {
                    $scope.map.zoomToBounds($scope.dataBounds);
                };

                /**
                 * Computes the minimum bounding rect to bound the data
                 * @param data
                 */
                $scope.computeDataBounds = function (data) {
                    if (data.length === 0) {
                        return {left: -180, bottom: -90, right: 180, top: 90};
                    }
                    else {
                        var minLon = 180;
                        var minLat = 90;
                        var maxLon = -180;
                        var maxLat = -90;
                        data.forEach(function (d) {
                            var lat = d[$scope.latitudeField];
                            var lon = d[$scope.longitudeField];
                            if (lon < minLon) {
                                minLon = lon;
                            }
                            if (lon > maxLon) {
                                maxLon = lon;
                            }
                            if (lat < minLat) {
                                minLat = lat;
                            }
                            if (lat > maxLat) {
                                maxLat = lat;
                            }
                        });
                        return { left: minLon, bottom: minLat, right: maxLon, top: maxLat};
                    }

                }

                $scope.buildQuery = function () {
                    var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName).limit($scope.limit)
                    var groupByFields = [$scope.latitudeField, $scope.longitudeField];

                    if ($scope.colorByField) {
                        groupByFields.push($scope.colorByField);
                        query = query.groupBy($scope.latitudeField, $scope.longitudeField, $scope.colorByField)
                    }
                    else {
                        query = query.groupBy($scope.latitudeField, $scope.longitudeField);
                    }

                    if ($scope.sizeByField) {
                        query.aggregate(neon.query.SUM, $scope.sizeByField, $scope.sizeByField);
                    }
                    else {
                        query.aggregate(neon.query.COUNT, '*', coreMap.Map.DEFAULT_SIZE_MAPPING);
                    }

                    return query;
                }

                $scope.buildPointQuery = function() {
                    var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName).limit($scope.limit)
                    return query;
                }

                $scope.hideClearFilterButton = function () {
                    // hide the Clear Filter button.
                    $scope.showFilter = false;
                    $scope.error = "";
                };

                /**
                 * Create a Neon query to pull data limited to the current extent of the map.
                 * @method createFilterFromExtent
                 */
                $scope.createFilterFromExtent = function (extent) {

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

                    return new neon.query.Filter().selectFrom($scope.databaseName, $scope.tableName).where(filterClause);
                };

                /**
                 * Clear Neon query to pull data limited to the current extent of the map.
                 * @method clearFilter
                 */
                $scope.clearFilter = function () {
                    XDATA.activityLogger.logUserActivity('HeatMap - user removed geographic filter area', 'remove_visual_filter',
                        XDATA.activityLogger.WF_GETDATA);
                    XDATA.activityLogger.logSystemActivity('HeatMap - removing neon filter based on users geographic selection');
                    $scope.messenger.removeFilter($scope.filterKey, function () {
                        $scope.$apply(function () {
                            XDATA.activityLogger.logSystemActivity('HeatMap - geographic neon filter removed');
                            clearZoomRect();
                            $scope.queryForMapData();
                            $scope.hideClearFilterButton();
                            $scope.zoomToDataBounds();
                            XDATA.activityLogger.logSystemActivity('HeatMap - removed filter graphics');
                        });
                    }, function () {
                        XDATA.activityLogger.logSystemActivity('HeatMap - Failed to remove neon filter based on users geographic selection');
                        // Notify the user of the error.
                        $scope.error = "Error: Failed to clear filter.";
                    });
                };

                /**
                 * Sets the size mapping field used by the map for its layers.  This should be a top level
                 * field in the data objects passed to the map.
                 * @param String mapping
                 * @method setMapSizeMapping
                 */
                $scope.setMapSizeMapping = function(mapping) {
                    if (mapping) {
                        $scope.map.sizeMapping = mapping;
                    }
                    else {
                        $scope.map.sizeMapping = "";
                    }
                    $scope.map.updateRadii();
                }

                /**
                 * Sets the category mapping field used by the map for its layers.  This should be a top level
                 * field in the data objects passed to the map.  If a non-truthy mapping is provided, the
                 * @param String mapping
                 * @method setMapCategoryMapping
                 */
                $scope.setMapCategoryMapping = function(mapping) {
                    if (mapping) {
                        $scope.map.categoryMapping = mapping;
                    }
                    else {
                        $scope.map.categoryMapping = undefined;
                    }
                }

                /**
                 * Toggles whether or not the options menu should be displayed.
                 * @method toggleOptionsDisplay
                 */
                $scope.toggleOptionsDisplay = function() {
                    $scope.optionsDisplayed = !$scope.optionsDisplayed;
                };

                // Wait for neon to be ready, the create our messenger and intialize the view and data.
                neon.ready(function () {
                    $scope.initialize();
                });

            }
        };
    }]);
