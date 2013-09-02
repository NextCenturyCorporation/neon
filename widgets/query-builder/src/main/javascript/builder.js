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

neon.queryBuilder = (function(){
    var table = new tables.Table('#results', {data: []});
    var numberOfRows = 0;

    function resizeHeightOfResultDivAndRefreshGridLayout(){
        $("#results").height(function(){
            var windowHeight = $(window).height();
            var formHeight = $("#queryForm").height();
            //36 == margin-top(20) + (2 * padding (8)).
            var containerHeight = windowHeight - formHeight - 36;
            var rowsHeight = (numberOfRows + 1) * 28;

            if(rowsHeight > containerHeight){
                return containerHeight;
            }
            return rowsHeight;
        });
        table.refreshLayout();
    }

    function submitQueryToServer() {
        $("#errorText").empty();
        var query = $('#queryText').val();
        neon.util.AjaxUtils.doPost(neon.query.SERVER_URL + "/services/languageservice/query",
            {
                data: { text: query },
                success: function (data) {
                    $('#results').empty();
                    numberOfRows = data.data.length;
                    neon.queryBuilder.layoutResults();
                    table = new tables.Table('#results', {data: data.data, gridOptions: {fullWidthRows: true}}).draw();
                }
            });
    }

    return {
        displayError: function(text){
            $("#errorText").append(text);
        },
        layoutResults: resizeHeightOfResultDivAndRefreshGridLayout,
        submit: submitQueryToServer
    };
})();

