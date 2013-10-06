<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="neon" uri="http://nextcentury.com/neon/taglib"%>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>
    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/builder.css"/>
    <link rel="stylesheet" type="text/css" href="css/slickgrid/slick.grid.css"/>
    <link rel="stylesheet" type="text/css" href="css/smoothness/jquery-ui-1.8.16.custom.css"/>
    <!-- widgetbase.css must be included after slickgrid in order to override the slickgrid table color scheme -->
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="<neon:out name="neon.url"/>/css/neon.css"/>

    <script src="<neon:out name="owf.url"/>/js/owf-widget.js"></script>
    <script src="<neon:out name="neon.url"/>/js/neon.js"></script>

    <!-- build:js js/query-builder.js -->
    <script src="js/tables.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/jquery-resize/jquery.ba-resize-1.1.min.js"></script>
    <script src="js-lib/lodash/1.3.1/lodash.min.js"></script>
    <script src="javascript/init.js"></script>
    <script src="javascript/builder.js"></script>
    <!-- endbuild -->

</head>
<body>
<neon:hidden-neon-server/>

<div id="queryForm">
    <h4> Enter a Query </h4>
    <textarea id="queryText" class="space-below" rows="3"></textarea>

    <div id="errorText" class="error-text space-below"></div>
    <button id="submit" class="btn" onclick="neon.queryBuilder.submit();">Submit</button>
</div>

<div id="results"></div>

</body>
</html>