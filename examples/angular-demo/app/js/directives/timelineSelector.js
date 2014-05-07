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
 * This Angular JS directive adds a timeline selector to a page.  The timeline selector uses the Neon
 * API to query the currently selected data source the number of records matched by current Neon filters.
 * These records are binned by hour to display the number of records available temporally.  Additionally,
 * the timeline includes a brushing tool that allows a user to select a time range.  The time range is
 * set as a Neon selection filter which will limit the records displayed by any visualization that
 * filters their datasets with the active selection.
 *
 * @example
 *    &lt;timeline-selector&gt;&lt;/timeline-selector&gt;<br>
 *    &lt;div timeline-selector&gt;&lt;/div&gt;
 *
 * @class neonDemo.directives.timelineSelector
 * @constructor
 */
angular.module('timelineSelectorDirective', []).directive('timelineSelector', ['ConnectionService',
    function (connectionService) {

        return {
            templateUrl: 'partials/timelineSelector.html',
            restrict: 'EA',
            scope: {

            },
            controller: function ($scope) {

            },
            link: function ($scope, element, attr) {

                // Cache the number of milliseconds in an hour for processing.
                var MILLIS_IN_HOUR = 1000 * 60 * 60;
                var MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;
                var HOUR = "hour";
                var DAY = "day";

                element.addClass('timeline-selector');

                /**
                 * Initializes the name of the date field used to query the current dataset
                 * and the Neon Messenger used to monitor data change events.
                 * @method initialize
                 */
                $scope.initialize = function () {
                    // Defaulting the expected date field to 'date'.
                    $scope.dateField = 'date';

                    // Default our time data to an empty array.
                    $scope.data = [];
                    $scope.brush = [];
                    $scope.extentDirty = false;
                    $scope.startDate = undefined;
                    $scope.startDateForDisplay = undefined;
                    $scope.endDate = undefined;
                    $scope.endDateForDisplay = undefined;
                    $scope.referenceStartDate = undefined;
                    $scope.referenceEndDate = undefined;

                    $scope.granularity = DAY;
                    $scope.millisMultiplier = MILLIS_IN_DAY;
                    $scope.recordCount = 0;
                    $scope.filterId = 'timelineFilter';
                    $scope.filterKey = neon.widget.getInstanceId($scope.filterId);
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
                var onFiltersChanged = function (message) {
                    $scope.queryForChartData();
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
                    $scope.startDate = undefined;
                    $scope.startDateForDisplay = undefined;
                    $scope.endDate = undefined;
                    $scope.endDateForDisplay = undefined;
                    $scope.referenceStartDate = undefined;
                    $scope.referenceEndDate = undefined;
                    $scope.data = [];
                    $scope.brush = [];
                    $scope.queryForChartData();
                };

                /**
                 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
                 * @method queryForChartData
                 */
                $scope.queryForChartData = function () {
                    $scope.dateField = connectionService.getFieldMapping($scope.database, $scope.tableName, "date");
                    $scope.dateField = $scope.dateField.mapping || 'date';

                    var yearGroupClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, $scope.dateField, 'year');
                    var monthGroupClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, $scope.dateField, 'month');
                    var dayGroupClause = new neon.query.GroupByFunctionClause(neon.query.DAY, $scope.dateField, 'day');
                    var hourGroupClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, $scope.dateField, 'hour');

                    var query = new neon.query.Query()
                        .selectFrom($scope.databaseName, $scope.tableName)
                        .where($scope.dateField, '!=', null)

                    // Group by the appropriate granularity.
                    if ($scope.granularity === DAY) {
                        query.groupBy(yearGroupClause, monthGroupClause, dayGroupClause);
                    }
                    else if ($scope.granularity === HOUR) {
                        query.groupBy(yearGroupClause, monthGroupClause, dayGroupClause, hourGroupClause);
                    }

                    query.aggregate(neon.query.COUNT, '*', 'count');
                    // TODO: Does this need to be an aggregate on the date field? What is MIN doing or is this just an arbitrary function to include the date with the query?
                    query.aggregate(neon.query.MIN, $scope.dateField, 'date');
                    query.sortBy('date', neon.query.ASCENDING);
                    query.ignoreFilters([$scope.filterKey]);
                    connectionService.getActiveConnection().executeQuery(query, function (queryResults) {
                        $scope.$apply(function () {
                            $scope.updateChartData(queryResults);
                        });
                    });

                };

                /**
                 * Updates the chart start/end times to use as a Neon selection and their associated conversion for displaying in
                 * UTC. The display value for the total records is updates as well.
                 * of the data array.
                 * @method updateChartTimesAndTotal
                 */
                $scope.updateChartTimesAndTotal = function () {
                    // Handle bound conditions.

                    var extentStartDate;
                    var extentEndDate;
                    if (this.brush.length == 2) {
                        extentStartDate = this.brush[0];
                        extentEndDate = this.brush[1];
                    }
                    else {
                        extentStartDate = $scope.startDate;
                        extentEndDate = $scope.endDate;
                    }

                    // can happen when switching between granularities on edge cases
                    if (extentStartDate < $scope.startDate) {
                        extentStartDate = $scope.startDate;
                    }

                    if (extentEndDate > $scope.endDate) {
                        extentEndDate = $scope.endDate;
                    }

                    extentStartDate = $scope.zeroOutDate(extentStartDate);
                    extentEndDate = $scope.roundUpBucket(extentEndDate);

                    var startIdx = Math.floor(Math.abs($scope.startDate - extentStartDate) / $scope.millisMultiplier);
                    var endIdx = Math.floor(Math.abs($scope.startDate - extentEndDate) / $scope.millisMultiplier);

                    // Update the start/end times and totals used for the Neon selection and their
                    // display versions.  Since Angular formats dates as local values, we create new display values
                    // for the appropriate month/day/hours we want to appear in this directive's associated partial.
                    // This essentially shifts the display times from local to the value we want to appear in UTC time.
                    var total = 0;
                    // endIdx points to the start of the day/hour just after the buckets we want to count, so do not
                    // include the bucket at endIdx.
                    for (var i = startIdx; i < endIdx; i++) {
                        total += $scope.data[i].value;
                    }

                    var displayStartDate = new Date(extentStartDate);
                    var displayEndDate = new Date(extentEndDate);
                    if ($scope.granularity === HOUR) {
                        $scope.startDateForDisplay = new Date(displayStartDate.getUTCFullYear(),
                            displayStartDate.getUTCMonth(),
                            displayStartDate.getUTCDate(),
                            displayStartDate.getUTCHours());
                        $scope.endDateForDisplay = new Date(displayEndDate.getUTCFullYear(),
                            displayEndDate.getUTCMonth(),
                            displayEndDate.getUTCDate(),
                            displayEndDate.getUTCHours());
                    }
                    else if ($scope.granularity === DAY) {
                        $scope.startDateForDisplay = new Date(displayStartDate.getUTCFullYear(),
                            displayStartDate.getUTCMonth(),
                            displayStartDate.getUTCDate());
                        $scope.endDateForDisplay = new Date(displayEndDate.getUTCFullYear(),
                            displayEndDate.getUTCMonth(),
                            displayEndDate.getUTCDate());
                    }


                    $scope.recordCount = total;
                }

                $scope.convertDateForDisplay = function (date) {
                    return
                }

                /**
                 * Updates the data bound to the chart managed by this directive.  This will trigger a change in
                 * the chart's visualization.
                 * @param {Object} queryResults Results returned from a Neon query.
                 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
                 * @method updateChartData
                 */
                $scope.updateChartData = function (queryResults) {

                    if (queryResults.data.length > 0) {
                        var updateDatesCallback = function () {
                            if ($scope.startDate === undefined || $scope.endDate === undefined) {
                                $scope.updateDates();
                            }
                            $scope.data = $scope.createTimelineData(queryResults);
                            $scope.updateChartTimesAndTotal();
                        };

                        // on the initial query, setup the start/end bounds
                        if ($scope.referenceStartDate === undefined || $scope.referenceEndDate === undefined) {
                            $scope.getMinMaxDates(updateDatesCallback);
                        }
                        else {
                            updateDatesCallback();
                        }

                    }
                    else {
                        $scope.data = $scope.createTimelineData(queryResults);
                        $scope.recordCount = 0;
                    }
                };

                $scope.getMinMaxDates = function (success) {
                    // TODO: neon doesn't yet support a more efficient way to just get the min/max fields without aggregating
                    // TODO: This could be done better with a promise framework - just did this in a pinch for a demo
                    var minDateQuery = new neon.query.Query()
                        .selectFrom($scope.databaseName, $scope.tableName)
                        .where($scope.dateField, '!=', null).sortBy($scope.dateField, neon.query.ASCENDING).limit(1);

                    connectionService.getActiveConnection().executeQuery(minDateQuery, function (queryResults) {
                        if (queryResults.data.length > 0) {
                            $scope.referenceStartDate = new Date(queryResults.data[0][$scope.dateField]);
                            if ($scope.referenceEndDate !== undefined) {
                                success();
                            }
                        }
                    });

                    var maxDateQuery = new neon.query.Query()
                        .selectFrom($scope.databaseName, $scope.tableName)
                        .where($scope.dateField, '!=', null).sortBy($scope.dateField, neon.query.DESCENDING).limit(1);

                    connectionService.getActiveConnection().executeQuery(maxDateQuery, function (queryResults) {
                        if (queryResults.data.length > 0) {
                            $scope.referenceEndDate = new Date(queryResults.data[0][$scope.dateField]);
                            if ($scope.referenceStartDate !== undefined) {
                                success();
                            }
                        }
                    });

                };

                /**
                 * Updates the starts/end dates based on the chart granularity
                 */
                $scope.updateDates = function () {
                    $scope.startDate = $scope.zeroOutDate($scope.referenceStartDate);
                    $scope.endDate = $scope.zeroOutDate(new Date($scope.referenceEndDate.getTime() + $scope.millisMultiplier));
                };

                /**
                 * Sets the minutes, seconds and millis to 0. If the granularity of the date is day, then the hours are also zeroed
                 * @param date
                 * @returns {Date}
                 */
                $scope.zeroOutDate = function (date) {
                    var zeroed = new Date(date);
                    zeroed.setUTCMinutes(0);
                    zeroed.setUTCSeconds(0);
                    zeroed.setUTCMilliseconds(0);
                    if ($scope.granularity === DAY) {
                        zeroed.setUTCHours(0);
                    }
                    return zeroed;
                };

                /**
                 * Rounds the date up to the beginning of the next bucket
                 * @param date
                 * @returns {Date}
                 */
                $scope.roundUpBucket = function (date) {
                    return $scope.zeroOutDate(new Date(date.getTime() - 1 + $scope.millisMultiplier));
                };

                /**
                 * Creates a new data array used to populate our contained timeline.  This function is used
                 * as or by Neon query handlers.
                 * @param {Object} queryResults Results returned from a Neon query.
                 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
                 * @method createTimelineData
                 */
                $scope.createTimelineData = function (queryResults) {
                    var rawData = queryResults.data;
                    var data = [];
                    var i = 0;
                    var rawLength = rawData.length;

                    // If we have no values, use our dates if they existed or now.
                    if (rawLength === 0) {
                        rawData[0] = {
                            date: new Date(),
                            count: 0
                        }
                    }
                    // If we have only 1 value, create a range for it.
                    else if (rawLength === 1) {
                        rawData[1] = rawData[0];
                        rawLength = 2;
                    }

                    // Setup the data buckets for them.
                    // Determine the number of hour buckets along with the start and end dates for our buckets.
                    // var startDate = new Date(Date.UTC(rawData[0].year, rawData[0].month - 1, rawData[0].day, rawData[0].hour));
                    // var endDate = new Date(Date.UTC(rawData[rawLength - 1].year, rawData[rawLength - 1].month - 1,
                    // 	rawData[rawLength - 1].day, rawData[rawLength - 1].hour));
                    var startDate = $scope.zeroOutDate($scope.startDate);
                    var endDate = $scope.zeroOutDate($scope.endDate);

                    var numBuckets = Math.ceil(Math.abs(endDate - startDate) / $scope.millisMultiplier) + 1;
                    var startTime = startDate.getTime();

                    // Initialize our time buckets.
                    for (i = 0; i < numBuckets; i++) {
                        // Calculate the start date for a bucket (e.g., 01:00, 02:00) using the millisMultiplier.  
                        // Also calculate the temporal midpoint of that bucket by adding 1/2 the multiplier to get the
                        // point on the timeline at which we want to display the count for that time bucket.
                        // For the 01:00 to 01:59 time bucket, we want to display the aggregate value at the
                        // perceived center of the bucket, 01:30, on the timeline graph.
                        var bucketGraphDate = new Date(startTime + ($scope.millisMultiplier * i) + ($scope.millisMultiplier / 2));
                        data[i] = {
                            date: bucketGraphDate,
                            value: 0
                        }
                    }

                    // Fill our rawData into the appropriate hour buckets.
                    var resultDate;
                    for (i = 0; i < rawLength; i++) {
                        resultDate = new Date(rawData[i].date);
                        data[Math.floor(Math.abs(resultDate - startDate) / $scope.millisMultiplier)].value = rawData[i].count;
                    }
                    return data;
                };

                /**
                 * Clears the timeline brush and filter.
                 * @method clearBrush
                 */
                $scope.clearBrush = function () {
                    $scope.brush = [];
                    $scope.messenger.removeFilter($scope.filterKey);
                };

                // Update the millis multipler when the granularity is changed.
                $scope.$watch('granularity', function (newVal, oldVal) {
                    if (newVal && newVal !== oldVal) {
                        $scope.startDateForDisplay = undefined;
                        $scope.endDateForDisplay = undefined;
                        if (newVal === DAY) {
                            $scope.millisMultiplier = MILLIS_IN_DAY;
                        }
                        else if (newVal === HOUR) {
                            $scope.millisMultiplier = MILLIS_IN_HOUR;
                        }
                        $scope.updateDates();

                        if ($scope.brush.length > 0) {
                            var newBrushStart = $scope.brush[0];
                            // Set the brush to one millisecond back when changing resolutions back to what we had.
                            // Otherwise, our brush will drift forward in time on consecutive granularity changes due to the 
                            // nature of having to zero out the value and calculating the start point of the next day/hour.
                            var newBrushEnd = $scope.roundUpBucket($scope.brush[1]);

                            if (newBrushStart.getTime() !== $scope.brush[0].getTime() || newBrushEnd.getTime() !== $scope.brush[1].getTime()) {
                                $scope.brush = [newBrushStart, newBrushEnd];
                            }
                            $scope.queryForChartData();

                        }
                        else {
                            $scope.queryForChartData();
                        }
                    }
                });

                // Watch for brush changes and set the appropriate neon filter.
                $scope.$watch('brush', function (newVal) {
                    // If we have a new value and a messenger is ready, set the new filter.
                    if (newVal && $scope.messenger && connectionService.getActiveConnection()) {
                        var startExtent;
                        var endExtent;
                        // if a single spot was clicked, just reset the timeline - An alternative would be to expand to the minimum width
                        if (newVal === undefined || newVal.length < 2 || newVal[0].getTime() === newVal[1].getTime()) {
                            // may be undefined when a new dataset is being loaded
                            if ($scope.startDate !== undefined && $scope.endDate !== undefined) {
                                startExtent = $scope.startDate;
                                endExtent = $scope.endDate;
                                $scope.brush = [];
                                $scope.extentDirty = true;
                            }
                            else {
                                return;
                            }
                        }
                        else {
                            startExtent = newVal[0];
                            endExtent = newVal[1];
                        }

                        var startFilterClause = neon.query.where($scope.dateField, '>=', $scope.zeroOutDate(startExtent));
                        var endFilterClause = neon.query.where($scope.dateField, '<', $scope.roundUpBucket(endExtent));
                        var clauses = [startFilterClause, endFilterClause];
                        var filterClause = neon.query.and.apply(this, clauses);
                        var filter = new neon.query.Filter().selectFrom($scope.databaseName, $scope.tableName).where(filterClause);
                        $scope.messenger.replaceFilter($scope.filterKey, filter, $scope.queryForChartData);
                    }
                }, true);

                // Wait for neon to be ready, the create our messenger and intialize the view and data.
                neon.ready(function () {
                    $scope.initialize();
                });

            }
        };
    }]);
