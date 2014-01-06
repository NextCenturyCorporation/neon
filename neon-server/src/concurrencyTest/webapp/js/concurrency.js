var databaseName = 'concurrencytest';
var tableName = 'records';
var filterKey;

$(function(){
    var where = neon.query.where;
    var or = neon.query.or;

    neon.query.SERVER_URL = "http://localhost:11402/neon";
    neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
        filterKey = filterResponse;
    });

    $('#mongo').click(function(){
        neon.query.connectToDatastore("mongo", "localhost");
    });

    $('#hive').click(function(){
        neon.query.connectToDatastore("hive", "xdata2");
    });

    $('#all-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.allDataMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data.data));
        });
    });

    $('#filtered-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.filteredMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data.data));
        });
    });

    $('#selection-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.selectionMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data.data));
        });
    });

    $('#add-filter').click(function(){
        var filter =  new neon.query.Filter().selectFrom(databaseName, tableName);
        var whereStateClause = or(where('state', '=', 'VA'), where('state', '=', 'DC'));
        filter.where(whereStateClause);
        neon.query.addFilter(filterKey, filter);

    });

    $('#remove-filter').click(function(){
        var filter =  new neon.query.Filter().selectFrom(databaseName, tableName);
        neon.query.removeFilter(filterKey);
    });

    $('#add-selection').click(function(){
        var selection =  new neon.query.Filter().selectFrom(databaseName, tableName);
        var salaryAndStateClause = where('salary', '<=', 100000);
        selection.where(salaryAndStateClause);
        neon.query.addSelection(filterKey, selection);
    });

    $('#remove-selection').click(function(){
        var selection =  new neon.query.Filter().selectFrom(databaseName, tableName);
        neon.query.removeSelection(filterKey);
    });

});

function displayResults(text){
    $('#results').text(text);
}

