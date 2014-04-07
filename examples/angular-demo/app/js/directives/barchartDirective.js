var barchart = angular.module('barchartDirective', []);

barchart.directive('barchart', function() {
	var elm;
	var link = function($scope, el, attr) {
		var messenger = new neon.eventing.Messenger();
		$scope.fields = [];
		$scope.xAxisSelect = $scope.fields[0] ? $scope.fields[0] : '';

		var COUNT_FIELD_NAME = 'Count';
		// var messenger = new neon.eventing.Messenger();
		var clientId;

		var initialize = function() {
			// neon.query.SERVER_URL = $("#neon-server").val();
			// clientId = neon.query.getInstanceId('neon.barchart');
			// neon.toggle.createOptionsPanel("#options-panel");
			drawChart();
			// messenger.registerForNeonEvents({
			// 	activeDatasetChanged: function (message) {
			// 		neon.chartWidget.onActiveDatasetChanged(message, drawChart, neon.widget.BARCHART);
			// 	},
			// 	activeConnectionChanged: neon.chartWidget.onConnectionChanged,
			// 	filtersChanged: drawChart
			// });

			// restoreState();
		};

		/**
		 * Redraws the chart based on the user selected attribtues
		 * @method drawChart
		 */
		var drawChart = function() {
			var xAttr = 'foo';//neon.chartWidget.getXAttribute();
			var yAttr = 'bar';//neon.chartWidget.getYAttribute();
			// if (!xAttr) {
				doDrawChart({data: [{foo:1, bar:10},{foo:2,bar:5},{foo:3, bar:15},{foo:4, bar:23}]});
				// return;
			// }


			// var query = new neon.query.Query()
			// 	.selectFrom(neon.chartWidget.getDatabaseName(), neon.chartWidget.getTableName())
			// 	.where(xAttr, '!=', null).groupBy(xAttr);

			// if (yAttr) {
			// 	query.aggregate(neon.query.SUM, yAttr, yAttr);
			// }
			// else {
			// 	query.aggregate(neon.query.COUNT, '*', COUNT_FIELD_NAME);
			// }

			// var stateObject = buildStateObject(query);
			// neon.query.executeQuery(neon.chartWidget.getConnectionId(), query, doDrawChart);
			// neon.query.saveState(clientId, stateObject);
		}

		var doDrawChart = function(data) {
			$('#chart').empty();
			// var xAttr = neon.chartWidget.getXAttribute();
			// var yAttr = neon.chartWidget.getYAttribute();
			var xAttr = 'foo';//neon.chartWidget.getXAttribute();
			var yAttr = 'bar';//neon.chartWidget.getYAttribute();

			// if (!yAttr) {
			// 	yAttr = COUNT_FIELD_NAME;
			// }

			//We need this because we set a window listener which holds a reference to old barchart objects.
			//We should really only use one barchart object, but that will be fixed as part of NEON-294
			$(window).off("resize");
			var opts = { "data": data.data, "x": xAttr, "y": yAttr, responsive: true};

			var chart = new charts.BarChart(el[0], '.barchart', opts).draw();
		}

		initialize();







		// function buildStateObject(query) {
		// 	return {
		// 		connectionId: neon.chartWidget.getConnectionId(),
		// 		filterKey: neon.chartWidget.getFilterKey(),
		// 		columns: neon.dropdown.getFieldNamesFromDropdown("x"),
		// 		xValue: neon.chartWidget.getXAttribute(),
		// 		yValue: neon.chartWidget.getYAttribute(),
		// 		query: query
		// 	};
		// }

		// function restoreState() {
		// 	neon.query.getSavedState(clientId, function (data) {
		// 		neon.chartWidget.onConnectionChanged(data.connectionId);
		// 		neon.chartWidget.setFilterKey(data.filterKey);
		// 		neon.chartWidget.setDatabaseName(data.filterKey.dataSet.databaseName);
		// 		neon.chartWidget.setTableName(data.filterKey.dataSet.tableName);

		// 		var elements = [new neon.dropdown.Element("x", ["text", "numeric"]), new neon.dropdown.Element("y", "numeric")];
		// 		neon.dropdown.populateAttributeDropdowns(data.columns, elements, drawChart);
		// 		neon.dropdown.setDropdownInitialValue("x", data.xValue);
		// 		neon.dropdown.setDropdownInitialValue("y", data.yValue);
		// 		neon.query.executeQuery(neon.chartWidget.getConnectionId(), data.query, doDrawChart);
		// 	});
		// }
	}





	return {
		templateUrl: 'partials/barchart.html',
		restrict: 'E',
		scope: {
		},
		link: link
	}
});
