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
 * This Angular JS directive adds a circular heat map to the DOM and drives the visualization data from
 * whatever database and table are currently selected in Neon.  This directive pulls the current
 * Neon connection from a connection service and listens for
 * neon system events (e.g., data tables changed) to determine when to update its visualization
 * by issuing a Neon query for aggregated time data.
 * 
 * @example
 *    &lt;circular-heat-form&gt;&lt;/circular-heat-form&gt;<br>
 *    &lt;div circular-heat-form&gt;&lt;/div&gt;
 * 
 * @class neonDemo.directives.circularHeatForm
 * @constructor
 */
angular.module('circularHeatFormDirective', []).directive('circularHeatForm', ['ConnectionService',
	function(connectionService) {

	return {
		templateUrl: 'partials/circularHeatForm.html',
		restrict: 'EA',
		scope: {
			filterKey: '='
		},
		controller: function($scope) {

			/** 
			 * Sets the name of the date field to pull from the current dataset.
			 * @method setDateField
			 */
			$scope.setDateField = function(field) {
				$scope.dateField = field;
			};

			/** 
			 * Returns the name of the date field used to pull from time data from the current dataset.
			 * @method getDateField
			 */
			$scope.getDateField = function() {
				return $scope.dateField;
			};
		},
		link: function($scope, element, attr) {

			$scope.days = [
				{name: "Sunday", count: 0},
				{name: "Monday", count: 0},
				{name: "Tuesday", count: 0},
				{name: "Wednesday", count: 0},
				{name: "Thursday", count: 0},
				{name: "Friday", count: 0},
				{name: "Saturday", count: 0}
			];
			$scope.timeofday = [
				{name: "morning", count: 0},
				{name: "afternoon", count: 0},
				{name: "evening", count: 0},
				{name: "night", count: 0}
			];
			$scope.maxDay = "";
			$scope.maxTime = "";

			element.addClass('circularheatform');

			var HOURS_IN_WEEK = 168;
			var HOURS_IN_DAY = 24;

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
			 * Event handler for filter changed events issues over Neon's messaging channels.
			 * @param {Object} message A Neon filter changed message.
			 * @method onFiltersChanged
			 * @private
			 */ 
			var onFiltersChanged = function(message) {
				$scope.queryForChartData();
			};

			/**
			 * Event handler for dataset changed events issues over Neon's messaging channels.
			 * @param {Object} message A Neon dataset changed message.
			 * @param {String} message.database The database that was selected.
			 * @param {String} message.table The table within the database that was selected.
			 * @method onDatasetChanged
			 * @private
			 */ 
			var onDatasetChanged = function(message) {
				$scope.databaseName = message.database;
				$scope.tableName = message.table;
				$scope.queryForChartData();
			};

			/**
			 * Triggers a Neon query that will aggregate the time data for the currently selected dataset.
			 * @method queryForChartData
			 */
			$scope.queryForChartData = function() {
				// TODO: Decide how to pass in field mappings.  We can do this through a controller or the
				// connection service or some mapping service.  Two example below, one commented out.
				//var dateField = $scope.getDateField();
				var dateField = connectionService.getFieldMapping($scope.databaseName, $scope.tableName, "date");
				dateField = dateField.mapping;

				if (!dateField) {
					$scope.updateChartData({data: []});
					return;
				}

				//TODO: NEON-603 Add support for dayOfWeek to query API
				var groupByDayClause = new neon.query.GroupByFunctionClause('dayOfWeek', $scope.dateField, 'day');
				var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, $scope.dateField, 'hour');

				var query = new neon.query.Query()
					.selectFrom($scope.databaseName, $scope.tableName)
					.groupBy(groupByDayClause, groupByHourClause)
					.where($scope.dateField, '!=', null)
					.aggregate(neon.query.COUNT, '*', 'count');

				// Issue the query and provide a success handler that will forcefully apply an update to the chart.
				// This is done since the callbacks from queries execute outside digest cycle for angular.
				// If updateChartData is called from within angular code or triggered by handler within angular,
				// then the apply is handled by angular.  Forcing apply inside updateChartData instead is error prone as it
				// may cause an apply within a digest cycle when triggered by an angular event.
				connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
					$scope.$apply(function(){
						$scope.updateChartData(queryResults);
					});
				});

			};

			/**
			 * Updates the data bound to the heat chart managed by this directive.  This will trigger a change in 
			 * the chart's visualization.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method updateChartData
			 */
			$scope.updateChartData = function(queryResults) {
				$scope.data = $scope.createHeatChartData(queryResults);
			};

			/**
			 * Creates a new data array used to populate our contained heat chart.  This function is used
			 * as or by Neon query handlers.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method createHeatChartData
			 */
			$scope.createHeatChartData = function(queryResults){
				var rawData = queryResults.data;

				var data = [];

				for (var i = 0; i < HOURS_IN_WEEK; i++) {
					data[i] = 0;
				}

				_.each(rawData, function (element) {
					data[(element.day - 1) * HOURS_IN_DAY + element.hour] = element.count;

					// Add count to total for this day of the week.
					$scope.days[element.day-1].count += element.count;

					// Add count to total for this time of day.
					if(element.hour >= 5 && element.hour < 12)
						$scope.timeofday[0].count += element.count;
					else if(element.hour >= 12 && element.hour < 17)
						$scope.timeofday[1].count += element.count;
					else if(element.hour >= 17 && element.hour < 21)
						$scope.timeofday[2].count += element.count;
					else
						$scope.timeofday[3].count += element.count;
				});

				// Find the day with the highest count.
				var maxCount = 0;
				_.each($scope.days, function (day) {
					if(day.count > maxCount){
						maxCount = day.count;
						$scope.maxDay = day.name;
					}
				});

				// Find the time of day with the highest count.
				maxCount = 0;
				_.each($scope.timeofday, function (time) {
					if(time.count > maxCount){
						maxCount = time.count;
						$scope.maxTime = time.name;
					}
				});

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
