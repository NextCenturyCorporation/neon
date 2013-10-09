<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html>
<head>
    <title>Map</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/map.js -->
    <script src="js-lib/openlayers/OpenLayers.js"></script>
    <script src="js-lib/heatmap/heatmap.js"></script>
    <script src="js-lib/heatmap/heatmap-openlayers.js"></script>
    <script src="javascript/mapcore.js"></script>
    <script src="javascript/mapwidget.js"></script>
    <!-- endbuild -->

</head>
<body>
<neon:hidden-neon-server/>

<div class="container">

    <div id="map"></div>
</div>

</body>
</html>