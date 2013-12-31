var databaseName = 'acceptanceTest';
var tableName = 'records';
var filterKey;

$(function(){
    neon.query.SERVER_URL = "http://localhost:11402/neon";
    neon.query.registerForFilterKey(databaseName, tableName, function (filterResponse) {
        filterKey = filterResponse;
    });

    $('#mongo').click(function(){
        alert('mongo');
    });

    $('#hive').click(function(){
        alert('hive');
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
        neon.query.addFilter(filterKey, filter);
    });

    $('#remove-filter').click(function(){
        var filter =  new neon.query.Filter().selectFrom(databaseName, tableName);
        neon.query.removeFilter(filterKey);
    });

    $('#add-selection').click(function(){
        var selection =  new neon.query.Filter().selectFrom(databaseName, tableName);
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

