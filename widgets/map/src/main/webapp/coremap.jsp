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

    <!-- build:js js/map.js -->
    <script src="js-lib/lodash/1.3.1/lodash.min.js"></script>
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
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