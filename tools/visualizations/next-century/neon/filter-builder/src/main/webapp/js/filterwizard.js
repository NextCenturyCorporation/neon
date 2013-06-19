(function () {

    function init() {
        hideWizardSteps();
        setupHostnames();
        addClickHandlers();
    }

    function hideWizardSteps() {
        $("#db-table").hide();
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
    }

    function connectToDatastore() {
        $("#db-table").show();
        var databaseSelectedOption = $('#datastore-select option:selected');
        var hostnameSelector = $('#hostname-input');

        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/connect",
            {
                data: { datastore: databaseSelectedOption.val(), hostname: hostnameSelector.val() },
                success: successfulConnect
            });
    }

    function successfulConnect() {
        var databaseSelectSelector = $('#database-select');
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/databaseNames",
            {
                success: function (databaseNames) {
                    $.each(databaseNames, function (index, value) {
                        $('<option>').val(value).text(value).appendTo(databaseSelectSelector);
                    });

                    databaseSelectSelector.change(function () {
                        var selectedDatabase = $('#database-select option:selected');
                        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/filterservice/tableNames",
                            {
                                data: { database: selectedDatabase.val() },
                                success: function (tableNames) {
                                    $.each(tableNames, function (index, value) {
                                        $('<option>').val(value).text(value).appendTo('#table-select');
                                    });
                                    //Execute change right away.
                                    databaseSelectSelector.change();
                                }
                            });

                    });

                }
            });
    }

    $(function () {
        init();
    });


})();

