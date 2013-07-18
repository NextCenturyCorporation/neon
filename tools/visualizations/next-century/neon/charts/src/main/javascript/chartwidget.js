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

// this file contains some javascript functions that are useful for the chart based widgets
var datasource;
var datasetId;

function populateAttributeDropdowns(message, changeHandler) {
    datasource = message.database;
    datasetId = message.table;
    neon.query.getFieldNames(datasource, datasetId, function(data){
        doPopulateAttributeDropdowns(data,changeHandler);
    });
}

function doPopulateAttributeDropdowns(data, changeHandler) {
    ['x', 'y'].forEach(function (selectId) {
        var select = $('#' + selectId);
        select.empty();
        select.append($('<option></option>').attr('value', '').text('(Select Field)'));
        data.fieldNames.forEach(function (field) {
            select.append($('<option></option>').attr('value', field).text(field));
        });
        select.change(changeHandler);
    });
}

function getXAttribute() {
    return $('#x option:selected').val();
}

function getYAttribute() {
    return $('#y option:selected').val();
}