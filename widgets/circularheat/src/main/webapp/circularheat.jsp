<!--
Copyright 2013 Next Century Corporation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html>
<head>
    <title>Circular Heat Chart</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/circularheat.css">
    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/circularheat.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/circularHeatChart/circularHeatChart.js"></script>
    <script src="js/toggle.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/circularheatwidget.js"></script>
    <!-- endbuild -->

</head>
<body>
<neon:hidden-neon-server/>

<div id="options-panel" class="options">
    <div class="controls-row">
        <div class="control-group">
            <label class="control-label" for="date">Date</label>

            <div class="controls">
                <select id="date" class="configuration-dropdown"></select>
            </div>
        </div>
    </div>
</div>

<div id="circularheatchart"></div>


</body>
</html>