(function () {

    var columns = [];

    var messageHandler = {
        publishMessage: function(){}
    };

    if(typeof (OWF) !== "undefined"){
        OWF.ready(function () {
            // right now the message handler only receives messages (which happens just by creating it),
            // but in the future we might want to send messages based on actions performed on the table
            messageHandler = new neon.eventing.MessageHandler({
                activeDatasetChaged: broadcastActiveDataset
            });
        });
    }

    function init() {
        hideWizardSteps();
        setupHostnames();
        addClickHandlers();
    }

    function hideWizardSteps() {
        $("#db-table").hide();
        $("#filter-container").hide();
        $("#clear-filters-button").hide();
    }

    function setupHostnames() {
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/hostnames",
            {
                success: function (data) {
                    $("#hostname-input").autocomplete({
                        source: data
                    });
                }
            });
    }

    function addClickHandlers() {
        $("#datastore-button").click(connectToDatastore);
        $("#database-table-button").click(selectDatabaseAndTable);
        $("#clear-filters-button").click(clearAllFilters);
    }

    function connectToDatastore() {
        $("#filter-container").hide();
        $("#clear-filters-button").hide();
        $("#db-table").show();
        var databaseSelectedOption = $('#datastore-select option:selected');
        var hostnameSelector = $('#hostname-input');

        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/connect",
            {
                data: { datastore: databaseSelectedOption.val(), hostname: hostnameSelector.val() },
                success: populateDatabaseDropdown
            });
    }

    function populateDatabaseDropdown() {
        var databaseSelectSelector = $('#database-select');
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/databasenames",
            {
                success: function (databaseNames) {
                    databaseSelectSelector.find('option').remove();
                    $.each(databaseNames, function (index, value) {
                        $('<option>').val(value).text(value).appendTo(databaseSelectSelector);
                    });

                    databaseSelectSelector.change(populateTableDropdown);
                    populateTableDropdown();
                }
            });
    }

    function populateTableDropdown() {
        var selectedDatabase = $('#database-select option:selected');
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/tablenames",
            {
                data: { database: selectedDatabase.val() },
                success: function (tableNames) {
                    $('#table-select').find('option').remove();
                    $.each(tableNames, function (index, value) {
                        $('<option>').val(value).text(value).appendTo('#table-select');
                    });
                }
            });
    }

    function selectDatabaseAndTable(){
        var dataSet = neon.wizard.dataset();


        neon.query.clearFilters(function() {});
        neon.query.registerForFilterKey(dataSet.database, dataSet.table, function(filterResponse){
            neon.filter.setFilterKey(filterResponse);
        });

        neon.query.getFieldNames(dataSet.database, dataSet.table, function(data){
            columns = data.fieldNames;
            neon.filter.grid(columns);
            $("#filter-container").show();
            $("#clear-filters-button").show();
        });

        broadcastActiveDataset();
    }

    function clearAllFilters(){
        neon.query.clearFilters(function(){
            var database = $('#database-select').val();
            var table = $('#table-select').val();
            var message = { "database" : database, "table" : table };
            neon.filter.grid(columns);
            messageHandler.publishMessage(neon.eventing.Channels.FILTERS_CHANGED, message);
        });
    }

    function broadcastActiveDataset() {
        var database = $('#database-select').val();
        var table = $('#table-select').val();
        var message = { "database" : database, "table" : table };
        messageHandler.publishMessage(neon.eventing.Channels.ACTIVE_DATASET_CHANGED, message);
    }

    $(function () {
        init();
    });

})();

var neon = neon || {};
neon.wizard = (function () {
    function getBaseDatasetInfo(){
        var selectedDatabase = $('#database-select option:selected');
        var selectedTable = $('#table-select option:selected');
        return { database: selectedDatabase.val(), table: selectedTable.val() };
    }

    return {
        dataset : getBaseDatasetInfo
    };
})();
