(function(){

    function init(){
        hideWizardSteps();
        setupHostnames();
        addClickHandlers();
    }

    function hideWizardSteps(){
        $("#db-table").hide();
    }

    function setupHostnames(){
        var hostnames = ["localhost", "xdata2"];

        $("#hostname-input").autocomplete({
            source: hostnames
        });
    }

    function addClickHandlers(){
        $("#datastore-button").click(connectToDatastore);
    }

    function connectToDatastore(){
        $("#db-table").show();
        var databaseSelectSelector = $('#database-select');

        var databaseNames = ["cn_db", "mydb", "insert"];
        $.each(databaseNames, function(index, value){
            $('<option>').val(value).text(value).appendTo(databaseSelectSelector);
        });

        databaseSelectSelector.change(function(){
            var tableNames = ["things"];
            $.each(tableNames, function(index, value){
                $('<option>').val(value).text(value).appendTo('#table-select');
            });
        });
        //Execute change right away.
        databaseSelectSelector.change();
    }


    $(function(){
        init();
    });


})();

