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
 * This Angular JS directive adds a timeline.
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
			filterKey: '='
		},
		controller: function($scope) {

		},
		link: function($scope, element, attr) {

            // Cache the number of milliseconds in an hour for processing.
            var MILLIS_IN_HOUR = 1000 * 60 * 60;

			element.addClass('timeline-selector');

			// Default our time data to an empty array.
			$scope.data = [];

			/** 
			 * Initializes the name of the date field used to query the current dataset
			 * and the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
				// Defaulting the expected date field to 'time'.
				$scope.dateField = 'time';

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
				$scope.databaseName = message.database;
				$scope.tableName = message.table;
			};

			/**
			 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
			 * @method queryForChartData
			 */
			$scope.queryForChartData = function() {
				var timeField = connectionService.getFieldMapping($scope.database, $scope.tableName, "date");
				timeField = timeField.mapping || 'time';

				var yearGroupClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, timeField, 'year');
				var monthGroupClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, timeField, 'month');
				var dayGroupClause = new neon.query.GroupByFunctionClause(neon.query.DAY, timeField, 'day');
				var hourGroupClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, timeField, 'hour');

				var query = new neon.query.Query()
				    .selectFrom($scope.databaseName, $scope.tableName)
				    .where(timeField, '!=', null)
				    .groupBy(yearGroupClause, monthGroupClause, dayGroupClause, hourGroupClause);

				query.aggregate(neon.query.COUNT, '*', 'count');
				query.aggregate(neon.query.MIN, timeField, 'date');
				query.sortBy('date', neon.query.ASCENDING);

				connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
					$scope.$apply(function(){
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
					// var startDate = new Date(rawData[0].date);
					// var endDate = new Date(rawData[rawData.length - 1].date);
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

			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.messenger = new neon.eventing.Messenger();
				$scope.initialize();
			});

		}
	};
}]);
