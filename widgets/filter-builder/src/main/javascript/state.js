var neon = neon || {};
neon.filterBuilderState = (function () {

    var clientId;

    function restoreState(){
        if(typeof (OWF) !== "undefined"){
            OWF.ready(function(){
                clientId = OWF.getInstanceId();
                neon.query.getSavedState(clientId, function(data){
                    restoreSimpleState(data);
                    if(data.filterKey){
                        restoreComplexState(data);
                    }
                });
            });
        }
    }

    function restoreSimpleState(data){
        $("#db-table").show();
        $('#hostname-input').val(data.selectedHostname);

        //set database options
        var databaseSelectSelector = $('#database-select');
        databaseSelectSelector.find('option').remove();
        $.each(data.databases, function (index, value) {
            $('<option>').val(value).text(value).appendTo(databaseSelectSelector);
        });

        //set table options
        var tableSelectSelector = $('#table-select');
        tableSelectSelector.find('option').remove();
        $.each(data.tables, function (index, value) {
            $('<option>').val(value).text(value).appendTo(tableSelectSelector);
        });

        $('#datastore-select option[value="' + data.selectedDatastore + '"]').prop('selected', true);
        $('#database-select option[value="' + data.selectedDatabase + '"]').prop('selected', true);
        $('#table-select option[value="' + data.selectedTable + '"]').prop('selected', true);
    }

    function restoreComplexState(data){
        $("#filter-container").show();
        $("#clear-filters-button").show();
        neon.filter.setFilterKey(data.filterKey);
        neon.filter.setFilterState(data.filterState);
        $("input[type='radio'][name='boolean'][value=" + data.andOr + "]").attr('checked', 'checked');
    }

    function buildSimpleStateObject(){
        var dataset = neon.wizard.dataset();
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