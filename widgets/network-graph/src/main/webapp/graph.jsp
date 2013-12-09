<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html>
<head>
    <title>Network Graph</title>

    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/network-graph.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="javascript/initialize.js"></script>
    <!-- endbuild -->
</head>
<body>
<neon:hidden-neon-server/>

<div id="container">
</div>

</body>
</html>