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

$(function () {

    OWF.ready(function () {
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = $("#neon-server").val();

        var databaseName;
        var tableName;
        var table;

        var clientId = OWF.getInstanceId();

        neon.eventing.messageHandler.subscribeToNeonEvents({
            activeDatasetChanged: populateInitialData,
            filtersChanged: updateTable
        });

        neon.toggle.createOptionsPanel("#options-panel", "table-options");
        populateSortDirection();
        addLimitListener();
        restoreState();

        $(window).resize(sizeTableToRemainingSpace);
        sizeTableToRemainingSpace();

        function populateInitialData(message) {
            databaseName = message.database;
            tableName = message.table;
            neon.query.getFieldNames(databaseName, tableName, neon.widget.TABLE, populateSortFieldDropdown);
        }

        function populateSortFieldDropdown(data) {
            neon.dropdown.populateAttributeDropdowns(data, 'sort-field', updateTable);
            updateTable();
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

        function styleSortDirectionButtonFromValue(value){
            var ascending = $('#sort-ascending');
            var descending = $('#sort-descending');
            ascending.removeClass('active');
            descending.removeClass('active');

            var button = ascending;
            if(parseInt(value) === neon.query.DESCENDING){
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
            var query = new neon.query.Query().selectFrom(databaseName, tableName);
            applyLimit(query);
            applySort(query);

            var stateObject = buildStateObject(query);
            neon.query.executeQuery(query, populateTable);
            neon.query.saveState(clientId, stateObject);
        }

        function applyLimit(query) {
            var limitVal = $('#limit').val();
            // make sure there is a value - it could be empty (no limit) which is valid
            if (limitVal) {
                query.limit(parseInt(limitVal));
            }
        }

        function applySort(query) {
            var sortField = getSortField();
            if (sortField) {
                var sortDirection = $('#sort-direction').val();
                query.sortBy(sortField, sortDirection);
            }
        }

        function populateTable(data) {
            table = new tables.Table('#table', {data: data.data}).draw();
            sizeTableToRemainingSpace();
        }

        function sizeTableToRemainingSpace() {
            // table may not be drawn yet
            if (table) {
                table.refreshLayout();
            }
        }

        function addLimitListener() {
            $('#limit').change(updateTable);
        }

        function buildStateObject(query) {
            return {
                databaseName: databaseName,
                tableName: tableName,
                limitValue: $('#limit').val(),
                sortColumns: neon.dropdown.getFieldNamesFromDropdown("sort-field"),
                sortValue: getSortField(),
                sortDirection: $('#sort-direction').val(),
                query: query
            };
        }

        function restoreState() {
            neon.query.getSavedState(clientId, function (data) {
                databaseName = data.databaseName;
                tableName = data.tableName;
                $('#limit').val(data.limitValue);
                neon.dropdown.populateAttributeDropdowns(data.sortColumns, 'sort-field', updateTable);
                neon.dropdown.setDropdownInitialValue("sort-field", data.sortValue);
                styleSortDirectionButtonFromValue(data.sortDirection);
                neon.query.executeQuery(data.query, populateTable);
            });
        }

    });

});
