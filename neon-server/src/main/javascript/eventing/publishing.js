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

/**
 Wraps neon query functions that should automatically fire OWF events.
 @class neon.eventing.publishing
 @static
 **/

neon.eventing.publishing = (function () {

    function createChannelCallback(channelName, successCallback){
        var callback = function(results){
            if(successCallback && typeof successCallback === 'function'){
                successCallback();
            }
            neon.eventing.messaging.publish(channelName, results || {});
        };
        return callback;
    }

    return {
        /**
         * Fires a filter changed event.
         * See {{#crossLink "neon.query.Query/addFilter:method"}}{{/crossLink}}
         * @method addFilter
         */
        addFilter: function(filterKey, filter, successCallback, errorCallback){
            neon.query.addFilter(filterKey, filter, createChannelCallback(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a filter changed event.
         * See {{#crossLink "neon.query.Query/removeFilter:method"}}{{/crossLink}}
         * @method removeFilter
         */
        removeFilter: function(filterKey, successCallback, errorCallback){
            neon.query.removeFilter(filterKey, createChannelCallback(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a filter changed event.
         * See {{#crossLink "neon.query.Query/replaceFilter:method"}}{{/crossLink}}
         * @method replaceFilter
         */
        replaceFilter: function(filterKey, filter, successCallback, errorCallback){
            neon.query.replaceFilter(filterKey, filter, createChannelCallback(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a filter changed event.
         * See {{#crossLink "neon.query.Query/clearFilters:method"}}{{/crossLink}}
         * @method clearFilters
         */
        clearFilters: function (successCallback, errorCallback) {
            neon.query.clearFilters(createChannelCallback(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/setSelectionWhere:method"}}{{/crossLink}}
         * @method setSelectionWhere
         */
        setSelectionWhere: function(filter, successCallback, errorCallback){
            neon.query.setSelectionWhere(filter, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/setSelectedIds:method"}}{{/crossLink}}
         * @method setSelectedIds
         */
        setSelectedIds: function(ids, successCallback, errorCallback){
            neon.query.setSelectedIds(ids, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/addSelectedIds:method"}}{{/crossLink}}
         * @method addSelectedIds
         */
        addSelectedIds: function (ids, successCallback, errorCallback) {
            neon.query.addSelectedIds(ids, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/removeSelectedIds:method"}}{{/crossLink}}
         * @method removeSelectedIds
         */
        removeSelectedIds: function (ids, successCallback, errorCallback) {
            neon.query.removeSelectedIds(ids, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/clearSelection:method"}}{{/crossLink}}
         * @method clearSelection
         */
        clearSelection: function (successCallback, errorCallback) {
            neon.query.clearSelection(createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        }
    };

})();