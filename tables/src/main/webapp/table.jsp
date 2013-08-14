<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Table widget</title>

    <link rel="stylesheet" type="text/css" href="css/slickgrid/slick.grid.css"/>
    <link rel="stylesheet" type="text/css" href="css/smoothness/jquery-ui-1.8.16.custom.css"/>
    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/tablewidget.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/tables.js -->
    <script src="jquery/jquery-1.7.min.js"></script>
    <script src="jquery/jquery.event.drag-2.2.js"></script>
    <script src="jquery/jquery-ui-1.8.16.custom.min.js"></script>
    <script src="slickgrid/slick.core.js"></script>
    <script src="slickgrid/slick.grid.js"></script>
    <script src="slickgrid/slick.dataview.js"></script>
    <script src="mergesort/merge-sort.js"></script>
    <script src="slickgrid/plugins/slick.autotooltips.js"></script>
    <script src="bootstrap/bootstrap.min.js"></script>
    <script src="table.js"></script>
    <!-- endbuild -->

    <!-- build:js js/tablewidget.js -->
    <script src="tablewidget.js"></script>
    <!-- endbuild -->

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>


</head>
<body>
<div class="container">

    <div id="controls">

        <div class="controls-row">
            <div class="control-group">
                <label class="control-label" for="limit">Limit</label>

                <div class="controls">
                    <input id="limit" type="number" min="1" value="500">
                </div>
            </div>
        </div>


        <div class="controls-row">
            <div class="control-group">

                <label class="control-label" for="sort-field">Sort</label>

                <div id="sort-controls" class="controls-row input-append">

                    <select id="sort-field"></select>

                    <div class="btn-group" data-toggle="buttons-radio">
                        <button id="sort-ascending" type="button" data-toggle="button" class="btn">Ascending
                        </button>
                        <button id="sort-descending" type="button" data-toggle="button" class="btn">Descending
                        </button>
                    </div>

                    <input id="sort-direction" type="hidden"/>
                </div>
            </div>
        </div>

    </div>

    <div id="table"></div>
</div>

</body>
</html>

