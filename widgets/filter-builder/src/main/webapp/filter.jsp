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
<%@taglib prefix="neon" uri="http://nextcentury.com/tags/neon" %>

<!DOCTYPE html>
<html>
<head>
    <title>Filter Builder</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="<neon:neon-url/>/css/neon.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/filter.css">
    <link href="css/ui-darkness/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">

    <script src="<neon:owf-url/>/js/owf-widget.js"></script>
    <script src="<neon:neon-url/>/js/neon.js"></script>

    <!-- build:js js/filter-builder.js -->
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js-lib/handlebars/handlebars.js"></script>
    <script src="js/dropdown.js"></script>
    <script src="javascript/state.js"></script>
    <script src="javascript/initialize.js"></script>
    <script src="javascript/filterwizard.js"></script>
    <script src="javascript/filtertable.js"></script>
    <!-- endbuild -->

    <script id="filters" type="text/x-handlebars-template">
        {{#data}}
        <div class="controls controls-row">

            <div class="filter-dropdown">
                <div class="controls">
                    <select id="column-select-{{@index}}" style="width:100%">
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
                    <select id="operator-select-{{@index}}" class="span1">
                        {{#select operatorValue}}
                        {{#operatorOptions}}
                        <option value="{{.}}">{{.}}</option>
                        {{/operatorOptions}}
                        {{/select}}
                    </select>
                </div>
            </div>

            <div class="filter-dropdown">
                <div class="controls">
                    {{#escapeQuotes value}}
                    <input type="text" id="value-input-{{@index}}" value="{{value}}" class="textfield" style="width:90%"/>
                    {{/escapeQuotes}}
                </div>
            </div>

            <div class="control-group">
                {{#unless submittable}}
                <div class="btn-group">
                    <button class="btn" id="add-filter-button" onclick="neon.filterTable.addFilter({{@index}})">
                        Add
                    </button>

                </div>
                {{/unless}}
                {{#if submittable}}
                <div class="btn-group">
                    <button class="btn btn-mini" id="update-filter-button" onclick="neon.filterTable.addFilter({{@index}})">
                        Update
                    </button>
                </div>

                <div class="btn-group">
                    <button class="btn btn-mini" id="remove-filter-button" onclick="neon.filterTable.removeFilter({{@index}})">
                        Remove
                    </button>
                </div>
                {{/if}}
            </div>
        </div>
        {{/data}}

    </script>


</head>
<body>
<neon:hidden-neon-server/>

<div class="container">
    <div id="datastore-container">
        <h4>Connection</h4>

        <div class="controls-row">

            <div class="control-group">
                <label class="control-label" for="datastore-select">Type</label>

                <div class="controls">
                    <select id="datastore-select" class="span2">
                        <option value="mongo">Mongo</option>
                        <option value="hive">Hive</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="hostname-input">Host</label>

                <div class="controls" class="textfield">
                    <input class="span2" type="text" id="hostname-input" value="localhost"/>
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
        <h4>Database</h4>

        <div class="controls controls-row">
            <div class="control-group">
                <label class="control-label" for="database-select">Database</label>

                <div class="controls">
                    <select id="database-select" class="span2">
                        <option value="">Select Database...</option>
                    </select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="table-select">Table</label>

                <div class="controls">
                    <select id="table-select" class="span2"></select>
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

        <div id="filter-content"/>

    </div>
    <div class="controls controls-row">
        <button id="clear-filters-button" class="btn">Clear All Filters</button>

        <div class="control-group" id="radio-buttons">
            <label class="radio inline control-label">
                <input type="radio" name="boolean" value="AND" checked/>AND
            </label>
            <label class="radio inline control-label">
                <input type="radio" name="boolean" value="OR"/>OR
            </label>
        </div>
    </div>
</div>

</body>
</html>