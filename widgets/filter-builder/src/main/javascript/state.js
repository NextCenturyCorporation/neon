var neon = neon || {};
neon.filterBuilderState = (function () {

    var clientId;

    function restoreState(){
        neon.ready(function(){
            clientId = neon.eventing.messaging.getInstanceId();
            neon.query.getSavedState(clientId, function(data){
                restoreConnectionState(data);
                if(data.filterKey){
                    restoreConnectionAndFilterState(data);
                }
            });
        });
    }

    function restoreConnectionState(data){
        $("#db-table").show();
        $('#hostname-input').val(data.selectedHostname);

        //set database options
        neon.wizard.populateDropdown('#database-select', data.databases);
        //set table options
        neon.wizard.populateDropdown('#table-select', data.tables);

        neon.dropdown.setDropdownInitialValue("datastore-select", data.selectedDatastore);
        neon.dropdown.setDropdownInitialValue("database-select", data.selectedDatabase);
        neon.dropdown.setDropdownInitialValue("table-select", data.selectedTable);
    }

    function restoreConnectionAndFilterState(data){
        $("#filter-container").show();
        $("#clear-filters-button").show();
        neon.filter.setFilterKey(data.filterKey);
        neon.filter.setFilterState(data.filterState);
        $("input[type='radio'][name='boolean'][value=" + data.andOr + "]").attr('checked', 'checked');
    }

    function buildSimpleStateObject(){
        var dataset = neon.wizard.getDataset();
        var databaseOptions = $('#database-select option');
        var tableOptions = $('#table-select option');

        var stateObject = {
            selectedHostname: $('#hostname-input').val(),
            selectedDatastore: $('#datastore-select option:selected').val(),
            selectedDatabase: dataset.database,
            selectedTable: dataset.table,
            databases: $.map(databaseOptions, function(option){ return option.value;}),
            tables: $.map(tableOptions, function(option){ return option.value;})
        };

        return stateObject;
    }

    function buildFullStateObject(){
        var stateObject = buildSimpleStateObject();
        var selectedAndOr = $("input[type='radio'][name='boolean']:checked").val();

        $.extend(stateObject, {
            filterKey: neon.filter.getFilterKey(),
            filterState: neon.filter.getFilterState(),
            andOr: selectedAndOr
        });

        return stateObject;
    }

    function saveState(){
        if(neon.filter.getFilterKey()){
            neon.query.saveState(clientId, buildFullStateObject());
        }
        else{
            neon.query.saveState(clientId, buildSimpleStateObject());
        }
    }

    return {
        restoreState: restoreState,
        saveState: saveState
    };

})();