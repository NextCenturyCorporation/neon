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
'use strict';

/**
 * This directive is for building a tag cloud
 */
angular.module('tagCloudDirective', []).directive('tagCloud', ['ConnectionService', '$timeout',
    function (connectionService, $timeout) {

        return {
            templateUrl: 'partials/tagCloud.html',
            restrict: 'E',
            scope: {
                tagField: '='
            },
            controller: function ($scope) {
            },
            link: function ($scope, element, attr) {
                /**
                 * Initializes the name of the directive's scope variables
                 * and the Neon Messenger used to monitor data change events.
                 * @method initialize
                 */
                $scope.initialize = function () {
                    $scope.databaseName = '';
                    $scope.tableName = '';

                    // TODO: Temporary workaround for NEON-1069 - too many events being fired
                    $scope.inProgress = false;

                    // data will be a list of tag name/counts in descending order
                    $scope.data = [];

                    // Setup our messenger.
                    $scope.messenger = new neon.eventing.Messenger();

                    $scope.messenger.events({
                        activeDatasetChanged: onDatasetChanged,
                        filtersChanged: onFiltersChanged,
                        selectionChanged: onSelectionChanged
                    });

                    // setup tag cloud color/size changes
                    $.fn.tagcloud.defaults = {
                        size: {start: 12, end: 22, unit: 'pt'},
                        color: {start: '#aaaaaa', end: '#2f9f3e'}
                    };

                };

                /**
                 * Event handler for selection changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon selection changed message.
                 * @method onSelectionChanged
                 * @private
                 */
                var onSelectionChanged = function (message) {
                    $scope.queryForTags();
                };

                /**
                 * Event handler for filter changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon filter changed message.
                 * @method onFiltersChanged
                 * @private
                 */
                var onFiltersChanged = function (message) {
                    $scope.queryForTags();
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
                    $scope.databaseName = message.database;
                    $scope.tableName = message.table;
                    // check if the field was passed in, otherwise check the mapping. if neither is found leave it empty
                    $scope.tagField = $scope.tagField || connectionService.getFieldMapping($scope.databaseName, $scope.tableName, "tags").mapping || '';
                };

                /**
                 * Triggers a Neon query that will aggregate the most popular tags in the tag cloud
                 * @method queryForTags
                 */
                $scope.queryForTags = function () {
                    // TODO: use in progress for temporary workaround for NEON-1069
                    if (!$scope.inProgress) {
                        $scope.inProgress = true;
                        if ($scope.tagField !== '') {
                            var host = connectionService.getActiveConnection().host_;
                            var url = neon.serviceUrl('mongotagcloud', 'tagcounts', 'host=' + host + "&db=" + $scope.databaseName + "&collection=" + $scope.tableName + "&arrayfield=" + $scope.tagField + "&limit=30");
                            neon.util.ajaxUtils.doGet(url, {
                                success: function (tagCounts) {
                                    $scope.$apply(function () {
                                        $scope.updateTagData(tagCounts)
                                        $scope.inProgress = false;
                                    });
                                }
                            });
                        }
                    }
                };

                /**
                 * Updates the data bound to the tag cloud managed by this directive.  This will trigger a change in
                 * the chart's visualization.
                 * @param {Object} tagCloud A mapping of tag names to their counts
                 * @method updateTagData
                 */
                $scope.updateTagData = function (tagCounts) {
                    $scope.data = tagCounts;
                    // style the tags after they are displayed
                    $timeout(function() {
                        element.find('.tag').tagcloud();
                    });
                };

                // Wait for neon to be ready, the create our messenger and intialize the view and data.
                neon.ready(function () {
                    $scope.initialize();
                });

            }
        }
            ;
    }])
;
