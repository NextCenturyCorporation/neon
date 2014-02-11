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
        <td><button id="mongo">Mongo@localhost</button></td>
        <td><button id="hive">Hive@shark</button></td>
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