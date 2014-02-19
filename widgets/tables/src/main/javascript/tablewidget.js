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


neon.ready(function () {
    neon.query.SERVER_URL = $("#neon-server").val();
    var widgetName = 'neon.table';

    var table;
    var query;
    var state = neon.activeDataset;
    var messenger = new neon.eventing.Messenger();


    initialize();
    function initialize() {
        messenger.registerForNeonEvents({
            activeDatasetChanged: onActiveDatasetChanged,
            activeConnectionChanged: onConnectionChanged,
            filtersChanged: updateTable,
            selectionChanged: onSelectionChanged
        });

        neon.toggle.createOptionsPanel("#options-panel", "table-options");
        populateSortDirection();
        addLimitListener();
        restoreState();

        $(window).resize(sizeTableToRemainingSpace);
        sizeTableToRemainingSpace();
    }

    function onConnectionChanged(id) {
        neon.tableState.setConnectionId(id);
    }

    function getSortField() {
        return $('#sort-field').val();
    }

    function populateSortDirection() {
        var ascending = $('#sort-ascending');
        var descending = $('#sort-descending');

        ascending.val(neon.query.ASCENDING);
        descending.val(neon.query.DESCENDING);

        ascending.click(updateSortDirection);
        descending.click(updateSortDirection);

        styleSortDirectionButtonFromValue(neon.query.ASCENDING);
    }

    function styleSortDirectionButtonFromValue(value) {
        var ascending = $('#sort-ascending');
        var descending = $('#sort-descending');
        ascending.removeClass('active');
        descending.removeClass('active');

        var button = ascending;
        if (parseInt(value) === neon.query.DESCENDING) {
            button = descending;
        }

        button.addClass('active');
        $('#sort-direction').val(button.val());
    }

    function updateSortDirection() {
        var sortVal = $(this).val();
        $('#sort-direction').val(sortVal);

        if (getSortField()) {
            updateTable();
        }
    }

    function updateTable() {
        query = new neon.query.Query().selectFrom(state.getDatabaseName(), state.getTableName());
        applyLimit(query);
        applySort(query);

        neon.query.executeQuery(neon.tableState.getConnectionId(), query, populateTable);
        neon.tableState.saveState(neon.query.getInstanceId(widgetName),query);
    }

    function applyLimit() {
        var limitVal = $('#limit').val();
        if (limitVal) {
            query.limit(parseInt(limitVal));
        }
    }

    function applySort() {
        var sortField = getSortField();
        if (sortField) {
            var sortDirection = $('#sort-direction').val();
            query.sortBy(sortField, sortDirection);
        }
    }

    function populateTable(data) {
        var options = createOptions(data);
        table = new tables.Table('#table', options).draw().registerSelectionListener(onSelection);
        sizeTableToRemainingSpace();
    }

    function createOptions(data) {
        var _id = "_id";
        var has_id = true;

        _.each(data.data, function (element) {
            if (!(_.has(element, _id))) {
                has_id = false;
            }
        });

        var options = {data: data.data};

        if (has_id) {
            options.id = _id;
        }
        return options;
    }

    function onSelection(idField, rows) {
        var values = [];
        _.each(rows, function (row) {
            values.push(row[idField]);
        });

        if (values.length > 0) {
            var filterClause = neon.query.where(idField, "in", values);
            var filter = new neon.query.Filter().selectFrom(state.getDatabaseName(), state.getTableName()).where(filterClause);
            messenger.replaceSelection(state.getFilterKey(), filter);
        }
        else {
            messenger.removeSelection(state.getFilterKey());
        }
    }

    function sizeTableToRemainingSpace() {
        if (table) {
            table.refreshLayout();
        }
    }

    function addLimitListener() {
        $('#limit').change(updateTable);
    }

    function onSelectionChanged(message) {
        neon.tableState.saveState(query, table.table_.getSelectedRows());
    }

    function onActiveDatasetChanged(message) {
        state.setActiveDataset(message);
        state.getFieldNamesForDataset(neon.tableState.getConnectionId(), neon.widget.TABLE, populateSortFieldDropdown);
        updateTable();
    }

    function populateSortFieldDropdown(data) {
        var element = new neon.dropdown.Element("sort-field");
        neon.dropdown.populateAttributeDropdowns(data, element, updateTable);
    }

    function restoreState() {
        neon.tableState.restoreState(neon.query.getInstanceId(widgetName), function (data) {
            query = data.query;
            if (data.sortColumns) {
                populateSortFieldDropdown(data.sortColumns);
            }
            neon.dropdown.setDropdownInitialValue("sort-field", data.sortValue);
            styleSortDirectionButtonFromValue(data.sortDirection);
            neon.query.executeQuery(neon.tableState.getConnectionId(), query, function (queryResults) {
                populateTable(queryResults);
                if (data.selectedRows) {
                    table.table_.setSelectedRows(data.selectedRows);
                }
            });
        });
    }

});
