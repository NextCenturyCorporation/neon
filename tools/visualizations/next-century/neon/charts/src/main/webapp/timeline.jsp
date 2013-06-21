<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Timeline widget</title>

    <%--<link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">--%>
    <link rel="stylesheet" type="text/css" href="css/timelinewidget.css">
    <link rel="stylesheet" type="text/css" href="css/timeline.css">
    <link rel="stylesheet" type="text/css" href="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>


    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

</head>
<body>

<div class="container">


    <div>
        <label for="x">X (Date):</label>
        <select id="x"></select>

        <label for="y">Y:</label>
        <select id="y"></select>
    </div>

    <div id="timeline">
        <div id="chart"></div>
    </div>

</div>

<script src="js/charts.js"></script>
<script src="js/timelinewidget.js"></script>


</body>
</html>