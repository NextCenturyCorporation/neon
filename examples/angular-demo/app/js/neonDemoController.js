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

angular.module('neonDemo.controllers', []).controller('neonDemoController', ['$scope', 'FilterCountService',
	function($scope, filterCountService) {

		$scope.seeData = false;
        $scope.filterCount = 0;
        $scope.filterCountService = filterCountService;

		// Simple handler used by the app's main index page to determine when the user
		// wants to "see data" or not.  Essentially, this is used to sync an angular scope
		// variable with the collapsed state of a div whose view is managed by Bootstrap
		// hooks.
		$scope.toggleSeeData = function() {
			$scope.seeData = !$scope.seeData;
		}

        $scope.$watch('filterCountService.getCount()', function(count) {
            $scope.filterCount = count;
        })


	}]);
