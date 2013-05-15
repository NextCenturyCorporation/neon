<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>
</head>
<body>
    <h4>
    Enter a Query
    </h4>
    <form:form id="query-form" method="post" modelAttribute="query">
        <form:textarea path="text" cols="80" rows="3"/>
        <br/>
        <input type="Submit" value="Submit Query"/>
    </form:form>

</body>
</html>