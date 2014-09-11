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
]);