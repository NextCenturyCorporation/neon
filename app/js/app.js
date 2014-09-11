'use strict';
neon.SERVER_URL = "/neon";


angular.module("memex", [
	'neon.controllers',
	'neon.services',
	'neon.directives',

	'circularHeatChartDirective',
	'circularHeatFormDirective',
	'configurationDirective',
	'filterBuilderDirective',
	'heatMapDirective',
	'queryResultsTableDirective',
	'timelineSelectorDirective',
	'timelineSelectorChartDirective'
])
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