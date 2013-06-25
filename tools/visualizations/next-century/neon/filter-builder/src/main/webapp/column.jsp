<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <title>Query Builder</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/filter.css">
    <link href="css/ui-darkness/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">

    <script src="js/jquery/jquery-1.10.1.min.js"></script>
    <script src="js/jquery/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js/handlebars.js"></script>
    <script src="js/filtertable.js"></script>

</head>
<body>
    <div class="container" id="filter-content">

    </div>

    <script id="filters" type="text/x-handlebars-template">
        <h3>Add Global Filters</h3>

        <div class="controls controls-row">
            <div class="control-group">
                <label class="group-label">Column Name</label>
            </div>
            <div class="control-group">
                <label class="group-label">Operator</label>
            </div>
            <div class="control-group">
                <label class="group-label">Value</label>
            </div>
            <div class="control-group">
                <label class="radio inline">
                    <input type="radio" name="boolean" checked/>
                    AND
                </label>
                <label class="radio inline">
                    <input type="radio" name="boolean" />
                    OR
                </label>
            </div>
        </div>

        {{#data}}
        <div class="controls controls-row">

            <div class="control-group">
                <div class="controls">
                    <select id="column-select-{{@index}}">
                        <option value="">Select Column...</option>
                        {{#select columnValue}}
                        {{#columnOptions}}
                        <option value="{{.}}">{{.}}</option>
                        {{/columnOptions}}
                        {{/select}}
                    </select>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <select id="operator-select-{{@index}}">
                        {{#select operatorValue}}
                        {{#operatorOptions}}
                        <option value="{{value}}">{{text}}</option>
                        {{/operatorOptions}}
                        {{/select}}
                    </select>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <input type="text" id="value-input-{{@index}}" value="{{value}}"/>
                </div>
            </div>

            <div class="control-group">
                {{#unless reachedServer}}
                <div class="btn-group">
                    <button class="btn btn-primary" id="add-filter-button" onclick="neon.filter.addFilter({{@index}})">Add</button>
                </div>
                {{/unless}}
                {{#if reachedServer}}
                <div class="btn-group">
                    <button class="btn btn-danger" id="remove-filter-button" onclick="neon.filter.removeFilter({{@index}})">Remove</button>
                </div>
                {{/if}}
            </div>
        </div>
        {{/data}}

    </script>

    <script>
        $(function(){
            neon.filter.grid(["x","y","z"]);
        });

    </script>

</body>
</html>