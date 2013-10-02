$(function () {

    neon.query.SERVER_URL = $("#neon-server").val();
    var messageHandler = {
        publishMessage: function () {
        }
    };

    initializeWidget();
    neon.filterBuilderState.restoreState();


    function initializeWidget() {
        setupOWFMessageHandler();
        createHandlebarsHelpers();
        hideWizardSteps();
        setupHostnames();
        addClickHandlers();
    }

    function setupOWFMessageHandler() {
        if (typeof (OWF) !== "undefined") {
            OWF.ready(function () {
                messageHandler = new neon.eventing.MessageHandler({});
            });
            OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        }
    }

    function createHandlebarsHelpers() {
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
        neon.util.AjaxUtils.doGet(neon.query.SERVER_URL + "/services/filterservice/databasenames",
            {
                success: function (databaseNames) {
                    neon.wizard.populateDropdown('#database-select', databaseNames);
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
                    neon.wizard.populateDropdown('#table-select', tableNames);
                    neon.filter.setColumns([]);
                    neon.filter.initializeFilterSection();
                    neon.filterBuilderState.saveState();
                }
            });
    }

    function selectDatabaseAndTable() {
        var dataSet = neon.wizard.getDataset();
        var numTimes = 2;
        if(!neon.filter.getFilterKey()) {
            numTimes = 3;
        }

        var executingInitFilterSection = _.after(numTimes, initFilterForm);

        neon.query.clearFilters(function () {
            executingInitFilterSection();
        });

        if(!neon.filter.getFilterKey()) {
            neon.query.registerForFilterKey(dataSet.database, dataSet.table, function (filterResponse) {
                neon.filter.setFilterKey(filterResponse);
                executingInitFilterSection();
            });
        }

        neon.query.getFieldNames(dataSet.database, dataSet.table, function (data) {
            neon.filter.setColumns(data.fieldNames);
            executingInitFilterSection();
        });

        broadcastActiveDataset();
    }

    function initFilterForm() {
        neon.filter.initializeFilterSection();
    }

    function clearAllFilters() {
        neon.query.clearFilters(function () {
            var database = $('#database-select').val();
            var table = $('#table-select').val();
            var message = { "database": database, "table": table };
            neon.filter.initializeFilterSection();
            messageHandler.publishMessage(neon.eventing.Channels.FILTERS_CHANGED, message);
        });
    }

    function broadcastActiveDataset() {
        var database = $('#database-select').val();
        var table = $('#table-select').val();
        var message = { "database": database, "table": table };
        messageHandler.publishMessage(neon.eventing.Channels.ACTIVE_DATASET_CHANGED, message);
    }

});