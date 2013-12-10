var neon = neon || {};
neon.tableState = (function () {

    var clientId;

    function getSavedState(restoreStateCallback){
        neon.ready(function(){
            clientId = neon.eventing.messaging.getInstanceId();
            neon.query.getSavedState(clientId, function(data){
                restoreTableState(data, restoreStateCallback);
            });
        });
    }

    function setActiveDataset(data) {
        neon.activeDataset.setFilterKey(data.filterKey);
        neon.activeDataset.setDatabaseName(data.databaseName);
        neon.activeDataset.setTableName(data.tableName);
    }

    function setSortDropdown(data) {
        var element = new neon.dropdown.Element("sort-field");
        neon.dropdown.populateAttributeDropdowns(data.sortColumns, element, updateTable);
        neon.dropdown.setDropdownInitialValue("sort-field", data.sortValue);
    }

    function restoreTableState(data, restoreStateCallback){
        setActiveDataset(data);
        setSortDropdown(data);
        $('#limit').val(data.limitValue);

        if(restoreStateCallback && typeof restoreStateCallback === 'function'){
            restoreStateCallback(data);
        }
    }

    function buildStateObject(currentQuery, rowSelection){
        var stateObject = {
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

    return {
        restoreState: getSavedState,
        saveState: saveState
    };

})();