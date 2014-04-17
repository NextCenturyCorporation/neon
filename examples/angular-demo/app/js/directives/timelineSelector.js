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
	function(connectionService) {

	return {
		templateUrl: 'partials/timelineSelector.html',
		restrict: 'EA',
		scope: {

		},
		controller: function($scope) {

		},
		link: function($scope, element, attr) {

            // Cache the number of milliseconds in an hour for processing.
            var MILLIS_IN_HOUR = 1000 * 60 * 60;
			element.addClass('timeline-selector');

			/** 
			 * Initializes the name of the date field used to query the current dataset
			 * and the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
				// Defaulting the expected date field to 'date'.
				$scope.dateField = 'date';

				// Default our time data to an empty array.
				$scope.data = [];
				$scope.brush = [];
				$scope.filterKey = neon.widget.getInstanceId("timlineFilter");
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
				$scope.messenger.clearSelection();
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
			var onDatasetChanged = function(message) {
				// Clear our filters against the last table before requesting data.
				$scope.messenger.clearSelection($scope.filterKey);

				$scope.databaseName = message.database;
				$scope.tableName = message.table;
			};

			/**
			 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
			 * @method queryForChartData
			 */
			$scope.queryForChartData = function() {
				$scope.dateField = connectionService.getFieldMapping($scope.database, $scope.tableName, "date");
				$scope.dateField = $scope.dateField.mapping || 'date';

				var yearGroupClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, $scope.dateField, 'year');
				var monthGroupClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, $scope.dateField, 'month');
				var dayGroupClause = new neon.query.GroupByFunctionClause(neon.query.DAY, $scope.dateField, 'day');
				var hourGroupClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, $scope.dateField, 'hour');

				var query = new neon.query.Query()
				    .selectFrom($scope.databaseName, $scope.tableName)
				    .where($scope.dateField, '!=', null)
				    .groupBy(yearGroupClause, monthGroupClause, dayGroupClause, hourGroupClause);

				query.aggregate(neon.query.COUNT, '*', 'count');
				query.aggregate(neon.query.MIN, $scope.dateField, 'date');
				query.sortBy('date', neon.query.ASCENDING);

				connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
					$scope.$apply(function(){
						$scope.brush = [];
						$scope.updateChartData(queryResults);
					});
				});

			};

			/**
			 * Updates the data bound to the chart managed by this directive.  This will trigger a change in 
			 * the chart's visualization.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method updateChartData
			 */
			$scope.updateChartData = function(queryResults) {
				$scope.data = $scope.createTimelineData(queryResults);
			};

			/**
			 * Creates a new data array used to populate our contained timeline.  This function is used
			 * as or by Neon query handlers.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method createTimelineData
			 */
			$scope.createTimelineData = function(queryResults){
				var rawData = queryResults.data;
				var data = [];

				// If we have only 1 value, create a range for it.
				if (rawData.length === 1) {
					rawData[1] = rawData[0]; 
				}

				var rawLength = rawData.length;

                // If we have at least 2 values, setup the data buckets for them.
				if (rawLength > 1) {
					// Determine the number of hour buckets.
					var startDate = new Date(Date.UTC(rawData[0].year, rawData[0].month - 1, rawData[0].day, rawData[0].hour));
					var endDate = new Date(Date.UTC(rawData[rawLength - 1].year, rawData[rawLength - 1].month - 1, 
						rawData[rawLength - 1].day, rawData[rawLength - 1].hour));
					
					var numBuckets = Math.ceil(Math.abs(endDate - startDate) / MILLIS_IN_HOUR) + 1;
					var startTime = startDate.getTime();

					for (var i = 0; i < numBuckets; i++) {
						data[i] = {
							date: new Date(startTime + (MILLIS_IN_HOUR * i)),
							value: 0 
						}
					}

					// Fill our rawData into the appropriate hour buckets.
					var diff = 0;
					var resultDate;
					for (i = 0; i < rawLength; i++) {
						resultDate = new Date(rawData[i].date);
						data[Math.floor(Math.abs(resultDate - startDate) / MILLIS_IN_HOUR)].value = rawData[i].count;
					}
				}
				return data;
			};

			// Watch for brush changes and set the appropriate neon filter.
			$scope.$watch('brush', function(newVal) {
				// If we have a new value and a messenger is ready, set the new filter.
				if (newVal && $scope.messenger && connectionService.getActiveConnection()) {

					if (newVal === undefined || (newVal.length < 2) || (newVal[0].getTime() === newVal[1].getTime())) {
						$scope.messenger.clearSelection($scope.filterKey);
					} else {
						// Since we created our time buckets with times representing the start of an hour, we need to add an hour
						// to the time representing our last selected hour bucket to get all the records that occur in that hour.
						var startFilterClause = neon.query.where($scope.dateField, '>=', newVal[0]);
			            var endFilterClause = neon.query.where($scope.dateField, '<', new Date(newVal[1].getTime() + MILLIS_IN_HOUR));
			            var clauses = [startFilterClause, endFilterClause];
			            var filterClause = neon.query.and.apply(this, clauses);
			            var filter = new neon.query.Filter().selectFrom($scope.databaseName, $scope.tableName).where(filterClause);

			            $scope.messenger.replaceSelection($scope.filterKey, filter);
					}
				}
			}, true);

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
