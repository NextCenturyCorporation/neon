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

    <!-- build:js js/filter-builder.js -->
    <script src="jquery/jquery-1.10.1.min.js"></script>
    <script src="jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="handlebars/handlebars.js"></script>
    <script src="filterwizard.js"></script>
    <script src="filtertable.js"></script>
    <!-- endbuild -->

    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>


    <script id="filters" type="text/x-handlebars-template">
        {{#data}}
        <div class="controls controls-row">

            <div class="control-group">
                <div class="controls">
                    <select id="column-select-{{@index}}" class="dropdown">
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
                    <select id="operator-dropdown" id="operator-select-{{@index}}" class="dropdown" class="input-small">
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
                    <input type="text" id="value-input-{{@index}}" value="{{value}}" class="textfield"/>
                    {{/escapeQuotes}}
                </div>
            </div>

            <div class="control-group">
                {{#unless submittable}}
                <div class="btn-group">
                    <button class="btn" id="add-filter-button" onclick="neon.filter.addFilter({{@index}})">
                        Add
                    </button>

                </div>
                {{/unless}}
                {{#if submittable}}
                <div class="btn-group">
                    <button class="btn" id="add-filter-button" onclick="neon.filter.addFilter({{@index}})">
                        Update
                    </button>
                </div>

                <div class="btn-group">
                    <button class="btn" id="remove-filter-button"
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
    <div id="datastore-container">
        <div class="controls-row">

            <div class="control-group">
                <label class="control-label" for="datastore-select">Datastore Type</label>

                <div class="controls">
                    <select id="datastore-select" class="dropdown">
                        <option value="mongo">Mongo</option>
                        <option value="hive">Hive</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="hostname-input">Datastore Host Name</label>

                <div class="controls" class="textfield">
                    <input type="text" id="hostname-input" value="localhost"/>
                </div>
            </div>

            <div class="control-group">
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
                    <select id="database-select" class="dropdown">
                        <option value="">Select Database...</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="table-select">Table Name</label>

                <div class="controls">
                    <select id="table-select" class="dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <div class="controls">
                    <button class="btn" id="database-table-button">Continue</button>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-container">
        <h4>Filters</h4>

        <!-- OLD LOCATION OF RADIO-INLINE -->


        <div class="controls controls-row">
            <div class="control-group">
                <label id="column-name-label">Column Name</label>
            </div>
            <div class="control-group">
                <label id="operator-label">Operator</label>
            </div>
            <div class="control-group">
                <label id="value-label">Value</label>
            </div>
            <div class="control-group">
                <label class="radio-inline">
                    <input type="radio" name="boolean" value="AND" checked/>
                    AND
                </label>
                <label class="radio-inline">
                    <input type="radio" name="boolean" value="OR"/>
                    OR
                </label>
            </div>
        </div>
        <div id="filter-content"/>
    </div>
    <div class="controls controls-row">
        <button class="btn" id="clear-filters-button">Clear All Filters</button>
    </div>


</div>

</body>
</html>