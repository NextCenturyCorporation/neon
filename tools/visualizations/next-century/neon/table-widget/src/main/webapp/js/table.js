$(document).ready(function () {
    var oTable = $('#datatable').dataTable({
        "bLengthChange": false,
        "aoColumns": [
            { "sTitle": "Name" },
            { "sTitle": "Type", "sWidth": "100px" },
            { "sTitle": "Date Uploaded", "sWidth": "200px" },
            { "sTitle": "Run Algorithm", "sWidth": "200px" },
            { "bVisible": false }
        ]
    });
});