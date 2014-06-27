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

// Defaulting the Neon SERVER_URL to be under the neon context on the same host machine.
// If the neon application is loaded elsewhere, this can be changed as in the following example:
// neon.SERVER_URL = "http://localhost:8080/neon"
neon.SERVER_URL = "/neon";

var neonDemo = angular.module('neonDemo', [
	'neonDemo.controllers',
	'neonDemo.services',
	'configurationDirective',
	'filterBuilderDirective',
	'circularHeatChartDirective',
	'circularHeatFormDirective',
	'circularDrilldownFormDirective',
	'barchartDirective',
	'stackedBarchartDirective',
	'fieldSelectorDirective',
	'timelineSelectorChartDirective',
	'timelineSelectorDirective',
	'heatMapDirective',
	'queryResultsTableDirective',
	'tagCloudDirective',
	'linechartDirective'
]);
