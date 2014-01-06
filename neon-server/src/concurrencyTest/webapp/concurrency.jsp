<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Concurrency Tester</title>

    <script src="js/neon.js"></script>
    <script src="js/concurrency.js"></script>

</head>
<body>

<table>
    <tr>
        <td>Connection</td>
        <td><button id="mongo">Mongo@xdata1</button></td>
        <td><button id="hive">Hive@xdata2</button></td>
    </tr>
    <tr>
        <td>Query</td>
        <td><button id="all-query">All Data Query</button></td>
        <td><button id="filtered-query">Filtered Query</button></td>
        <td><button id="selection-query">Selection Query</button></td>
    </tr>
    <tr>
        <td>Filter</td>
        <td><button id="add-filter">Add Filter</button></td>
        <td><button id="remove-filter">Remove Filter</button></td>
    </tr>
    <tr>
        <td>Selection</td>
        <td><button id="add-selection">Add Selection</button></td>
        <td><button id="remove-selection">Remove Selection</button></td>
    </tr>
</table>

<div id="results"></div>

</body>
</html>