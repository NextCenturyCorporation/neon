<!DOCTYPE html>
<html>
<head>

    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Charity Net Query Widget</title>

    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <script src="js/intents.js"></script>

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

</head>
<body>

<div id="charities-container">

    <div>
        <div><input type="radio" name="donations" value="0" checked>To me</div>
        <div><input type="radio" name="donations" value="1">From my donors to other charities (peers) and me</div>
        <div><input type="radio" name="donations" value="2">To my peers from donors that do NOT donate to me</div>
    </div>

    <br>

    <div>Charities</div>
    <select name="charities"></select>
</div>
<br>
</div>


<script src="js/charitynetquerywidget.js"></script>
<script src="js/intents.js"></script>
<script>
    neon.eventing.OWFIntentsLauncher.addIntentsSelector(intentsLauncher,
            charityNetIntents.send,
            function () {
                return dataSourceName;
            },
            function () {
                return transactionsDatasetId;
            },
            '#charities-container');
</script>


</body>
</html>