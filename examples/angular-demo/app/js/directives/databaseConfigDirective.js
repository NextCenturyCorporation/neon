var databaseConfig = angular.module('configurationDirective', []);

databaseConfig.directive('databaseConfig', ['ConnectionService', function (connectionService) {
    var link = function ($scope, el, attr) {
        var messenger = new neon.eventing.Messenger();
        el.addClass('databaseConfig');

        $scope.datastoreSelect = $scope.storeSelect || 'mongo';
        $scope.hostnameInput = $scope.hostName || 'localhost';

        $scope.showDbTable = false;
        $scope.databases = [];
        $scope.dbTables = [];
        $scope.fields = [];
        $scope.isConnected = false;
        $scope.clearPopover = '';
        $scope.activeServer = "Choose dataset";
        $scope.servers = [
            {
                name: "Sample",
                datastoreSelect: "mongo",
                hostnameInput: "localhost",
                selectedDb: "mydb",
                selectedTable: "sample"
            },
            {
                name: "Twitter",
                datastoreSelect: "mongo",
                hostnameInput: "localhost",
                selectedDb: "mydb",
                selectedTable: "gbDate"
            }
        ];

        var connection;

        $scope.connectToDatastore = function () {
            $scope.showDbTable = true;

            // Connect to the datastore.
            connection = new neon.query.Connection();
            connection.connect($scope.datastoreSelect, $scope.hostnameInput);

            // Save the connection in the connection service for reuse by other directives.
            connectionService.setActiveConnection(connection);

            // Flag that we're connected for the front-end controls enable/disable code.
            $scope.isConnected = true;

            // Pull in the databse names.
            connection.getDatabaseNames(function (results) {
                $scope.$apply(function () {
                    populateDatabaseDropdown(results);
                });
            });
        };

        $scope.connectToPreset = function (server) {
            // Change name of active connection.
            $scope.activeServer = server.name;
            $scope.datastoreSelect = server.datastoreSelect;
            $scope.hostnameInput = server.hostnameInput;
            $scope.selectedDb = server.selectedDb;
            $scope.selectedTable = server.selectedTable;

            // Set datastore connection details and connect to the datastore.
            $scope.connectToDatastore();

            // Set database name and get list of tables.
            $scope.selectDatabase();

            // Set table name and initiate connection.
            $scope.connectToDatabase();
        };

        var populateDatabaseDropdown = function (dbs) {
            $scope.databases = dbs;
            //$scope.$apply();
        };

        $scope.selectDatabase = function () {
            connection.use($scope.selectedDb);
            connection.getTableNames(populateTableDropdown);
        };

        var populateTableDropdown = function (tables) {
            $scope.dbTables = tables;
            $scope.$apply();
        };

        $scope.connectToDatabase = function () {
            messenger.clearFiltersSilently(function () {
                $scope.broadcastActiveDataset();
            });
        };

        $scope.continueClick = function () {
            // Set active connection to Custom and connect.
            $scope.activeServer = "Custom";
            $scope.connectToDatabase();
        };

        $scope.broadcastActiveDataset = function () {
            // TODO: Alter or eliminate this when the Connection class in Neon is changed to emit
            // dataset selections.
            var message = {
                "database": $scope.selectedDb,
                "table": $scope.selectedTable
            };
            messenger.publish(neon.eventing.channels.ACTIVE_DATASET_CHANGED, message);
        }
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
}]);
