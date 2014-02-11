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
neon.tableState = (function () {

    var clientId;
    var connectionId;


    function getSavedState(restoreStateCallback){
        neon.ready(function(){
            clientId = neon.eventing.messaging.getInstanceId();
            neon.query.getSavedState(clientId, function(state){
                restoreTableState(state, restoreStateCallback);
            });
        });
    }

    function setActiveDataset(data) {
        neon.activeDataset.setFilterKey(data.filterKey);
        neon.activeDataset.setDatabaseName(data.databaseName);
        neon.activeDataset.setTableName(data.tableName);
    }

    function restoreTableState(data, restoreStateCallback){
        if(!data || !data.query){
            return;
        }

        connectionId = data.connectionId;
        setActiveDataset(data);
        $('#limit').val(data.limitValue);

        if(restoreStateCallback && typeof restoreStateCallback === 'function'){
            restoreStateCallback(data);
        }
    }

    function buildStateObject(currentQuery, rowSelection){
        var stateObject = {
            connectionId: connectionId,
            filterKey: neon.activeDataset.getFilterKey(),
            databaseName: neon.activeDataset.getDatabaseName(),
            tableName: neon.activeDataset.getTableName(),
            sortColumns: neon.dropdown.getFieldNamesFromDropdown("sort-field"),
            sortValue: $('#sort-field').val(),
            limitValue: $('#limit').val(),
            sortDirection: $('#sort-direction').val(),
            selectedRows: rowSelection,
            query: currentQuery
        };

        return stateObject;
    }


    function saveState(currentQuery, rowSelection){
        neon.query.saveState(clientId, buildStateObject(currentQuery, rowSelection));
    }

    function setConnectionId(id){
        connectionId = id;
    }

    function getConnectionId(){
        return connectionId;
    }

    return {
        restoreState: getSavedState,
        saveState: saveState,
        setConnectionId: setConnectionId,
        getConnectionId: getConnectionId
    };

})();