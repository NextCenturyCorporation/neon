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
var fieldSelector = angular.module('fieldSelectorDirective', []);

fieldSelector.directive('fieldselector', ['ConnectionService', function(connectionService) {
	var link = function($scope, el, attr) {
		var messenger = new neon.eventing.Messenger();
		$scope.database = '';
		$scope.tableName = '';
		$scope.fields = [];
		$scope.tmp = "";

		//add select
		//el.append('<select><option>Boo</option></select>');

		var initialize = function() {
			messenger.events({
				activeDatasetChanged: onDatasetChanged
			});
		};

		var onDatasetChanged = function(message) {
			$scope.database = message.database;
			$scope.table = message.table;

            connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);
			connectionService.getActiveConnection().getFieldNames($scope.table, function(results) {
				XDATA.activityLogger.logSystemActivity('FieldSelector - query for available fields');
				$scope.$apply(function() {
					$scope.fields = results;
					XDATA.activityLogger.logSystemActivity('FieldSelector - received available fields');
				});
			});

			if($scope.defaultMapping) {
                connectionService.loadMetadata(function() {
                    $scope.targetVar = connectionService.getFieldMapping($scope.defaultMapping);
                });
			}
		};

		var onSelectionChange = function(newVal, oldVal) {
			XDATA.activityLogger.logUserActivity('FieldSelector - user changed a field selection', 'define_axes',
                XDATA.activityLogger.WF_CREATE,
                {
                	"field": $scope.labelText,
                    "to": newVal,
                    "from": oldVal
                });
		};

		$scope.$watch("targetVar", onSelectionChange);

		// Wait for neon to be ready, the create our messenger and intialize the view and data.
		neon.ready(function () {
			initialize();
		});
	};

	return {
		template: '<label>{{labelText}}</label><select ng-model="targetVar" ng-options="field for field in fields" class="form-control"></select>',
		restrict: 'E',
		scope: {
			targetVar: '=',
			labelText: '=',
			defaultMapping: '='
		},
		link: link
	};
}]);
