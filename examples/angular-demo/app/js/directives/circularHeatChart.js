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
 * This Angualr JS directive creates a D3JS circular heat chart for hours within the days of the week and adds
 * that chart to the DOM.  This directive binds its display data to the an number array variable specified 
 * by the cell-values attribute.
 *
 * @example
 *    &lt;circular-heat-chart cell-values="data"&gt;&lt;/circular-heat-chart&gt;<br>
 *    &lt;div circular-heat-chart cell-values="data"&gt;&lt;/div&gt;
  *
 * @see neonDemo.charts.circularHeatChart
 * @class neonDemo.directives.circularHeatChart
 * @constructor
 */
angular.module('circularHeatChartDirective', []).directive('circularHeatChart', function() {

    var HOURS_IN_WEEK = 168;
    var HOURS_IN_DAY = 24;

	return {
		restrict: 'EA',
		scope: {
            cellValues: '='
        },
		link: function($scope, element, attrs) {
            element.addClass('circularheatchart');

            // Initialize our cell data.
            $scope.data = new Array(HOURS_IN_WEEK);
            for (var i = 0; i < HOURS_IN_WEEK; i++) {
                $scope.data[i] = 0;
            }

            // Initialize the chart.
            $scope.chart = new circularHeatChart(element[0])
                .segmentHeight(20)
                .innerRadius(20)
                .numSegments(24)
                .radialLabels(["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"])
                .segmentLabels(["12am", "1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm", "9pm", "10pm", "11pm"])
                .margin({top: 20, right: 20, bottom: 20, left: 20});

            // Render an initial empty view.
            $scope.chart.render($scope.data);

            // If our data updates, reset our internal value fields and render the new view.
            $scope.$watch('cellValues', function(newVal) {
                if (newVal) {
                    var length = newVal.length || 0;
                    if (length > HOURS_IN_WEEK) {
                        length = HOURS_IN_WEEK;
                    }

                    for (var i = 0; i < length; i++) {
                        $scope.data[i] = newVal[i];
                    }

                    $scope.chart.render($scope.data);
                }
            }, true);
        }
	}
});

