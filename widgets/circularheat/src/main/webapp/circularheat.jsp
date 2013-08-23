<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Circular Heat Chart Display</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/circularheat.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/circularheat.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/circularHeatChart/circularHeatChart.js"></script>
    <script src="javascript/circularheatwidget.js"></script>
    <!-- endbuild -->

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

</head>
<body>

<div class="container">

    <div id="options-bar">
        <div id="toggle">
            <img id="toggle-image" />
        </div>

        <div id="options">
            <div class="controls-row">
                <div class="control-group">
                    <label class="control-label" for="date">Date</label>

                    <div class="controls">
                        <select id="date" class="dropdown"></select>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="circularheatchart"></div>

</div>

</body>
</html>