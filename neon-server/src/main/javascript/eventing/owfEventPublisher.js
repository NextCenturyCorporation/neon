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


neon.eventing.owfEventPublisher = (function () {

    function createChannelCallback(channelName, successCallback){
        var callback = function(results){
            if(successCallback){
                successCallback();
            }
            neon.eventing.messageHandler.publish(channelName, results || {});
        };
        return callback;
    }

    return {
        addFilter: function(filterKey, filter, successCallback, errorCallback){
            neon.query.addFilter(filterKey, filter, createChannelCallback(neon.eventing.Channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        removeFilter: function(filterKey, successCallback, errorCallback){
            neon.query.removeFilter(filterKey, createChannelCallback(neon.eventing.Channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        replaceFilter: function(filterKey, filter, successCallback, errorCallback){
            neon.query.replaceFilter(filterKey, filter, createChannelCallback(neon.eventing.Channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        clearFilters: function (successCallback, errorCallback) {
            neon.query.clearFilters(createChannelCallback(neon.eventing.Channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        setSelectionWhere: function(filter, successCallback, errorCallback){
            neon.query.setSelectionWhere(filter, createChannelCallback(neon.eventing.Channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        setSelectedIds: function(ids, successCallback, errorCallback){
            neon.query.setSelectedIds(ids, createChannelCallback(neon.eventing.Channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        addSelectedIds: function (ids, successCallback, errorCallback) {
            neon.query.addSelectedIds(ids, createChannelCallback(neon.eventing.Channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        removeSelectedIds: function (ids, successCallback, errorCallback) {
            neon.query.removeSelectedIds(ids, createChannelCallback(neon.eventing.Channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        clearSelection: function (successCallback, errorCallback) {
            neon.query.clearSelection(createChannelCallback(neon.eventing.Channels.SELECTION_CHANGED, successCallback), errorCallback);
        }
    };

})();