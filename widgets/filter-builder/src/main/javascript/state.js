var neon = neon || {};
neon.filterBuilderState = (function () {

    var clientId;
    if(typeof (OWF) !== "undefined"){
        OWF.ready(function(){
            clientId = OWF.getInstanceId();
            restoreState();
        });
    }

    function restoreState(){
        //Need to implement this.
        console.log("Restoring state...");
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
            andOr: selectedAndOr,
            columns: neon.filter.getColumnOptions()
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
        saveState: saveState
    };

})();