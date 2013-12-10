var neon = neon || {};

neon.activeDataset = (function(){
    var databaseName;
    var tableName;
    var filterKey;

    function setActiveDataset(message){
        databaseName = message.database;
        tableName = message.table;
        neon.ready(function(){
            neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
                filterKey = filterResponse;
            });
        });
    }

    function getFieldNamesForDataset(widgetName, callback){
        neon.query.getFieldNames(databaseName, tableName, widgetName, callback);
    }

    return {
        setActiveDataset: setActiveDataset,
        getFieldNamesForDataset: getFieldNamesForDataset,
        getDatabaseName: function(){
            return databaseName;
        },
        getTableName: function(){
            return tableName;
        },
        getFilterKey: function(){
            return filterKey;
        },
        setDatabaseName: function(name){
            databaseName = name;
        },
        setTableName: function(name){
            tableName = name;
        },
        setFilterKey: function(key){
            filterKey = key;
        }
    };

})();
