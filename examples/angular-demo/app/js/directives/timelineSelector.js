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
 * @class neonDemo.directives.circularHeatForm
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

			element.addClass('timeline-selector');

			// TODO: Default to [] and remove sample data when finally hooked up.
			$scope.data = [{
		        date: new Date(Date.now()),
		        value: 0
		    },{
		        date: new Date(Date.now() + (31536000000 / 2)),
		        value: 50
		    },{
		        date: new Date(Date.now() + 31536000000),
		        value: 50
		    }];

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
			 * Initializes the arrays and variables used to track the most active day of the week and time of day.
			 * @method initDayTimeArrays
			 */
			$scope.initDayTimeArrays = function(){
				// TODO: Everything.
			}

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
				// TODO: Decide how to pass in field mappings.  We can do this through a controller or the
				// connection service or some mapping service.  Two example below, one commented out.
				//var dateField = $scope.getDateField();
				// var dateField = connectionService.getFieldMapping($scope.databaseName, $scope.tableName, "date");
				// var yAxis = connectionService.getFieldMapping($scope.databaseName, $scope.tableName, "y-axis");
				// dateField = dateField.mapping;

				// if (!dateField) {
				// 	$scope.updateChartData({data: []});
				// 	return;
				// } else {
				// 	$scope.dateField = dateField;
				// }

				// //TODO: NEON-603 Add support for dayOfWeek to query API
				// var groupByDayClause = new neon.query.GroupByFunctionClause('dayOfWeek', $scope.dateField, 'day');
				// var groupByHourClause = new neon.query.GroupByFunctionClause(neon.query.HOUR, $scope.dateField, 'hour');

				// var query = new neon.query.Query()
				// 	.selectFrom($scope.databaseName, $scope.tableName)
				// 	.groupBy(groupByDayClause, groupByHourClause)
				// 	.where($scope.dateField, '!=', null)
				// 	.aggregate(neon.query.COUNT, '*', 'count');

				// // Issue the query and provide a success handler that will forcefully apply an update to the chart.
				// // This is done since the callbacks from queries execute outside digest cycle for angular.
				// // If updateChartData is called from within angular code or triggered by handler within angular,
				// // then the apply is handled by angular.  Forcing apply inside updateChartData instead is error prone as it
				// // may cause an apply within a digest cycle when triggered by an angular event.
				// connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
				// 	$scope.$apply(function(){
				// 		$scope.updateChartData(queryResults);
				// 	});
				// });

			};

			/**
			 * Updates the data bound to the chart managed by this directive.  This will trigger a change in 
			 * the chart's visualization.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method updateChartData
			 */
			$scope.updateChartData = function(queryResults) {
				$scope.data = $scope.createHeatChartData(queryResults);
			};

			/**
			 * Creates a new data array used to populate our contained timeline.  This function is used
			 * as or by Neon query handlers.
			 * @param {Object} queryResults Results returned from a Neon query.
			 * @param {Array} queryResults.data The aggregate numbers for the heat chart cells.
			 * @method createHeatChartData
			 */
			$scope.createTimelineData = function(queryResults){
				var rawData = queryResults.data;

				var data = [];
                // TODO: Everything.

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
