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
 * 
 * @author dflynt
 */


/**
 * Executes the specified query and fires the callback when complete
 * @method executeQuery
 * @param {neon.query.Query} query the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.executeGraphQuery = function (query, successCallback, errorCallback) {
    return neon.query.executeGraphQueryService_(query, successCallback, errorCallback, 'query');
};

neon.query.executeGraphQueryService_ = function (query, successCallback, errorCallback, serviceName) {
    if(query.selectionOnly_){
        serviceName += "withselectiononly";
    }
    else if (query.disregardFilters_) {
        serviceName += "disregardfilters";
    }

    return neon.util.ajaxUtils.doPostJSON(
        query,
        neon.query.serviceUrl('graphqueryservice', serviceName),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


/*
* This function demonstrates retrieving and displaying graph which has layout
* coordinates.
*/
neon.ready(function () {
    neon.query.SERVER_URL = $("#neon-server").val();

    fetchData();

    function fetchData() {
        var query = new neon.query.Query().selectFrom('test', 'cpan');

        neon.query.executeGraphQuery(query, displayGraph);
    }

    function displayGraph(data) {
        // translate and scale gephi layout data to html positions
        // Note: the translation and scale values may need adjustment        
        var nodeArr = $.map(data.data[0].nodes, function(val, index) {

            function transAndScale(val) {
                return (val + 600)/2;
            }

            return {
                id: val.id,
                x: transAndScale(val.x),
                y: transAndScale(val.y),
                label: val.label,
                size: val.size
            };
        });

        // draw the graph
        var opts = {"x":"x","y":"y"};
        var graph = new graphs.Graph("#graph", opts);
        graph.draw(nodeArr, data.data[0].edges);       
    }
});
