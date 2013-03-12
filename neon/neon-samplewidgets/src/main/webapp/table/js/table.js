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

neon.namespace('neon.table');

neon.table.Table = function () {
    this.table_;
    var isMultiSelect = false;

    this.setData = function (json) {
        var keys = _.keys(json[0]);
        var columns = [];
        keys.forEach(function (key) {
            // underscore indicates a private field
            if ( key.charAt(0) !== '_') {
                columns.push({"mDataProp": key, "sTitle": key, "sDefaultContent": ""});
            }
        });

        this.table_ = $('#datatable').dataTable({
            bDestroy: true,
            "sDom": 'T<"clear">lfrtip',
            "aaData": json,
            "aoColumns": columns,
            "oTableTools": {
                "sRowSelect": "single",
                "aButtons": [ "select_all", "select_none" ]
            }
        });
    };

    // TODO: Fix this for when a user selects and then deselects
//    this.doRowsSelected_ = function (rows) {
//        var selectType = neon.table.Table.getTableTools_().s.select.type;
//        console.log('isMultiSelect = ' + isMultiSelect);
//        // clear out any old selection in case there was previously a multi selection
//        if ( selectType === "single") {
//            if ( isMultiSelect ) {
//                isMultiSelect = false;
//                console.log(rows);
//                neon.table.Table.getTableTools_().fnSelectNone();
//                neon.table.Table.getTableTools_().fnSelect(rows);
//            }
//        }
//        else {
//            isMultiSelect = true;
//        }
//    };
};

neon.table.Table.prototype.show = function () {
    $('#table').html('<table id="datatable"></table>');
    $(document).keydown(function (event) {
        if (event.ctrlKey) {
            neon.table.Table.getTableTools_().s.select.type = "multi";
        }
    });
    $(document).keyup(function (event) {
        if (event.ctrlKey) {
            neon.table.Table.getTableTools_().s.select.type = "single";
        }
    });
};

neon.table.Table.getTableTools_ = function () {
    return TableTools.fnGetInstance('datatable');
};