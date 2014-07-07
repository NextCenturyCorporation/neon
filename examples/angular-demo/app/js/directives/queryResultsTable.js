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
 * This Angular JS directive adds a data table to a page showing the records that match the current
 * filter set.
 * 
 * @example
 *    &lt;query-results-table&gt;&lt;/query-results-table&gt;<br>
 *    &lt;div query-results-table&gt;&lt;/div&gt;
 * 
 * @class neonDemo.directives.queryResultsTable
 * @constructor
 */
angular.module('queryResultsTableDirective', []).directive('queryResultsTable', ['ConnectionService',
    function(connectionService) {

    return {
        templateUrl: 'partials/queryResultsTable.html',
        restrict: 'EA',
        scope: {
            showData: '='
        },
        controller: function($scope) {

        },
        link: function($scope, element, attr) {

            element.addClass('query-results-table');

            /** 
             * Initializes the name of the directive's scope variables 
             * and the Neon Messenger used to monitor data change events.
             * @method initialize
             */
            $scope.initialize = function() {
            	$scope.ASCENDING = neon.query.ASCENDING;
            	$scope.DESCENDING = neon.query.DESCENDING;

                $scope.databaseName = '';
                $scope.tableName = '';
                $scope.fields = [];
                $scope.sortByField = '';
                $scope.sortDirection = neon.query.ASCENDING;
                $scope.limit = 500;
                $scope.totalRows = 0;
                $scope.error = '';

                // Default our data table to be empty.  Generate a unique ID for it 
                // and pass that to the tables.Table object.
                $scope.data = [];
                $scope.tableId = 'query-results-' + uuid();
                var $tableDiv = $(element).find('.query-results-grid');
                var options = $scope.createOptions([]);

                $tableDiv.attr("id", $scope.tableId);

                // Setup our messenger.
                $scope.messenger = new neon.eventing.Messenger();

                $scope.messenger.events({
                    activeDatasetChanged: onDatasetChanged,
                    filtersChanged: onFiltersChanged
                });

            };

            $scope.createOptions = function(data) {
                var _id = "_id";
                var has_id = true;

                _.each(data.data, function (element) {
                    if (!(_.has(element, _id))) {
                        has_id = false;
                    }
                });

                var options = {
                    data: data.data,
                    gridOptions: {
                        forceFitColumns: false,
                        enableColumnReorder: true,
                        forceSyncScrolling: true
                    }
                };

                if (has_id) {
                    options.id = _id;
                }
                return options;
            };

            /**
             * Event handler for filter changed events issued over Neon's messaging channels.
             * @param {Object} message A Neon filter changed message.
             * @method onFiltersChanged
             * @private
             */ 
            var onFiltersChanged = function(message) {
                XDATA.activityLogger.logSystemActivity('DataView - received neon filter changed event');
                updateRowsAndCount();
            };

            /**
             * Updates the data and the count of total rows in the data
             */
            var updateRowsAndCount = function() {
                $scope.queryForTotalRows();
                $scope.queryForData();
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
                XDATA.activityLogger.logSystemActivity('DataView - received neon dataset changed event');
                $scope.databaseName = message.database;
                $scope.tableName = message.table;

                // if there is no active connection, try to make one.
                connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

                // Pull data.
                var connection = connectionService.getActiveConnection();
                if (connection) {
                    connectionService.loadMetadata(function() {
                        connection.getFieldNames($scope.tableName, function (results) {
                            $scope.$apply(function () {
                                populateFieldNames(results);
                                $scope.sortByField = connectionService.getFieldMapping("sort_by");
                                $scope.sortByField = $scope.sortByField || $scope.fields[0];
                                updateRowsAndCount();
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
            var populateFieldNames = function(fields) {
                $scope.fields = fields;
            };

            /**
             * Forces a data query regardless of the current need to query for data.
             * @method refreshData
             */
            $scope.refreshData = function() {
                XDATA.activityLogger.logUserActivity('DataView - user requested table refresh', 'execute_query_filter',
                    XDATA.activityLogger.WF_GETDATA);
                $scope.queryForData();
            }

            /**
             * Triggers a Neon query that pull the a number of records that match the current Neon connection
             * and filter set.  The query will be limited by the record number and sorted by the field
             * selected in this directive's form.  This directive includes support for a show-data directive attribute 
             * that binds to a scope variable and controls table display.  If the bound variable evaulates to false,
             * no data table is generated.  queryForData will not issue a query until the directive thinks it needs to 
             * poll for data and should show data.
             * Resets internal "need to query" state to false.  
             * @method queryForData
             */
            $scope.queryForData = function() {
                if ($scope.showData) {
                    var connection = connectionService.getActiveConnection();
                    if (connection) {
                        var query = $scope.buildQuery();

                        XDATA.activityLogger.logSystemActivity('DataView - query for data');
                        connection.executeQuery(query, function(queryResults) {
                            XDATA.activityLogger.logSystemActivity('DataView - received data');
                            $scope.$apply(function(){
                                $scope.updateData(queryResults);
                                XDATA.activityLogger.logSystemActivity('DataView - rendered data');
                            });
                        });
                    }
                }
            };

            /**
             * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
             * @method queryForData
             */
            $scope.queryForTotalRows = function() {

                var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName)
                    .aggregate(neon.query.COUNT, '*', 'count');

                XDATA.activityLogger.logSystemActivity('DataView - query for total rows of data');
                connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
                    $scope.$apply(function(){
                        if (queryResults.data.length > 0) {
                            $scope.totalRows = queryResults.data[0].count;
                        }
                        else {
                            $scope.totalRows = 0;
                        }
                        XDATA.activityLogger.logSystemActivity('DataView - received total; updating view');
                    });
                });
            };

            /**
             * Refresh query forces a fresh query for data given the current sorting and limiting selections.
             * @method refreshQuery
             */

            /**
             * Updates the data bound to the table managed by this directive.  This will trigger a change in 
             * the chart's visualization.
             * @param {Object} queryResults Results returned from a Neon query.
             * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
             * @method updateData
             */
            $scope.updateData = function(queryResults) {
                // Handle the new data.
                $scope.tableOptions = $scope.createOptions(queryResults);

                $scope.table = new tables.Table("#" + $scope.tableId, $scope.tableOptions).draw();//.registerSelectionListener(onSelection);
                $scope.table.refreshLayout();

            };

            /**
             * Builds a query to pull a limited set of records that match any existing filter sets.
             * @return neon.query.Query
             * @method buildQuery
             */
            $scope.buildQuery = function() {
                var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName);
                query.limit($scope.limit);
                if ($scope.sortByField !== "undefined" && $scope.sortByField.length > 0) {
                    query.sortBy($scope.sortByField, $scope.sortDirection);
                }

                return query;
            }

            // KLUDGE: Watch for changes to showData if it goes from false to true, we want to requery for data to 
            // trigger the data table to be recreated.  While deferring data queries to when the user want to display them
            // is benefitial for initial application load, it can interfere with animations tied to whether or not this is
            // displayed.  The other reason to query for data on show is because of issues with SlickGrid.  It does not 
            // display proper scrolling and sizing behavior if it is rendered while not visible.
            $scope.$watch('showData', function(newVal, oldVal) {
                if(newVal) {
                    $scope.queryForData();
                }
            });

            $scope.$watch('sortByField', function(newVal, oldVal) {
                XDATA.activityLogger.logUserActivity('DataView - user set database level sorting field', 'select_filter_menu_option',
                    XDATA.activityLogger.WF_GETDATA,
                    {
                        from: oldVal,
                        to: newVal
                    });
            });

            $scope.$watch('sortDirection', function(newVal, oldVal) {
                XDATA.activityLogger.logUserActivity('DataView - user set database level sorting direction', 'select_filter_menu_option',
                    XDATA.activityLogger.WF_GETDATA,
                    {
                        from: oldVal,
                        to: newVal
                    });
            });

            $scope.$watch('limit', function(newVal, oldVal) {
                XDATA.activityLogger.logUserActivity('DataView - user set max rows to pull from database', 'enter_filter_text',
                    XDATA.activityLogger.WF_GETDATA,
                    {
                        from: oldVal,
                        to: newVal
                    });
            });

            // Wait for neon to be ready, the create our messenger and intialize the view and data.
            neon.ready(function () {
                $scope.initialize();
            });

        }
    };
}]);
