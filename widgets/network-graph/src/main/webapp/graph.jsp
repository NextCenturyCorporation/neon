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
    <title>Network Graph</title>

    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">
    <link rel="stylesheet" type="text/css" href="css/graph.css">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/network-graph.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="javascript/graph.js"></script>
    <script src="javascript/graphwidget.js"></script>
    <!-- endbuild -->


</head>
<body>
<neon:hidden-neon-server/>

<div id="graph">
</div>
</body>
</html>