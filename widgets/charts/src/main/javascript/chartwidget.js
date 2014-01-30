

var neon = neon || {};
neon.chartWidget = (function (){
    var databaseName;
    var tableName;
    var widgetName;
    var filterKey;
    var onChange;

    function onActiveDatasetChanged(message, changeHandler, widget) {
        databaseName = message.database;
        tableName = message.table;
        onChange = changeHandler;
        widgetName = widget;

        neon.query.registerForFilterKey(databaseName, tableName, function(filterResponse){
            filterKey = filterResponse;
        });
        neon.query.getFieldNames(databaseName, tableName, widgetName, populateFromColumns);
    }

    function populateFromColumns(data) {
        var elements = [new neon.dropdown.Element("x", "temporal"), new neon.dropdown.Element("y", "numeric")];
        if(widgetName === neon.widget.BARCHART){
            elements = [new neon.dropdown.Element("x", ["text", "numeric"]), new neon.dropdown.Element("y", "numeric")];
        }
        neon.dropdown.populateAttributeDropdowns(data, elements, onChange);
    }

    return {
        onActiveDatasetChanged: onActiveDatasetChanged,

        getXAttribute: function(){
            return $('#x option:selected').val();
        },
        getYAttribute: function(){
            return $('#y option:selected').val();
        },
        getDatabaseName: function(){
            return databaseName;
        },
        getTableName: function(){
            return tableName;
        },
        getFilterKey: function(){
            return filterKey;
        },
        setDatabaseName: function(dbName){
            databaseName = dbName;
        },
        setTableName: function(table){
            tableName = table;
        },
        setFilterKey: function(key){
            filterKey = key;
        }
    };

})();
