<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Timeline widget</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/barchart.css">
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">
    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/charts.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js-lib/lodash/1.3.1/lodash.min.js"></script>
    <script src="js/toggle.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/namespaces.js"></script>
    <script src="javascript/barchart.js"></script>
    <script src="javascript/timeline.js"></script>
    <!-- endbuild -->

    <!-- build:js js/chartwidget.js -->
    <script src="javascript/chartwidget.js"></script>
    <!-- endbuild -->

    <!-- build:js js/timelinewidget.js -->
    <script src="javascript/timelinewidget.js"></script>
    <!-- endbuild -->

</head>
<body>

<input type="hidden" id="neon-server" value="<%=neonServerUrl%>"/>

<div id="options-panel" class="options">
    <div class="controls-row">
        <div class="control-group">
            <label class="control-label" for="x">x-axis (Date)</label>

            <div class="controls">
                <select id="x" class="configuration-dropdown"></select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="y">y-axis</label>

            <div class="controls">
                <select id="y" class="configuration-dropdown"></select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="time-granularity">Time Granularity</label>

            <div class="controls">
                <select id="time-granularity" class="configuration-dropdown">
                </select>
            </div>
        </div>

        <div class="control-group">
            <div class="controls">
                <button type="button" id="redraw-bounds" class="btn-small">Redraw Bounds</button>

                <button type="button" id="reset-filter" class="btn-small">Reset Time Period</button>
            </div>
        </div>
    </div>
</div>

<div id="timeline" class="timeline-div">
    <div id="chart" class="chart-div"></div>
</div>

</body>
</html>
