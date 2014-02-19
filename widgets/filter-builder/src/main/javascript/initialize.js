/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

$(function () {

    initializeWidget();
    neon.filterBuilderState.restoreState();

    function initializeWidget() {
        neon.query.SERVER_URL = $("#neon-server").val();
        createHandlebarsHelpers();
        hideWizardSteps();
        setupHostnames();
        addClickHandlers();
    }

    function createHandlebarsHelpers() {
        Handlebars.registerHelper('select', function (context, options) {
            var el = $('<select />').html(options.fn(this));
            el.find('option').filter(function () {
                return this.value === context;
            }).attr('selected', 'selected');
            return el.html();
        });
        Handlebars.registerHelper('escapeQuotes', function (context, options) {
            var el = $('<div/>').html(options.fn(this));
            if (context === "") {
                el.find('input').attr('value', '""');
            }
            return el.html();
        });
    }

    function hideWizardSteps() {
        $("#db-table").hide();
        $("#filter-container").hide();
        $("#clear-filters-button").hide();
    }

    function setupHostnames() {
        neon.query.connection.getHostnames(
            function (data) {
                $("#hostname-input").autocomplete({
                    source: data
                });
            }
        );
    }

    function addClickHandlers() {
        $("#datastore-button").click(connectToDatastore);
        $("#database-table-button").click(selectDatabaseAndTable);
        $("#clear-filters-button").click(clearAllFilters);
        $('#database-select').change(populateTableDropdown);
        $('#table-select').change(neon.filterBuilderState.saveState);
    }

    function connectToDatastore() {
        $("#db-table").show();
        var databaseSelectedOption = $('#datastore-select option:selected').val();
        var hostnameSelector = $('#hostname-input').val();
        var connection = new neon.query.connection.Connection(databaseSelectedOption, hostnameSelector);
        neon.query.connection.connectToDatastore(connection, populateDatabaseDropdown);
    }

    function populateDatabaseDropdown(id) {
        neon.filterBuilderState.setConnectionId(id);
        neon.filterTable.messenger.publish(neon.eventing.channels.ACTIVE_CONNECTION_CHANGED, id);
        neon.query.getDatabaseNames(neon.filterBuilderState.getConnectionId(), function (databaseNames) {
            neon.wizard.populateDropdown('#database-select', databaseNames);
            populateTableDropdown();
        });
    }

    function populateTableDropdown() {
        var selectedDatabase = $('#database-select option:selected').val();
        neon.query.getTableNames(neon.filterBuilderState.getConnectionId(), selectedDatabase, function (tableNames) {
            neon.wizard.populateDropdown('#table-select', tableNames);
            neon.filterTable.setColumns([]);
            neon.filterTable.initializeFilterSection();
            neon.filterBuilderState.saveState();
        });
    }

    function selectDatabaseAndTable() {
        var dataSet = neon.wizard.getDataset();
        var executingInitFilterSection = _.after(3, initFilterForm);

        neon.query.clearFilters(function () {
            executingInitFilterSection();
        });

        neon.query.registerForFilterKey(dataSet.database, dataSet.table, function (filterResponse) {
            neon.filterTable.setFilterKey(filterResponse);
            executingInitFilterSection();
        });

        neon.query.getFieldNames(neon.filterBuilderState.getConnectionId(), dataSet.database, dataSet.table, "", function (data) {
            neon.filterTable.setColumns(data.data);
            executingInitFilterSection();
        });

        broadcastActiveDataset();
    }

    function initFilterForm() {
        neon.filterTable.initializeFilterSection();
    }

    function clearAllFilters() {
        neon.filterTable.messenger.clearFilters(function () {
            var database = $('#database-select').val();
            var table = $('#table-select').val();
            var message = { "database": database, "table": table };
            neon.filterTable.initializeFilterSection();
        });
    }

    function broadcastActiveDataset() {
        var database = $('#database-select').val();
        var table = $('#table-select').val();
        var message = { "database": database, "table": table };
        neon.filterTable.messenger.publish(neon.eventing.channels.ACTIVE_DATASET_CHANGED, message);
    }

});