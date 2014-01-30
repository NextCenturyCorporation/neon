/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
         * See {{#crossLink "neon.query.Query/addSelection:method"}}{{/crossLink}}
         * @method addSelection
         */
        addSelection: function(filterKey, filter, successCallback, errorCallback){
            neon.query.addSelection(filterKey, filter, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/removeSelection:method"}}{{/crossLink}}
         * @method removeSelection
         */
        removeSelection: function(filterKey, successCallback, errorCallback){
            neon.query.removeSelection(filterKey, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
        },
        /**
         * Fires a selection changed event.
         * See {{#crossLink "neon.query.Query/replaceSelection:method"}}{{/crossLink}}
         * @method replaceSelection
         */
        replaceSelection: function (filterKey, filter, successCallback, errorCallback) {
            neon.query.replaceSelection(filterKey, filter, createChannelCallback(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
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