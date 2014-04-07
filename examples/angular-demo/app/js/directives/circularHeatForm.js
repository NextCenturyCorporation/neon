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
angular.module('circularHeatFormDirective', []).directive('circularHeatForm', function() {

	return {
		templateUrl: 'partials/circularHeatForm.html',
		restrict: 'E',
		scope: {
			//serverUrl: 'localhost',
			//databaseName: 'mydb',
			clientId: '&',
			//tableName: 'sample',
			filterKey: '=',
			//dateField: 'time'
		},
		link: function($scope, element, attr) {
			element.addClass('circularheatform');

			var HOURS_IN_WEEK = 168;
			var HOURS_IN_DAY = 24;

			$scope.initialize = function() {
		    	// Not sure we want to change this in a single page app.
		        //neon.query.SERVER_URL = $("#neon-server").val();
		        
		        // TODO: Is this necessary?  Need to remove or convert to use new API.
		        // Stubbing out for now.
		        // clientId = neon.query.getInstanceId('neon.circularheat');
		        $scope.clientId = 'neon.circularheat.example';
		        $scope.serverUrl = 'localhost';
		        $scope.databaseName = 'mydb';
		        $scope.tableName = 'sample';
		        $scope.dateField = 'time';
		        // $scope.messenger.registerForNeonEvents({
		        //     activeDatasetChanged: onDatasetChanged,
		        //     activeConnectionChanged: onConnectionChanged,
		        //     filtersChanged: onFiltersChanged
		        // });

		        // TODO: Remove; no longer in API.
		        //neon.toggle.createOptionsPanel("#options-panel");
		        $scope.initChart();
		        //$scope.restoreState();
		    };

			$scope.initChart = function() {
		        $scope.queryForChartData();
		    }

		    $scope.onFiltersChanged = function(message) {
		        $scope.queryForChartData();
		    }

		    $scope.onConnectionChanged = function(id){
		        connectionId = id;
		    }

		    $scope.onDatasetChanged = function(message) {
		        $scope.databaseName = message.database;
		        $scope.tableName = message.table;
		        neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
		            filterKey = filterResponse;
		        });
		        neon.query.getFieldNames(connectionId, databaseName, tableName, neon.widget.CIRCULAR_HEAT, populateFromColumns);
		    }

		    // TODO: Remove or reimplement; these methods are no longer in neon API.
		    // $scope.populateFromColumns = function(data) {
		    //     var element = new neon.dropdown.Element("date", "temporal");
		    //     neon.dropdown.populateAttributeDropdowns(data, element, queryForChartData);
		    // }

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

		        //var stateObject = buildStateObject(dateField, query);
		        // neon.query.Connection.executeQuery($scope.connectionId, query, $scope.drawChart);

		        var connection = new neon.query.Connection();
		        connection.connect(neon.query.Connection.MONGO, "localhost");
		        connection.use($scope.databaseName);
		        connection.executeQuery(query, $scope.drawChart);
		        // TODO: Remove or rewrite.  This method no longer in API.
		        // neon.query.saveState(clientId, stateObject);
		    }

		    $scope.drawChart = function(queryResults) {
		        $scope.data = $scope.createHeatChartData(queryResults);
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

		    $scope.getDateField = function() {
		    	return $scope.dateField;
		        //return $('#date option:selected').val();
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

		    // TODO: Remove or rewrite.  These methods are no longer in the Neon API and we're ditching the 
		    // column pull-down menus.
		    // $scope.restoreState = function() {
		    //     neon.query.getSavedState(clientId, function (data) {
		    //         filterKey = data.filterKey;
		    //         connectionId = data.connectionId;
		    //         if(!filterKey || !connectionId){
		    //             return;
		    //         }
		    //         databaseName = data.filterKey.dataSet.databaseName;
		    //         tableName = data.filterKey.dataSet.tableName;
		    //         var element = new neon.dropdown.Element("date", "temporal");
		    //         neon.dropdown.populateAttributeDropdowns(data.columns, element, queryForChartData);
		    //         neon.dropdown.setDropdownInitialValue("date", data.selectedField);
		    //         neon.query.executeQuery(connectionId, data.query, drawChart);
		    //     });
		    // }

			neon.ready(function () {

			    $scope.messenger = new neon.eventing.Messenger();
			    // scope.clientId;
			    // scope.connectionId;

			    $scope.initialize();   
			});

		}
	}
});
