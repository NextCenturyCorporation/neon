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
 * This Angualr JS directive creates a D3JS timeline chart that includes a brushing tool to facilitate the selection of
 * of data based on a date range.
 *
 * @example
 *    &lt;timeline-selector-chart cell-values="data"&gt;&lt;/timeline-selector&gt;<br>
 *    &lt;div timeline-selector-chart cell-values="data"&gt;&lt;/div&gt;
 *
 * @see neonDemo.charts.timelineSelectorChart
 * @class neonDemo.directives.timelineSelectorChart
 * @constructor
 */
angular.module('timelineSelectorChartDirective', []).directive('timelineSelectorChart', function () {

    return {
        restrict: 'EA',
        scope: {
            timelineData: '=',
            timelineBrush: '=',
            extentDirty: '=',
            collapsed: '=',
            primarySeries: '=',
            granularity: '='
        },
        link: function ($scope, element, attrs) {

            // Initialize the chart.
            $scope.chart = new charts.TimelineSelectorChart(element[0]);

            // Add a brush handler.
            $scope.chart.addBrushHandler(function (data) {
                // Wrap our data change in $apply since this is fired from a D3 event and outside of
                // angular's digest cycle.
                $scope.$apply(function () {
                    $scope.timelineBrush = data;
                })
            });

            // Render an initial empty view.
            $scope.chart.render([]);

            // If our data updates, reset our internal value fields and render the new view
            // and clear the brush.
            $scope.$watch('timelineData', function (newVal) {
                if (newVal && (newVal.length > 0)) {
                    $scope.chart.updateGranularity($scope.granularity);
                    $scope.chart.render(newVal);
                    $scope.chart.renderExtent($scope.timelineBrush);
                }
            }, true);

            $scope.$watch('timelineBrush', function (newVal) {
                if (newVal && newVal.length === 0) {
                    $scope.chart.clearBrush();
                }
            });

            $scope.$watch('extentDirty', function(newVal) {
                if (newVal) {
                    $scope.extentDirty = false;
                    $scope.chart.renderExtent($scope.timelineBrush);
                }
            });

            $scope.$watch('collapsed', function(newVal) {
                if (typeof newVal !== "undefined") {
                    $scope.chart.render($scope.timelineData);
                    $scope.chart.renderExtent($scope.timelineBrush);
                }
            });

            $scope.$watch('primarySeries', function(newVal) {
                if (newVal) {
                    $scope.chart.updatePrimarySeries(newVal);
                    $scope.chart.render($scope.timelineData);
                    $scope.chart.renderExtent($scope.timelineBrush);
                }
            });
        }
    }
});

