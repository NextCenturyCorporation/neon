<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Timeline widget</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/timelinewidget.css">
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">
    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <script src="js/charts.js"></script>
    <script src="js/timelinewidget.js"></script>

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

</head>
<body>

<div class="container">


    <div class="controls-row">
        <div class="control-group">
            <label class="control-label" for="x">x-axis (Date)</label>

            <div class="controls">
                <select id="x"></select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="y">y-axis</label>

            <div class="controls">
                <select id="y"></select>
            </div>
        </div>
    </div>

    <div class="controls-row">
        <div class="control-group">
            <label class="control-label" for="time-granularity">Time Granularity</label>

            <div class="controls">
                <select id="time-granularity">
                </select>
            </div>
        </div>
    </div>

    <div id="timeline">
        <div id="chart"></div>
    </div>

    <div class="controls-row">
        <div id="button-row" class="control-group">
            <div class="controls">
                <button type="button" type="button" id="redraw-bounds" class="btn btn-info">Redraw Bounds</button>

                <button type="button" type="button" id="reset-filter" class="btn btn-danger">Reset Time Period</button>
            </div>
        </div>
    </div>


</div>

</body>
</html>
