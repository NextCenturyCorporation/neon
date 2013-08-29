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

$(document).ready(function () {

    OWF.ready(function () {

        var databaseName;
        var tableName;
        var table;

        // just creating the message handler will register the listeners
        var messageHandler = new neon.eventing.MessageHandler({
            activeDatasetChanged: populateInitialData,
            filtersChanged: updateTable

        });

        function populateInitialData(message) {
            saveDatasetInfo(message);
            neon.query.getFieldNames(databaseName, tableName, populateSortFieldDropdown);
            updateTable();
        }

        function saveDatasetInfo(message) {
            databaseName = message.database;
            tableName = message.table;
        }

        function populateSortFieldDropdown(data) {
            var select = $('#sort-field');
            select.empty();
            select.append($('<option></option>').attr('value', '').text('(Select Field)'));
            data.fieldNames.forEach(function (field) {
                select.append($('<option></option>').attr('value', field).text(field));
            });
            select.change(updateSortField);
        }

        function getSortField() {
            return $('#sort-field').val();
        }

        function updateSortField() {
            if (getSortField()) {
                updateTable();
            }
        }

        function populateSortDirection() {
            var ascending = $('#sort-ascending');
            var descending = $('#sort-descending');

            ascending.val(neon.query.ASCENDING);
            descending.val(neon.query.DESCENDING);

            ascending.click(updateSortDirection);
            descending.click(updateSortDirection);

            var defaultButton = ascending;
            defaultButton.addClass('active');
            $('#sort-direction').val(defaultButton.val());
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
            if ($('#limit')[0].validity.valid) {
                applyLimit(query);
            }
            else {
                return;
            }
            applySort(query);
            neon.query.executeQuery(query, populateTable);
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
            $('#table').css('top', $('#table-options').position().top + $('#table-options').outerHeight());
            // table may not be drawn yet
            if (table) {
                table.refreshLayout();
            }
        }

        function addLimitListener() {
            $('#limit').change(updateTable);
        }

        populateSortDirection();
        addLimitListener();

        $(window).resize(sizeTableToRemainingSpace);
        sizeTableToRemainingSpace();

    });


});
