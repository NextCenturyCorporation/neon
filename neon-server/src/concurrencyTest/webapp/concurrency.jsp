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

<button id="mongo">Mongo@localhost</button>
<br/>
<button id="query" onclick="query('mydb', 'things');">Query</button>

<div id="results"></div>


</body>
</html>