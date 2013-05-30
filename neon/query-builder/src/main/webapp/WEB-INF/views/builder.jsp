<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>
    <script src="js/jquery-1.9.1.js"></script>
</head>
<body>
    <h4>
    Enter a Query
    </h4>
    <textarea id="queryText">

    </textarea>
    <br/><br/>
    <button id="submit" onclick="submitter();">Submit</button>

    <script>
        function submitter(){
            var query = $('#queryText').val();
            $.post("", query, function(data){
                console.log(data);
            });
        }
    </script>

</body>
</html>