/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

neon.ready(function () {
    neon.query.SERVER_URL = $("#neon-server").val();

    var table;
    var query;
    var state = neon.activeDataset;

    initialize();
    function initialize(){
        neon.eventing.messaging.registerForNeonEvents({
            activeDatasetChanged: onActiveDatasetChanged,
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

        neon.query.executeQuery(query, populateTable);
        neon.tableState.saveState(query);
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

    function createOptions(data){
        var _id = "_id";
        var has_id = true;

        _.each(data.data, function(element){
            if(!(_.has(element, _id))){
                has_id = false;
            }
        });

        var options = {data: data.data};

        if(has_id){
            options.id = _id;
        }
        return options;
    }

    function onSelection(idField, rows){
        var values = [];
        _.each(rows, function(row){
            values.push(row[idField]);
        });

        if(values.length > 0){
            var filterClause = neon.query.where(idField, "in", values);
            var filter = new neon.query.Filter().selectFrom(state.getDatabaseName(), state.getTableName()).where(filterClause);
            neon.eventing.publishing.replaceSelection(state.getFilterKey(), filter);
        }
        else{
            neon.eventing.publishing.removeSelection(state.getFilterKey());
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

    function onSelectionChanged(message, sender){
        if (sender === neon.eventing.messaging.getIframeId()) {
            neon.tableState.saveState(query, table.table_.getSelectedRows());
        }
    }

    function onActiveDatasetChanged(message) {
        state.setActiveDataset(message);
        state.getFieldNamesForDataset(neon.widget.TABLE, populateSortFieldDropdown);
        updateTable();
    }

    function populateSortFieldDropdown(data) {
        var element = new neon.dropdown.Element("sort-field");
        neon.dropdown.populateAttributeDropdowns(data, element, updateTable);
    }

    function restoreState() {
        neon.tableState.restoreState(function(data){
            query = data.query;
            if(data.sortColumns){
                populateSortFieldDropdown(data.sortColumns);
            }
            neon.dropdown.setDropdownInitialValue("sort-field", data.sortValue);
            styleSortDirectionButtonFromValue(data.sortDirection);
            neon.query.executeQuery(query, function(queryResults){
                populateTable(queryResults);
                if(data.selectedRows){
                    table.table_.setSelectedRows(data.selectedRows);
                }
            });
        });
    }

});
