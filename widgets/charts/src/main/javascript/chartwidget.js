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
