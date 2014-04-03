var databaseConfig = angular.module('configurationDirective', []);

databaseConfig.directive('databaseConfig', function() {
	var link = function($scope, el, attr) {
		var messenger = new neon.eventing.Messenger();

		$scope.datastoreSelect = $scope.storeSelect || 'mongo';
		$scope.hostnameInput = $scope.hostName || 'localhost';

		$scope.showDbTable = false;
		$scope.databases = [];
		$scope.dbTables = [];
		$scope.fields = [];

		var connection;

		$scope.connectToDatastore = function() {
			$scope.showDbTable = true;

			connection = new neon.query.Connection();
			connection.connect($scope.datastoreSelect, $scope.hostnameInput);

			connection.getDatabaseNames(populateDatabaseDropdown);
		};

		populateDatabaseDropdown = function(dbs) {
			$scope.databases = dbs;
			$scope.$apply();
		};

		$scope.selectDatabase = function() {
			connection.use($scope.selectedDb);
			connection.getTableNames(populateTableDropdown);
		};

		var populateTableDropdown = function(tables) {
			$scope.dbTables = tables;
			$scope.$apply();
		};

		$scope.continueClick = function() {
			connection.getFieldNames($scope.selectedTable, function (fields) {
				$scope.fields = fields;
				$scope.$apply();

				messenger.publish('neon.database.fields', JSON.stringify(fields));
			});
		};
	}

	return {
		templateUrl: 'partials/databaseConfig.html',
		restrict: 'E',
		scope: {
			storeSelect: '=',
			hostName: '='
		},
		link: link
	}
});
