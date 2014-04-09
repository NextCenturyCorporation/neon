/* global neon */
/* global charts */
'use strict';


var barchart = angular.module('barchartDirective', []);

barchart.directive('barchart', ['ConnectionService', function(connectionService) {
	var COUNT_FIELD_NAME = 'Count';

	var link = function($scope, el, attr) {

		var messenger = new neon.eventing.Messenger();
		$scope.fields = [];
		$scope.xAxisSelect = $scope.fields[0] ? $scope.fields[0] : '';

		var COUNT_FIELD_NAME = 'Count';
		// var messenger = new neon.eventing.Messenger();
		var clientId;

		var initialize = function() {
			//determine fields
			console.log($scope);

			$scope.attrX = ($scope.attrX ? $scope.attrX : 'foo') ;
			$scope.attrY = ($scope.attrY ? $scope.attrY : 'bar');

			drawChart();

			$scope.messenger.events({
				activeDatasetChanged: onDatasetChanged,
				filtersChanged: onFiltersChanged
			});
		};

		var onFiltersChanged = function(message) {
			$scope.queryForData();
		};

		var onDatasetChanged = function(message) {
			$scope.databaseName = message.database;
			$scope.tableName = message.table;


			queryForData();
		};

		var queryForData = function() {
			var query = new neon.query.Query()
			.selectFrom($scope.databaseName, $scope.tableName)
			.where($scope.attrX, '!=', null)
			.groupBy($scope.attrX);

			if($scope.attrY) {
				query.aggregate(neon.query.SUM, $scope.attrY, $scope.attrY);
			} else {
				query.aggregate(neon.query.COUNT, '*', COUNT_FIELD_NAME);
			}

			connectionService.getActiveConnection().executeQuery(query, function(queryResults) {
				$scope.$apply(function(){
					doDrawChart(queryResults);
				});
			});
		};

		/**
		 * Redraws the chart based on the user selected attribtues
		 * @method drawChart
		 */
		var drawChart = function() {
			doDrawChart({data: []});
		};

		var doDrawChart = function(data) {
			if (!$scope.attrY) {
				$scope.attrY = COUNT_FIELD_NAME;
			}

			var opts = { "data": data.data, "x": $scope.attrX, "y": $scope.attrY, responsive: true};

			//FIXME need to update... not recreate
			var chart = new charts.BarChart(el[0], '.barchart', opts).draw();
		};

		neon.ready(function () {
			$scope.messenger = new neon.eventing.Messenger();
			initialize();
		});
	};

	return {
		templateUrl: 'partials/barchart.html',
		restrict: 'E',
		scope: {
			attrX: '=',
			attrY: '='
		},
		link: link
	};
}]);
