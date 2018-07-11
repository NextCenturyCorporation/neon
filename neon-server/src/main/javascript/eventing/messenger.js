/*
 * Copyright 2014 Next Century Corporation
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

// Note: There is a single instance of an event bus shared by all messengers

/**
 * Creates the event bus to be used for communication between widgets. If running outside of OWF,
 * If in OWF, a {{#crossLink "neon.eventing.EventBus"}}{{/crossLink}} is returned. If running inside of OWF, a
 * {{#crossLink "neon.eventing.owf.OWFEventBus"}}{{/crossLink}} is returned.
 * @return {neon.eventing.EventBus | neon.eventing.owf.OWFEventBus}
 * @private
 * @method createEventBus_
 */
neon.eventing.createEventBus_ = function() {
    if(neon.util.owfUtils.isRunningInOWF()) {
        return new neon.eventing.owf.OWFEventBus();
    }
    return new neon.eventing.EventBus(this.id_);
};

/**
 * The event bus used for communication between widgets
 * @property eventBus_
 * @type {neon.eventing.EventBus|neon.eventing.owf.OWFEventBus}
 * @private
 */
neon.eventing.eventBus_ = neon.eventing.createEventBus_();

/**
 * A messenger is a widget's gateway to the event bus used for communication between widgets. Widgets publish and
 * subscribe to messages through a messenger. Each messenger has a unique id to identify which widget published the
 * message to ensure that widgets are not receiving their own messages to facilitate simpler widget code.
 * @class neon.eventing.Messenger
 * @constructor
 */

neon.eventing.Messenger = function() {
    this.id_ = uuid.v4();
    this.channels = [];
};

/**
 * Publishes a message to a channel
 * @param {String} channel The channel to publish the message to
 * @param {Object} message The message to publish
 * @method publish
 */
neon.eventing.Messenger.prototype.publish = function(channel, message) {
    //var data = { payload: message, sender: this.id_ };
    neon.eventing.eventBus_.publish(channel, message, this.id_);
};

/**
 * Subscribes to the channel to receives any messages published to it. Note that messengers will not receive their
 * own messages even if they are subscribed to that channel.
 * @param {String} channel The channel to subscribe to
 * @param {Function} callback The callback to invoke when a message is received on the channel. The function takes
 * @param {Object|String} callback.message the message that was published
 * @method subscribe
 */
neon.eventing.Messenger.prototype.subscribe = function(channel, callback) {
    neon.eventing.eventBus_.subscribe(channel, callback, this.id_);
    if(this.channels.indexOf(channel) < 0) {
        this.channels.push(channel);
    }
};

/**
 * Unsubscribes this messenger from the channel
 * @param {String} channel The channel to unsubscribe from
 * @method unsubscribe
 */
neon.eventing.Messenger.prototype.unsubscribe = function(channel) {
    neon.eventing.eventBus_.unsubscribe(channel, this.id_);
    var index = this.channels.indexOf(channel);
    if(index >= 0) {
        this.channels.splice(index, 1);
    }
};

/**
 * Unsubscribes this messenger from all its channels, including events
 * @method unsubscribeAll
 */
neon.eventing.Messenger.prototype.unsubscribeAll = function() {
    var me = this;
    var allChannels = me.channels.slice();
    _.each(allChannels, function(channel) {
        me.unsubscribe(channel);
    });
};

/**
 * Subscribe to events notifications
 * @param callbacks {object} An object containing callback functions. Each function takes one
 * parameter, the message that was published.
 * <ul>
 *     <li>selectionChanged - function to execute when the selection has changed</li>
 *     <li>filtersChanged - function to execute when the filters have been changed</li>
 *     <li>connectToHost - function to execute when the Connection.connect() has been called on a host.</li>
 * </ul>
 * @method events
 */
neon.eventing.Messenger.prototype.events = function(callbacks) {
    var me = this;
    var globalChannelConfigs = this.createGlobalChannelSubscriptions_(callbacks);
    _.each(globalChannelConfigs, function(channelConfig) {
        me.subscribe(channelConfig.channel, function(payload) {
                if(channelConfig.callback && typeof channelConfig.callback === 'function') {
                    channelConfig.callback(payload);
                }
            }
        );
    });
};

/**
 * Unsubscribe from all events:  selection changes, filters changes, active dataset changed.
 * @method removeEvents
 */
neon.eventing.Messenger.prototype.removeEvents = function() {
    var me = this;
    var globalChannelConfigs = this.createGlobalChannelSubscriptions_({});
    _.each(globalChannelConfigs, function(channelConfig) {
        me.unsubscribe(channelConfig.channel);
    });
};

neon.eventing.Messenger.prototype.createGlobalChannelSubscriptions_ = function(neonCallbacks) {
    // some of the callbacks may be null/undefined, which is ok since they will be ignored
    return [
        {
            channel: neon.eventing.channels.SELECTION_CHANGED,
            callback: neonCallbacks.selectionChanged
        },
        {
            channel: neon.eventing.channels.FILTERS_CHANGED,
            callback: neonCallbacks.filtersChanged
        },
        {
            channel: neon.eventing.channels.CONNECT_TO_HOST,
            callback: neonCallbacks.connectToHost
        }
    ];
};

/**
 * Adds a filter to the data being viewed, so only data matching that filter is returned from queries. This will
 * fire a filter changed event to notify other widgets that the filters have changed. If a filter with this id
 * already exists, the filter is appended to that one with an AND clause. To replace the filter entirely, use
 * {{#crossLink "neon.eventing.Messenger/replaceFilter:method"}}{{/crossLink}}.
 * @param {String} id The id of the filter to apply. A typical value to use for the id is the return value of
 * {{#crossLink "neon.widget/getInstanceId:method"}}{{/crossLink}}
 * @param {neon.query.Filter} filter The filter to add
 * @param {Function} successCallback The callback to invoke when the filter is added
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method addFilter
 */
neon.eventing.Messenger.prototype.addFilter = function(id, filter, successCallback, errorCallback) {
    var filterKey = this.createFilterKey_(id, filter);
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.serviceUrl('filterservice', 'addfilter'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback),
            error: errorCallback
        }
    );
};

neon.eventing.Messenger.prototype.addFilters = function(idAndFilterList, successCallback, errorCallback) {
    var me = this;
    var successes = [];
    var completedQueries = 0;
    var successCB = function(returnedData) {
        successes.push(returnedData.addedFilter);
        checkIfComplete();
    };
    var checkIfComplete = function() {
        if (++completedQueries === idAndFilterList.length) {
            if (successes.length > 0) {
                var channelCallback = me.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback);
                channelCallback(successes);
            } else {
                errorCallback();
            }
        }
    };
    idAndFilterList.forEach(function(idAndFilterPair) {
        var filterKey = me.createFilterKey_(idAndFilterPair[0], idAndFilterPair[1]);
        neon.util.ajaxUtils.doPostJSON(
            filterKey,
            neon.serviceUrl('filterservice', 'addfilter'),
            {
                success: successCB,
                error: checkIfComplete
            }
        );
    });
};

/**
 * Removes a previously added filter. This will fire a filter changed event to notify other widgets that the filters
 * have changed.
 * @param {String} id The id of the filter to remove. This will be the same id used to create the filter.
 * @param {Function} successCallback The callback to invoke when the filter is removed
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method removeFilter
 */
neon.eventing.Messenger.prototype.removeFilter = function(id, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('filterservice', 'removefilter'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback),
            error: errorCallback,
            data: id,
            contentType: 'text/plain',
            responseType: 'json'
        }
    );
};

neon.eventing.Messenger.prototype.removeFilters = function(idList, successCallback, errorCallback) {
    var me = this;
    var successes = [];
    var completedQueries = 0;
    var successCB = function(returnedData) {
        successes.push(returnedData.removedFilter);
        checkIfComplete();
    };
    var checkIfComplete = function() {
        if (++completedQueries === idList.length) {
            if (successes.length > 0) {
                var channelCallback = me.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback);
                channelCallback(successes);
            } else {
                errorCallback();
            }
        }
    };
    idList.forEach(function(id) {
        neon.util.ajaxUtils.doPost(
            neon.serviceUrl('filterservice', 'removefilter'),
            {
                success: successCB,
                error: checkIfComplete,
                data: id,
                contentType: 'text/plain',
                responseType: 'json'
            }
        );
    });
};

/**
 * Replaces a previously added filter. This is a similar to calling remove and then add, but does so with one
 * notification event.
 * @param {String} id The id of the filter to replace.
 * @param {neon.query.Filter} filter The filter to replace the old one with
 * @param {Function} successCallback The callback to invoke when the filter is replaced
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method replaceFilter
 */
neon.eventing.Messenger.prototype.replaceFilter = function(id, filter, successCallback, errorCallback) {
    var filterKey = this.createFilterKey_(id, filter);
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.serviceUrl('filterservice', 'replacefilter'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback),
            error: errorCallback
        }
    );
};

neon.eventing.Messenger.prototype.replaceFilters = function(idAndFilterList, successCallback, errorCallback) {
    var me = this;
    var successes = [];
    var completedQueries = 0;
    var successCB = function(returnedData) {
        successes.push({
            added: returnedData.addedFilter,
            removed: returnedData.removedFilter
        });
        checkIfComplete();
    };
    var checkIfComplete = function() {
        if (++completedQueries === idAndFilterList.length) {
            if (successes.length > 0) {
                var channelCallback = me.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback);
                channelCallback(successes);
            } else {
                errorCallback();
            }
        }
    };
    idAndFilterList.forEach(function(idAndFilterPair) {
        var filterKey = me.createFilterKey_(idAndFilterPair[0], idAndFilterPair[1]);
        neon.util.ajaxUtils.doPostJSON(
            filterKey,
            neon.serviceUrl('filterservice', 'replacefilter'),
            {
                success: successCB,
                error: checkIfComplete
            }
        );
    });
};

/**
 * Clears all filters. This will fire a filter changed event to notify other widgets that the filters have changed.
 * @method clearFilters
 */
neon.eventing.Messenger.prototype.clearFilters = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('filterservice', 'clearfilters'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback),
            error: errorCallback,
            responseType: 'json'
        }
    );
};

/**
 * Clears all filters but does not notify other widgets that the filters have been changed. This is useful if this is
 * part of another event that may result in the widgets updating themselves
 * @method clearFiltersSilently
 */
neon.eventing.Messenger.prototype.clearFiltersSilently = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('filterservice', 'clearfilters'),
        {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};

/**
 * Adds any elements matching the filter to the current selection. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * @param {String} id The id of the selection to apply. A typical value to use for the id is the return value of
 * {{#crossLink "neon.widget/getInstanceId:method"}}{{/crossLink}}
 * @param {neon.query.Filter} filter Items matching this filter will be marked as selected
 * @param {Function} successCallback The callback to invoke when the selection is added
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method addSelection
 */
neon.eventing.Messenger.prototype.addSelection = function(id, filter, successCallback, errorCallback) {
    var filterKey = this.createFilterKey_(id, filter);
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.serviceUrl('selectionservice', 'addselection'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback),
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Removes this filter from the selected items. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * @param {String} id The id of the selection to remove. This will be the same id used to create the selection.
 * @param {Function} successCallback The callback to invoke when the selection is removed
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method removeSelection
 */
neon.eventing.Messenger.prototype.removeSelection = function(id, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('selectionservice', 'removeselection'),
        {
            success:  this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback),
            error: errorCallback,
            global: false,
            data: id,
            contentType: 'text/plain',
            responseType: 'json'
        }
    );
};

/**
 * Replaces a previously added selection. This is a similar to calling remove and then add, but does so with one
 * notification event.
 * @param {String} id The id of the selection to replace.
 * @param {neon.query.Filter} filter The filter that defines the new selection
 * @param {Function} successCallback The callback to invoke when the selection is replaced
 * @param {Function} errorCallback The callback to invoke if an error occurs.
 * @method replaceSelection
 */

neon.eventing.Messenger.prototype.replaceSelection = function(id, filter, successCallback, errorCallback) {
    var filterKey = this.createFilterKey_(id, filter);
    return neon.util.ajaxUtils.doPostJSON(
        filterKey,
        neon.serviceUrl('selectionservice', 'replaceselection'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback),
            error: errorCallback,
            global: false
        }
    );
};

/**
 * Clears the currently selected items. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * @method clearSelection
 */
neon.eventing.Messenger.prototype.clearSelection = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('selectionservice', 'clearselection'),
        {
            success: this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback),
            error: errorCallback,
            global: false,
            responseType: 'json'
        }
    );
};

neon.eventing.Messenger.prototype.createFilterKey_ = function(id, filter) {
    return {
        id: id,
        filter: filter
    };
};

neon.eventing.Messenger.prototype.createChannelCallback_ = function(channelName, successCallback) {
    var me = this;
    var callback = function(results) {
        // pass the results in the callback and publish them to the channel so callers can listen
        // in either place
        if(successCallback && typeof successCallback === 'function') {
            successCallback(results);
        }
        me.publish(channelName, results || {});
    };
    return callback;
};
