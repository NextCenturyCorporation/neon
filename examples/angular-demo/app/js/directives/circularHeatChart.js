var databaseConfig = angular.module('circularHeatChartDirective', []);

databaseConfig.directive('circularHeatChart', function() {

    var HOURS_IN_WEEK = 168;
    var HOURS_IN_DAY = 24;

	return {
		restrict: 'E',
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

                    for (var i = 0; i < length; i++) {
                        $scope.data[i] = newVal[i];
                    }

                    $scope.chart.render($scope.data);
                }
            }, true);
        }
	}
});

