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
                $scope.databaseName = '';
                $scope.tableName = '';
                $scope.fields = [];
                $scope.limit = 500;
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
                    filtersChanged: onFiltersChanged,
                    selectionChanged: onSelectionChanged
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

                var options = {data: data.data};

                if (has_id) {
                    options.id = _id;
                }
                return options;
            };

            /**
             * Event handler for a user selecting fields in the data table.
             * @method onSelection
             * @private
             */
            var onSelection = function() {
                console.log("user selected something in the table.");
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

            /**
             * Event handler for filter changed events issued over Neon's messaging channels.
             * @param {Object} message A Neon filter changed message.
             * @method onFiltersChanged
             * @private
             */ 
            var onFiltersChanged = function(message) {
                // Clear our filters against the last table and filter before requesting data.
                //$scope.queryForData();
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
             * @method queryForData
             */
            $scope.queryForData = function() {

                var query = $scope.buildQuery();

                connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
                    $scope.$apply(function(){
                        $scope.updateData(queryResults);
                    });
                });
            };

            /**
             * Updates the data bound to the table managed by this directive.  This will trigger a change in 
             * the chart's visualization.
             * @param {Object} queryResults Results returned from a Neon query.
             * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
             * @method updateData
             */
            $scope.updateData = function(queryResults) {
                // TODO: handle the new data.
                $scope.tableOptions = $scope.createOptions(queryResults);

                $scope.table = new tables.Table("#" + $scope.tableId, $scope.tableOptions).draw().registerSelectionListener(onSelection);
                $scope.table.refreshLayout();

            };

            $scope.buildQuery = function() {
                var query = new neon.query.Query().selectFrom($scope.databaseName, $scope.tableName);
                query.limit($scope.limit);

                return query;
            }

            // Wait for neon to be ready, the create our messenger and intialize the view and data.
            neon.ready(function () {
                $scope.initialize();
            });

        }
    };
}]);
