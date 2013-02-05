<%@ page import="java.util.Enumeration" %>
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
    <script src="<%=neonServerUrl%>/js/jquery/jquery-1.8.3.min.js"></script>
    <script src="<%=neonServerUrl%>/js/underscore/1.4.3/underscore.js"></script>
    <script src="<%=neonServerUrl%>/js/uuid/uuid.js"></script>

    <script src="<%=neonServerUrl%>/js/namespace.js"></script>

    <script src="<%=neonServerUrl%>/js/util/ajaxUtils.js"></script>
    <script src="<%=neonServerUrl%>/js/util/arrayUtils.js"></script>
    <script src="<%=neonServerUrl%>/js/query/query.js"></script>
    <script src="<%=neonServerUrl%>/js/eventing/channels.js"></script>
    <script src="<%=neonServerUrl%>/js/eventing/messageHandler.js"></script>
    <script src="<%=neonServerUrl%>/js/query/owfEventPublisher.js"></script>
    <script src="<%=neonServerUrl%>/js/eventing/owfIntentsLauncher.js"></script>

    <script src="js/intents.js"></script>

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
    </script>

</head>
<body>

<div>Charities</div>
<select name="charities" disabled></select>
<br><br><br>

<div>Display as:</div>
<select name="intents" disabled></select><br>
<input type="button" id="launch" value="Display Data" onclick="displayData()" disabled/>

<script src="js/charitynetquerywidget.js"></script>


</body>
</html>