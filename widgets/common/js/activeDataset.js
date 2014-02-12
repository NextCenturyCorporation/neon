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

    function getFieldNamesForDataset(connectionId, widgetName, callback){
        neon.query.getFieldNames(connectionId, databaseName, tableName, widgetName, callback);
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
