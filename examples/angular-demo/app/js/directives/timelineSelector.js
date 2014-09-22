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
                    $scope.primarySeries = false;

                    $scope.granularity = DAY;
                    $scope.millisMultiplier = MILLIS_IN_DAY;
                    $scope.recordCount = 0;
                    $scope.filterId = 'timelineFilter';
                    $scope.filterKey = neon.widget.getInstanceId($scope.filterId);
                    $scope.messenger = new neon.eventing.Messenger();

                    $scope.collapsed = true;
                    $scope.eventProbabilitiesDisplayed = false;

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
                    XDATA.activityLogger.logSystemActivity('TimelineSelector - received neon filter changed event');
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
                    XDATA.activityLogger.logSystemActivity('TimelineSelector - received neon dataset changed event');

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

                    // if there is no active connection, try to make one.
                    connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

                    // Pull data.
                    var connection = connectionService.getActiveConnection();
                    if (connection) {
                        connectionService.loadMetadata(function() {
                            $scope.queryForChartData();
                        });
                    }
                };

                /**
                 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
                 * @method queryForChartData
                 */
                $scope.queryForChartData = function () {
                    $scope.dateField = connectionService.getFieldMapping("date");
                    $scope.dateField = $scope.dateField || 'date';

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

                    XDATA.activityLogger.logSystemActivity('TimelineSelector - query for data');
                    connectionService.getActiveConnection().executeQuery(query, function (queryResults) {
                        $scope.$apply(function () {
                            $scope.updateChartData(queryResults);
                            XDATA.activityLogger.logSystemActivity('TimelineSelector - data received');
                        });
                    }, function(error) {
                        $scope.$apply(function () {
                            $scope.updateChartData([]);
                            XDATA.activityLogger.logSystemActivity('TimelineSelector - data requested failed');
                        })
                    });

                };

                /**
                 * Updates the chart start/end times to use as a Neon selection and their associated conversion for displaying in
                 * UTC. The display value for the total records is updates as well.
                 * of the data array.
                 * @method updateChartTimesAndTotal
                 */
                $scope.updateChartTimesAndTotal = function () {
                    // Try to find primary series in new data
                    var primaryIndex = 0;
                    if($scope.primarySeries){
                        for (var i = 0; i < $scope.data.length; i++) {
                            if($scope.primarySeries.name == $scope.data[i].name){
                                 primaryIndex = i;
                                 break;
                            }
                        }
                    }
                    $scope.primarySeries = $scope.data[primaryIndex];

                    // Handle bound conditions.

                    var extentStartDate;
                    var extentEndDate;
                    if ($scope.brush.length == 2) {
                        extentStartDate = $scope.brush[0];
                        extentEndDate = $scope.brush[1];
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
                        total += $scope.primarySeries.data[i].value;
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
                        $scope.endDateForDisplay = new Date(new Date(displayEndDate.getUTCFullYear(),
                            displayEndDate.getUTCMonth(),
                            displayEndDate.getUTCDate()).getTime() - 1);
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

                    // Any time new data is fetched, the old MMPP analysis is invalidated.
                    $scope.eventProbabilitiesDisplayed = false;

                    if (queryResults.data.length > 0) {
                        var updateDatesCallback = function () {
                            if ($scope.startDate === undefined || $scope.endDate === undefined) {
                                $scope.updateDates();
                            }
                            var data = $scope.createTimelineData(queryResults);
                            $scope.data = data;
                            $scope.updateChartTimesAndTotal();
                            $scope.addTimeSeriesAnalysis(data[0].data, data);
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
                        $scope.updateChartTimesAndTotal();
                    }
                };

                $scope.getMinMaxDates = function (success) {
                    // TODO: neon doesn't yet support a more efficient way to just get the min/max fields without aggregating
                    // TODO: This could be done better with a promise framework - just did this in a pinch for a demo
                    var minDateQuery = new neon.query.Query()
                        .selectFrom($scope.databaseName, $scope.tableName).ignoreFilters()
                        .where($scope.dateField, '!=', null).sortBy($scope.dateField, neon.query.ASCENDING).limit(1);

                    XDATA.activityLogger.logSystemActivity('TimelineSelector - query for minimum date');
                    connectionService.getActiveConnection().executeQuery(minDateQuery, function (queryResults) {
                        if (queryResults.data.length > 0) {
                            XDATA.activityLogger.logSystemActivity('TimelineSelector - minimum date received');
                            $scope.referenceStartDate = new Date(queryResults.data[0][$scope.dateField]);
                            if ($scope.referenceEndDate !== undefined) {
                                $scope.$apply(success);
                            }
                        }
                    });

                    var maxDateQuery = new neon.query.Query()
                        .selectFrom($scope.databaseName, $scope.tableName).ignoreFilters()
                        .where($scope.dateField, '!=', null).sortBy($scope.dateField, neon.query.DESCENDING).limit(1);

                    XDATA.activityLogger.logSystemActivity('TimelineSelector - query for maximum date');
                    connectionService.getActiveConnection().executeQuery(maxDateQuery, function (queryResults) {
                        if (queryResults.data.length > 0) {
                            XDATA.activityLogger.logSystemActivity('TimelineSelector - maximum date received');
                            $scope.referenceEndDate = new Date(queryResults.data[0][$scope.dateField]);
                            if ($scope.referenceStartDate !== undefined) {
                                $scope.$apply(success);
                            }
                        }
                    });

                };

                /**
                 * Updates the starts/end dates based on the chart granularity
                 */
                $scope.updateDates = function () {
                    // Updates depend on having valid reference dates which may not be the case during directive initialization
                    if ($scope.referenceStartDate && $scope.referenceEndDate) {
                        $scope.startDate = $scope.zeroOutDate($scope.referenceStartDate);
                        $scope.endDate = $scope.zeroOutDate(new Date($scope.referenceEndDate.getTime() + $scope.millisMultiplier));
                    }
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
                    var roundedDate = $scope.zeroOutDate(new Date(date.getTime() - 1 + $scope.millisMultiplier));
                    if(roundedDate > $scope.endDate)
                        return $scope.endDate;
                    else
                        return roundedDate
                };

                /**
                 * Rounds the date down to the beginning of the current bucket
                 * @param date
                 * @returns {Date}
                 */
                $scope.roundDownBucket = function (date) {
                    var roundedDate = $scope.zeroOutDate(new Date(date.getTime() + 1));
                    if(roundedDate < $scope.startDate)
                        return $scope.startDate;
                    else
                        return roundedDate
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
                    var queryData = [];
                    var i = 0;
                    var rawLength = rawData.length;

                    // If we have no values, use our dates if they existed or now.
                    if (rawData.length === 0) {
                        rawData[0] = {
                            date: $scope.startDate || new Date(),
                            count: 0
                        }
                        rawLength = 1;
                    }
                    // If we have only 1 value, create a range for it.
                    if (rawData.length === 1) {
                        rawData[1] = {
                            // Use a time just within our end date, if available, so it fits in a bucket.
                            date: new Date($scope.endDate.getTime() - 1) || rawData[0].date,
                            count: 0
                        }
                        rawLength = 2;
                    }

                    // Setup the data buckets for them.
                    // Determine the number of hour buckets along with the start and end dates for our buckets.
                    // var startDate = new Date(Date.UTC(rawData[0].year, rawData[0].month - 1, rawData[0].day, rawData[0].hour));
                    // var endDate = new Date(Date.UTC(rawData[rawLength - 1].year, rawData[rawLength - 1].month - 1,
                    // 	rawData[rawLength - 1].day, rawData[rawLength - 1].hour));
                    var startDate = $scope.zeroOutDate($scope.startDate || rawData[0].date);
                    var endDate = $scope.zeroOutDate($scope.endDate  || rawData[rawData.length - 1].date);

                    var numBuckets = Math.ceil(Math.abs(endDate - startDate) / $scope.millisMultiplier);
                    var startTime = startDate.getTime();

                    // Initialize our time buckets.
                    for (i = 0; i < numBuckets; i++) {
                        // Calculate the start date for a bucket (e.g., 01:00, 02:00) using the millisMultiplier.  
                        // Also calculate the temporal midpoint of that bucket by adding 1/2 the multiplier to get the
                        // point on the timeline at which we want to display the count for that time bucket.
                        // For the 01:00 to 01:59 time bucket, we want to display the aggregate value at the
                        // perceived center of the bucket, 01:30, on the timeline graph.
                        var bucketGraphDate = new Date(startTime + ($scope.millisMultiplier * i));
                        queryData[i] = {
                            date: bucketGraphDate,
                            value: 0
                        }
                    }

                    // Fill our rawData into the appropriate hour buckets.
                    var resultDate;
                    for (i = 0; i < rawLength; i++) {
                        resultDate = new Date(rawData[i].date);
                        queryData[Math.floor(Math.abs(resultDate - startDate) / $scope.millisMultiplier)].value = rawData[i].count;
                    }

                    data.push({
                        name: 'Total',
                        type: 'bar',
                        color: '#39b54a',
                        data: queryData
                    });

                    return data;
                };

                /**
                 * Adds the timeseries analysis to the data to be graphed.
                 * @param timelineData an array of {date: Date(...), value: n} objects, one for each day
                 * @param graphData the array of objects that will be graphed
                 */
                $scope.addTimeSeriesAnalysis = function(timelineData, graphData) {
                    // If OpenCPU isn't available, then just return without doing anything.
                    if (!ocpu.connected) {
                        return;
                    }

                    $scope.addStl2TimeSeriesAnalysis(timelineData, graphData);
                };

                $scope.runMMPP = function() {
                    if (!ocpu.connected) {
                        return;
                    }
                    $scope.addMmppTimeSeriesAnalysis($scope.primarySeries.data, $scope.data);
                };

                $scope.addMmppTimeSeriesAnalysis = function(timelineData, graphData) {
                    // The MMPP analysis needs hourly data
                    if ($scope.granularity !== HOUR) {
                        return;
                    }
                    // The MMPP code wants a matrix of the counts, with each row being an hour of
                    // the day and each column being a day. Depending on the dataset, the results from the
                    // dataset may start in the middle of the first day. Missing data should be encoded as -1

                    var timelineMatrix = [];
                    for (var i = 0; i < 24; ++i) {
                        timelineMatrix[i] = [];
                    }
                    var day = 0; var hour = 0;
                    for (day = 0; day * 24 < timelineData.length; ++day) {
                        for (hour = 0; hour < 24; ++hour) {
                            var index = day * 24 + hour;
                            if (index < timelineData.length) {
                                timelineMatrix[hour][day] = timelineData[day*24 + hour].value;
                            } else {
                                timelineMatrix[hour][day] = -1;
                            }
                        }
                    }
                    var req = ocpu.rpc("nsensorMMPP", {
                        N: timelineMatrix,
                        ITER: [50, 10]
                    }, function(output) {
                        var probability = _.map(timelineData, function(it, i) { return {date: it.date, value: (output.Z[i]*100)};});
                        graphData.push({
                            name: 'Event Probability',
                            type: 'bar',
                            color: '#000000',
                            data: probability
                        });
                        $scope.$apply(function() {
                            $scope.eventProbabilitiesDisplayed = true;
                        });
                    }).fail(function (output) {
                        // If the request fails, then just update.
                        $scope.$apply();
                    });

                };

                $scope.addStl2TimeSeriesAnalysis = function(timelineData, graphData) {
                    // The analysis code just wants an array of the counts
                    var timelineVector = _.map(timelineData, function(it) {return it.value});

                    var periodLength = 1;
                    var seasonWindow = 1;
                    var trendWindow = 1;
                    if ($scope.granularity === DAY) {
                        // At the day granularity, look for weekly patterns
                        periodLength = 7;
                        seasonWindow = 31;
                        trendWindow = 41;
                    } else if ($scope.granularity === HOUR) {
                        // At the hourly granularity, look for daily patterns
                        periodLength = 24;
                        seasonWindow = 24*7*2;
                        trendWindow = 24*30;
                    }
                    var req = ocpu.rpc("nstl2",{
                        x : timelineVector,
                        "n.p": periodLength, // specifies seasonal periodicity
                        "t.degree": 2, "t.window": 41, // trend smoothing parameters
                        "s.window": seasonWindow, "s.degree": 2, // seasonal smoothing parameters
                        outer: 10 // number of robustness iterations
                    }, function(output){
                        // Square the trend data so that it is on the same scale as the counts
                        var trend = _.map(timelineData, function(it, i) { return {date: it.date, value: output[i].trend};});
                        graphData.push({
                            name: 'Trend',
                            type: 'line',
                            color: '#ff7f0e',
                            data: trend
                        });
                        var seasonal = _.map(timelineData, function(it, i) { return {date: it.date, value: output[i].seasonal};});
                        graphData.push({
                            name: 'Seasonal',
                            type: 'line',
                            color: '#3333C2',
                            data: seasonal
                        });
                        // Square the remainder data so that it is on the same scale as the counts
                        var remainder = _.map(timelineData, function(it, i) { return {date: it.date, value: output[i].remainder};});
                        graphData.push({
                            name: 'Remainder',
                            type: 'bar',
                            color: '#C23333',
                            data: remainder
                        });
                        $scope.$apply();
                    }).fail(function() {
                        // If the request fails, then just update.
                        $scope.$apply();
                    });
                };

                /**
                 * Clears the timeline brush and filter.
                 * @method clearBrush
                 */
                $scope.clearBrush = function () {
                    XDATA.activityLogger.logUserActivity('TimelineSelector - Clear temporal filter', 'remove_visual_filter',
                        XDATA.activityLogger.WF_GETDATA);
                    XDATA.activityLogger.logSystemActivity('TimelineSelector - Removing Neon filter');

                    $scope.brush = [];
                    $scope.messenger.removeFilter($scope.filterKey);
                };

                // Update the millis multipler when the granularity is changed.
                $scope.$watch('granularity', function (newVal, oldVal) {
                    if (newVal && newVal !== oldVal) {
                        XDATA.activityLogger.logUserActivity('TimelineSelector - Change timeline resolution', 'define_axes',
                            XDATA.activityLogger.WF_CREATE,
                            {
                                "resolution": newVal
                            });
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
                            var newBrushStart = $scope.roundDownBucket($scope.brush[0]);
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
                        XDATA.activityLogger.logUserActivity('TimelineSelector - Create/Replace temporal filter', 'execute_visual_filter',
                        XDATA.activityLogger.WF_GETDATA);
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

                        XDATA.activityLogger.logSystemActivity('TimelineSelector - Create/Replace neon filter');

                        // Because the timeline ignores its own filter, we just need to update the
                        // chart times and total when this filter is applied
                        $scope.messenger.replaceFilter($scope.filterKey, filter, $scope.updateChartTimesAndTotal());
                    }
                }, true);

                // Wait for neon to be ready, the create our messenger and intialize the view and data.
                neon.ready(function () {
                    $scope.initialize();
                });

            }
        };
    }]);
