<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>

    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
    %>

    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <script>
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>


    <link href="css/ui-darkness/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <!--<script src="js/jquery/jquery-1.10.1.min.js"></script> -->
    <script src="js/jquery/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/filterwizard.js"></script>

</head>
<body>

    <h3>Select a Datastore</h3>

    <table>
        <tr>
            <td style="width : 200px;">Datastore Type</td>
            <td style="width : 250px;">Datastore Host Name</td>
            <td></td>
        </tr>
        <tr>
            <td>
                <select id="datastore-select">
                    <option value="mongo">Mongo</option>
                    <option value="hive">Hive</option>
                </select>
            </td>
            <td>
                <div class="ui-widget">
                    <input id="hostname-input" />
                </div>
            </td>
            <td>
                <button id="datastore-button"> Continue</button>
            </td>
        </tr>
    </table>

    <br/>

    <div id="db-table">
        <h4> Select a Database and Table </h4>

        <table>
            <tr>
                <td style="width : 200px">Database Name</td>
                <td style="width : 250px">Table Name</td>
            </tr>
            <tr>
                <td>
                    <select id="database-select"></select>
                </td>
                <td>
                    <select id="table-select"></select>
                </td>
                <td>
                    <button id="database-table-button"> Continue</button>
                </td>
            </tr>
        </table>
    </div>

</body>
</html>