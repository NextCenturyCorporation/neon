<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html>
<head>
    <title>Aperture Map Display</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/aperture-map.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/aperture/1.0/lib/raphael.js"></script>
    <script src="js-lib/aperture/1.0/lib/json2-min.js"></script>
    <script src="js-lib/aperture/1.0/lib/proj4js.js"></script>
    <script src="js-lib/aperture/1.0/lib/OpenLayers-textures.js"></script>
    <script src="js-lib/aperture/1.0/aperture.js"></script>
    <script src="js/toggle.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/aperturemapconfig.js"></script>
    <script src="javascript/mapwidget.js"></script>
    <!-- endbuild -->

</head>
<body>
<neon:hidden-neon-server/>

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
        </div>
    </div>

    <div id="map"></div>

</div>

</body>
</html>