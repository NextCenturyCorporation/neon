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

var neon = neon || {};
neon.chartWidget = (function (){
    var databaseName;
    var tableName;
    var filterKey;
    var onChange;

    function onActiveDatasetChanged(message, changeHandler) {
        databaseName = message.database;
        tableName = message.table;
        onChange = changeHandler;

        neon.query.registerForFilterKey(databaseName, tableName, function(filterResponse){
            filterKey = filterResponse;
        });
        neon.query.getFieldNames(databaseName, tableName, populateFromColumns);
    }

    function populateFromColumns(data) {
        neon.dropdown.populateAttributeDropdowns(data, ['x', 'y'], onChange);
    }

    return {
        onActiveDatasetChanged: onActiveDatasetChanged,

        getXAttribute: function(){
            return $('#x option:selected').val();
        },
        getYAttribute: function(){
            return $('#y option:selected').val();
        },
        getDatabaseName: function(){
            return databaseName;
        },
        getTableName: function(){
            return tableName;
        },
        getFilterKey: function(){
            return filterKey;
        },
        setDatabaseName: function(dbName){
            databaseName = dbName;
        },
        setTableName: function(table){
            tableName = table;
        },
        setFilterKey: function(key){
            filterKey = key;
        }
    };

})();
