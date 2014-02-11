/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

//Used for recording gatling tests.

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
        neon.query.connectToDatastore("hive", "shark");
    });

    $('#all-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.allDataMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data));
        });
    });

    $('#filtered-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.filteredMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data));
        });
    });

    $('#selection-query').click(function(){
        var query =  new neon.query.Query().selectFrom(databaseName, tableName);
        query.selectionMode();
        neon.query.executeQuery(query, function(data){
            displayResults(JSON.stringify(data));
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
