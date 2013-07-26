<%@page contentType="text/html;charset=UTF-8" %>
<%@page pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html>
<head>
    <title>Filter Builder</title>

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

    <!-- build:js js/filter-builder.js -->
    <script src="jquery/jquery-1.10.1.min.js"></script>
    <script src="jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="handlebars/handlebars.js"></script>
    <script src="filterwizard.js"></script>
    <script src="filtertable.js"></script>
    <!-- endbuild -->

    <script id="filters" type="text/x-handlebars-template">
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
                    <select id="operator-select-{{@index}}" class="input-small">
                        {{#select operatorValue}}
                        {{#operatorOptions}}
                        <option value="{{.}}">{{.}}</option>
                        {{/operatorOptions}}
                        {{/select}}
                    </select>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    {{#escapeQuotes value}}
                    <input type="text" id="value-input-{{@index}}" value="{{value}}"/>
                    {{/escapeQuotes}}
                </div>
            </div>

            <div class="control-group">
                {{#unless submittable}}
                <div class="btn-group">
                    <button class="btn btn-primary" id="add-filter-button" onclick="neon.filter.addFilter({{@index}})">
                        Add
                    </button>
                </div>
                {{/unless}}
                {{#if submittable}}
                <div class="btn-group">
                    <button class="btn btn-primary" id="add-filter-button" onclick="neon.filter.addFilter({{@index}})">
                        Update
                    </button>
                </div>
                <div class="btn-group">
                    <button class="btn btn-danger" id="remove-filter-button"
                            onclick="neon.filter.removeFilter({{@index}})">Remove
                    </button>
                </div>
                {{/if}}
            </div>
        </div>
        {{/data}}

    </script>


</head>
<body>

<div class="container">
    <div id="datastore">
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
                    <input type="text" id="hostname-input" value="localhost"/>
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

    <fieldset id="filter-container">
        <legend>Filters</legend>
        <div class="controls controls-row">
            <button class="btn btn-danger" id="clear-filters-button">Clear All Filters</button>
        </div>

        <div class="controls controls-row">
            <div class="control-group">
                <label class="group-label">Column Name</label>
            </div>
            <div class="control-group">
                <label class="group-label-small">Operator</label>
            </div>
            <div class="control-group">
                <label class="group-label">Value</label>
            </div>
            <div class="control-group">
                <label class="radio inline">
                    <input type="radio" name="boolean" value="AND" checked/>
                    AND
                </label>
                <label class="radio inline">
                    <input type="radio" name="boolean" value="OR"/>
                    OR
                </label>
            </div>
        </div>
        <div id="filter-content"/>

    </fieldset>


</div>

</body>
</html>