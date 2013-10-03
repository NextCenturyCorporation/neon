<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Map</title>

    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/map.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/map.js -->
    <script src="js/toggle.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/map.js"></script>
    <!-- endbuild -->

</head>
<body>
<input type="hidden" id="neon-server" value="<%=neonServerUrl%>"/>

<div id="map">This is the map jsp</div>

</body>
</html>