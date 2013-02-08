<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Aperture Map Display</title>

    <link rel="stylesheet" type="text/css" href="css/map.css">
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

    <script src="js/aperture/1.0/lib/proj4js.js"></script>
    <script src="js/aperture/1.0/lib/OpenLayers-textures.js"></script>
    <script src="js/aperture/1.0/lib/raphael.js"></script>
    <script src="js/aperture/1.0/aperture.js"></script>
    <script src="js/mapconfig.js"></script>


    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
    </script>

</head>
<body>

<div id="map-display"></div>

<script src="js/mapwidget.js"></script>
</body>
</html>