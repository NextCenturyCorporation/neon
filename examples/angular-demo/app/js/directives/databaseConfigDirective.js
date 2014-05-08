var databaseConfig = angular.module('configurationDirective', []);

databaseConfig.directive('databaseConfig', ['ConnectionService', function (connectionService) {
    var link = function ($scope, el, attr) {
        el.addClass('databaseConfig');

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
                selectedTable: "gbSmall"
            }
        ];

        $scope.initialize = function() {
            $scope.messenger = new neon.eventing.Messenger();
            $scope.datastoreSelect = $scope.storeSelect || 'mongo';
            $scope.hostnameInput = $scope.hostName || 'localhost';
        }

        $scope.connectToDatastore = function () {
            $scope.showDbTable = true;

            // Connect to the datastore.
            $scope.connection = new neon.query.Connection();
            $scope.connection.connect($scope.datastoreSelect, $scope.hostnameInput);

            // Save the connection in the connection service for reuse by other directives.
            connectionService.setActiveConnection($scope.connection);

            // Flag that we're connected for the front-end controls enable/disable code.
            $scope.isConnected = true;

            // Pull in the databse names.
            $scope.connection.getDatabaseNames(function (results) {
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
            $scope.connection.use($scope.selectedDb);
            $scope.connection.getTableNames(populateTableDropdown);
        };

        var populateTableDropdown = function (tables) {
            $scope.dbTables = tables;
            $scope.$apply();
        };

        $scope.connectToDatabase = function () {
            $scope.messenger.clearFiltersSilently(function () {
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
                "datastore": $scope.datastoreSelect,
                "hostname": $scope.hostnameInput,
                "database": $scope.selectedDb,
                "table": $scope.selectedTable,
            };
            $scope.messenger.publish(neon.eventing.channels.ACTIVE_DATASET_CHANGED, message);
        }

        // Wait for neon to be ready, the create our messenger and intialize the view and data.
        neon.ready(function () {
            $scope.initialize();
        });

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
