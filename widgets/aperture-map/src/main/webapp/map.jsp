<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Aperture Map Display</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/aperture-map.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/aperture/1.0/lib/raphael.js"></script>
    <script src="js-lib/aperture/1.0/lib/json2-min.js"></script>
    <script src="js-lib/aperture/1.0/lib/proj4js.js"></script>
    <script src="js-lib/aperture/1.0/lib/OpenLayers-textures.js"></script>
    <script src="js-lib/aperture/1.0/aperture.js"></script>
    <script src="js/toggle.js"></script>
    <script src="javascript/aperturemapconfig.js"></script>
    <script src="javascript/mapwidget.js"></script>
    <!-- endbuild -->

</head>
<body>

<input type="hidden" id="neon-server" value="<%=neonServerUrl%>"/>

<div class="container">
    <div class="options-bar">
        <div class="toggle">
            <img class="toggle-image"/>
        </div>

        <div class="options">
            <div class="controls-row">
                <div class="control-group">
                    <label class="control-label" for="latitude">Latitude Field</label>

                    <div class="controls">
                        <select id="latitude" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="longitude">Longitude Field</label>

                    <div class="controls">
                        <select id="longitude" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="size-by">Size By</label>

                    <div class="controls">
                        <select id="size-by" class="dropdown"></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="color-by">Color By</label>

                    <div class="controls">
                        <select id="color-by" class="dropdown"></select>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="map"></div>

</div>

</body>
</html>