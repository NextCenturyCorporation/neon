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
/**
 * This directive adds a circular heat map to the DOM and drives the visualization data from
 * whatever database and table are currently selected in neon.  This directive accomplishes that
 * by using getting a neon connection from a connection service and listening for 
 * neon system events (e.g., data tables changed).  On these events, it requeries the active
 * connection for data and updates applies the change to its scope.  The contained
 * circular heat map will update as a result.
 * @class neonDemo.directives.circularHeatForm
 * @constructor
 */
angular.module('circularHeatFormDirective', []).directive('circularHeatForm', ['ConnectionService',
	function(connectionService) {

	return {
		templateUrl: 'partials/circularHeatForm.html',
		restrict: 'E',
		scope: {
			filterKey: '=',
		},
		controller: function($scope) {

			$scope.setDateField = function(field) {
				$scope.dateField = field;
			};

			$scope.getDateField = function() {
		    	return $scope.dateField;
		    }
		},
		link: function($scope, element, attr) {
			element.addClass('circularheatform');

			var HOURS_IN_WEEK = 168;
			var HOURS_IN_DAY = 24;

			$scope.initialize = function() {	        
		        // Defaulting the expected date field to 'time'.
		        $scope.dateField = 'time';

		        $scope.messenger.events({
		            activeDatasetChanged: $scope.onDatasetChanged,
		            filtersChanged: $scope.onFiltersChanged
		        });
		    };

		    $scope.onFiltersChanged = function(message) {
		        $scope.queryForChartData();
		    }

		    $scope.onDatasetChanged = function(message) {
		        $scope.databaseName = message.database;
		        $scope.tableName = message.table;
		        $scope.queryForChartData();
		    }

		    $scope.queryForChartData = function() {
		        var dateField = $scope.getDateField();

		        if (!dateField) {
		            $scope.drawChart({data: []});
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

		        //$scope.connection.executeQuery(query, $scope.drawChart);
		        connectionService.getActiveConnection().executeQuery(query, $scope.drawChart);
		    }

		    $scope.drawChart = function(queryResults) {
		        $scope.data = $scope.createHeatChartData(queryResults);
		        $scope.$apply();
		    }

		    $scope.createHeatChartData = function(queryResults){
		        var rawData = queryResults.data;

		        var data = [];

		        for (var i = 0; i < HOURS_IN_WEEK; i++) {
		            data[i] = 0;
		        }

		        _.each(rawData, function (element) {
		            data[(element.day - 1) * HOURS_IN_DAY + element.hour] = element.count;
		        });

		        return data;
		    }

		    $scope.buildStateObject = function(dateField, query) {
		        return {
		            connectionId: connectionId,
		            filterKey: filterKey,
		            columns: neon.dropdown.getFieldNamesFromDropdown("date"),
		            selectedField: dateField,
		            query: query
		        };
		    }

		    // Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
			    $scope.messenger = new neon.eventing.Messenger();
			    $scope.initialize();   
			});

		}
	}
}]);
