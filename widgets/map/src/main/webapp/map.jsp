<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Map</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/map.js -->
    <script src="js-lib/openlayers/OpenLayers.js"></script>
    <script src="js-lib/heatmap/heatmap.js"></script>
    <script src="js-lib/heatmap/heatmap-openlayers.js"></script>
    <script src="js/toggle.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/map.js"></script>
    <!-- endbuild -->

</head>
<body>
<input type="hidden" id="neon-server" value="<%=neonServerUrl%>"/>

<div class="container">

    <div id="options-panel" class="options">
        <div class="controls-row">
            <div class="control-group">
                <label class="control-label" for="latitude">Latitude Field</label>

                <div class="controls">
                    <select id="latitude" class="configuration-dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="longitude">Longitude Field</label>

                <div class="controls">
                    <select id="longitude" class="configuration-dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="size-by">Size By</label>

                <div class="controls">
                    <select id="size-by" class="configuration-dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="color-by">Color By</label>

                <div class="controls">
                    <select id="color-by" class="configuration-dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="layers">Layers</label>

                <label class="checkbox inline control-label" id="layers">
                    <input type="checkbox" value="points">Points</input>
                </label>
                <label class="checkbox inline control-label" id="layers">
                    <input type="checkbox" value="heatmap">Heatmap</input>
                </label>
            </div>
        </div>
    </div>

    <div id="map"></div>
</div>

</body>
</html>