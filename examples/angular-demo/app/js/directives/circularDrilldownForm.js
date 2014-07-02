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
angular.module('circularDrilldownFormDirective', []).directive('circularDrilldownForm', ['ConnectionService',
	function(connectionService) {

	return {
		templateUrl: 'partials/circularDrilldownForm.html',
		restrict: 'EA',
		scope: {
		},
		controller: function($scope) {
		},
		link: function($scope, element, attr) {

			/** 
			 * Initializes the Neon Messenger used to monitor data change events.
			 * @method initialize
			 */
			$scope.initialize = function() {
				var dataInject = new HeatChartNeonData(connectionService);
        		var heatChart = new HeatChartApp('year', new Date(2014, 0) ,dataInject, element[0]);  
			};



			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
