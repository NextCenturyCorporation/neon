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
var neonDemo = angular.module('neonDemo', [
	'neonDemo.controllers',
	'neonDemo.services',
	'configurationDirective',
	'poweredByNeonDirective',
	'filterBuilderDirective',
	'circularHeatChartDirective',
	'circularHeatFormDirective',
	'barchartDirective',
	'stackedBarchartDirective',
	'fieldSelectorDirective',
	'timelineSelectorChartDirective',
	'timelineSelectorDirective',
	'heatMapDirective',
	'queryResultsTableDirective',
	'tagCloudDirective',
	'linechartDirective',
	'numberShortModule'
]);

angular.module('numberShortModule', [])
 	.filter('numberShort', function () {
	    return function(number){
		    if (typeof number !== undefined){
		      var abs = Math.abs(number);
		      if (abs >= Math.pow(10, 12))
		        number = (number / Math.pow(10, 12)).toFixed(1)+"T";
		      else if (abs < Math.pow(10, 12) && abs >= Math.pow(10, 9))
		        number = (number / Math.pow(10, 9)).toFixed(1)+"B";
		      else if (abs < Math.pow(10, 9) && abs >= Math.pow(10, 6))
		        number = (number / Math.pow(10, 6)).toFixed(1)+"M";
		      else if (abs < Math.pow(10, 6) && abs >= Math.pow(10, 3))
		        number = (number / Math.pow(10, 3)).toFixed(1)+"K";
		      else
		      	number = Math.round(number * 100) / 100;
		    }
		    return number
		};
	});