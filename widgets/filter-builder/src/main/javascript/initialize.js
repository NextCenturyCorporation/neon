$(function () {

    Handlebars.registerHelper('select', function (context, options) {
        var el = $('<select />').html(options.fn(this));
        el.find('option').filter(function () {
            return this.value === context;
        }).attr('selected', 'selected');
        return el.html();
    });
    Handlebars.registerHelper('escapeQuotes', function (context, options) {
        var el = $('<div/>').html(options.fn(this));
        if (context === "") {
            el.find('input').attr('value', '""');
        }
        return el.html();
    });


    var columns = [];
    var messageHandler = {
        publishMessage: function(){}
    };

    if(typeof (OWF) !== "undefined"){
        OWF.ready(function () {
            // right now the message handler only receives messages (which happens just by creating it),
            // but in the future we might want to send messages based on actions performed on the table
            messageHandler = new neon.eventing.MessageHandler({});
        });
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
    }
    neon.query.SERVER_URL = $("#neon-server").val();

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
        neon.util.AjaxUtils.doGet(neon.query.SERVER_URL + "/services/filterservice/hostnames",
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
        $('#database-select').change(populateTableDropdown);
        $('#table-select').change(neon.filterBuilderState.saveState);

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
        neon.util.AjaxUtils.doGet(neon.query.SERVER_URL + "/services/filterservice/databasenames",
            {
                success: function (databaseNames) {
                    databaseSelectSelector.find('option').remove();
                    $.each(databaseNames, function (index, value) {
                        $('<option>').val(value).text(value).appendTo(databaseSelectSelector);
                    });

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
                    neon.filterBuilderState.saveState();
                }
            });
    }

    function selectDatabaseAndTable(){
        var dataSet = neon.wizard.dataset();

        neon.query.clearFilters(function() {});
        if(!neon.filter.getFilterKey()){
            neon.query.registerForFilterKey(dataSet.database, dataSet.table, function(filterResponse){
                neon.filter.setFilterKey(filterResponse);
            });
        }
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

    init();

});