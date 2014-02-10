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