(function () {

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
    }

    function connectToDatastore() {
        $("#filter-container").hide();
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
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/databaseNames",
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
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/tableNames",
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
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/columnNames",
            {
                data: neon.wizard.dataset(),
                success: function (columnNames) {
                    neon.filter.grid(columnNames);
                    $("#filter-container").show();
                }
            });
        broadcastActiveDataset();
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
neon.wizard = function () {

    function getBaseDatasetInfo(){
        var selectedDatabase = $('#database-select option:selected');
        var selectedTable = $('#table-select option:selected');
        return { database: selectedDatabase.val(), table: selectedTable.val() }
    }

    return {
        dataset : getBaseDatasetInfo
    }
}();
