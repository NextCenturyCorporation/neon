<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>

    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">
    <link rel="stylesheet" type="text/css" href="css/filter.css">
    <link href="css/ui-darkness/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">

    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <script>
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>


    <script src="js/jquery/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/filterwizard.js"></script>

</head>
<body>

<div class="container">
    <div id="datastore">
        <h3>Select a Datastore</h3>

        <div class="controls-row">

            <div class="control-group">
                <label class="control-label" for="datastore-select">Datastore Type</label>

                <div class="controls">
                    <select id="datastore-select">
                        <option value="mongo">Mongo</option>
                        <option value="hive">Hive</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="hostname-input">Datastore Host Name</label>

                <div class="controls">
                    <input type="text" id="hostname-input"/>
                </div>
            </div>

            <div class="control-group">
                <!-- spacer label to align the button -->
                <label class="control-label" for="datastore-button">&nbsp;</label>

                <div class="controls">
                    <button class="btn" id="datastore-button">Continue</button>
                </div>
            </div>
        </div>
    </div>

    <div id="db-table">
        <h4>Select a Database and Table</h4>

        <div class="controls controls-row">

            <div class="control-group">
                <label class="control-label" for="database-select">Database Name</label>

                <div class="controls">
                    <select id="database-select">
                        <option value="">Select Database...</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="table-select">Table Name</label>

                <div class="controls">
                    <select id="table-select"></select>
                </div>
            </div>

            <div class="control-group">
                <!-- spacer label to align the button -->
                <label class="control-label" for="database-table-button">&nbsp;</label>

                <div class="controls">
                    <button class="btn" id="database-table-button">Continue</button>
                </div>
            </div>
        </div>

    </div>
</div>

</body>
</html>