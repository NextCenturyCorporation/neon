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
neon.eventing.createEventBus_ = function () {
    if (neon.util.owfUtils.isRunningInOWF()) {
        return new neon.eventing.owf.OWFEventBus();
    }
    return new neon.eventing.EventBus();
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

neon.eventing.Messenger = function () {
    this.id_ = uuid.v4();
};

/**
 * Publishes a message to a channel
 * @param {String} channel The channel to publish the message to
 * @param {Object} message The message to publish
 * @method publish
 */
neon.eventing.Messenger.prototype.publish = function (channel, message) {
    var data = { payload: message, sender: this.id_ };
    neon.eventing.eventBus_.publish(channel, data);
};

/**
 * Subscribes to the channel to receives any messages published to it. Note that messengers will not receive their
 * own messages even if they are subscribed to that channel.
 * @param {String} channel The channel to subscribe to
 * @param {Function} callback The callback to invoke when a message is received on the channel. The function takes
 * 1 parameter: the message that was published.
 * @method subscribe
 */
neon.eventing.Messenger.prototype.subscribe = function (channel, callback) {
    var me = this;
    neon.eventing.eventBus_.subscribe(channel, function (message) {
        if (message.sender !== me.id_) {
            callback(message.payload);
        }
    });
};

/**
 * Unsubscribes this messenger from the channel
 * @param {String} channel The channel to unsubscribe from
 * @method unsubscribe
 */
neon.eventing.Messenger.prototype.unsubscribe = function(channel) {
    neon.eventing.eventBus_.unsubscribe(channel);
};

/**
 * Subscribe to Neon's global events.
 * @param neonCallbacks {object} An object containing callback functions to Neon's events. Each function takes one
 * parameter, the message that was published.
 * <ul>
 *     <li>selectionChanged - function to execute when the selection has changed</li>
 *     <li>filtersChanged - function to execute when the filters have been changed</li>
 *     <li>activeConnectionChanged - function to execute when the active connection has changed</li>
 *     <li>activeDatasetChanged - function to execute when the active dataset has changed</li>
 * </ul>
 * @method registerForNeonEvents
 */
neon.eventing.Messenger.prototype.registerForNeonEvents = function (neonCallbacks) {
    var me = this;
    var globalChannelConfigs = this.createGlobalChannelSubscriptions_(neonCallbacks);
    _.each(globalChannelConfigs, function (channelConfig) {
        me.subscribe(channelConfig.channel, function (message) {
                if (channelConfig.callback && typeof channelConfig.callback === 'function') {
                    channelConfig.callback(message);
                }
            }
        );
    });
};

neon.eventing.Messenger.prototype.createGlobalChannelSubscriptions_ = function (neonCallbacks) {
    // some of the callbacks may be null/undefined, which is ok since they will be ignored
    return [
        {channel: neon.eventing.channels.SELECTION_CHANGED, callback: neonCallbacks.selectionChanged},
        {channel: neon.eventing.channels.FILTERS_CHANGED, callback: neonCallbacks.filtersChanged},
        {channel: neon.eventing.channels.ACTIVE_DATASET_CHANGED, callback: neonCallbacks.activeDatasetChanged}
    ];
};

/**
 * Adds a filter to the data being viewed, so only data matching that filter is returned from queries. This will
 * fire a filter changed event to notify other widgets that the filters have changed.
 * See {{#crossLink "neon.query.Query/addFilter:method"}}{{/crossLink}}
 * @method addFilter
 */
neon.eventing.Messenger.prototype.addFilter = function (filterKey, filter, successCallback, errorCallback) {
    neon.query.addFilter(filterKey, filter, this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
};

/**
 * Removes a previously added filter. This will fire a filter changed event to notify other widgets that the filters
 * have changed.
 * See {{#crossLink "neon.query.Query/removeFilter:method"}}{{/crossLink}}
 * @method removeFilter
 */
neon.eventing.Messenger.prototype.removeFilter = function (filterKey, successCallback, errorCallback) {
    neon.query.removeFilter(filterKey, this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
};

/**
 * Replaces a previously added filter. This is a similar to calling remove and then add, but the id of the filter does
 * not change. It replaces the filter rather than creating a new one. This will fire a filter changed event to notify
 * other widgets that the filters have changed.
 * See {{#crossLink "neon.query.Query/replaceFilter:method"}}{{/crossLink}}
 * @method replaceFilter
 */
neon.eventing.Messenger.prototype.replaceFilter = function (filterKey, filter, successCallback, errorCallback) {
    neon.query.replaceFilter(filterKey, filter, this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
};

/**
 * Clears all filters. This will fire a filter changed event to notify other widgets that the filters have changed.
 * See {{#crossLink "neon.query.Query/clearFilters:method"}}{{/crossLink}}
 * @method clearFilters
 */
neon.eventing.Messenger.prototype.clearFilters = function (successCallback, errorCallback) {
    neon.query.clearFilters(this.createChannelCallback_(neon.eventing.channels.FILTERS_CHANGED, successCallback), errorCallback);
};

/**
 * Adds any elements matching the filter to the current selection. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * See {{#crossLink "neon.query.Query/addSelection:method"}}{{/crossLink}}
 * @method addSelection
 */
neon.eventing.Messenger.prototype.addSelection = function (filterKey, filter, successCallback, errorCallback) {
    neon.query.addSelection(filterKey, filter, this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
};

/**
 * Removes this filter from the selected items. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * See {{#crossLink "neon.query.Query/removeSelection:method"}}{{/crossLink}}
 * @method removeSelection
 */
neon.eventing.Messenger.prototype.removeSelection = function (filterKey, successCallback, errorCallback) {
    neon.query.removeSelection(filterKey, this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
};

/**
 * Replaces the contents of the filter that is specifying which items are selected. This will fire a selection changed
 * event to notify other widgets that the selection has changed.
 * See {{#crossLink "neon.query.Query/replaceSelection:method"}}{{/crossLink}}
 * @method replaceSelection
 */
neon.eventing.Messenger.prototype.replaceSelection = function (filterKey, filter, successCallback, errorCallback) {
    neon.query.replaceSelection(filterKey, filter, this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
};

/**
 * Clears the currently selected items. This will fire a selection changed event to notify
 * other widgets that the selection has changed.
 * See {{#crossLink "neon.query.Query/clearSelection:method"}}{{/crossLink}}
 * @method clearSelection
 */
neon.eventing.Messenger.prototype.clearSelection = function (successCallback, errorCallback) {
    neon.query.clearSelection(this.createChannelCallback_(neon.eventing.channels.SELECTION_CHANGED, successCallback), errorCallback);
};

neon.eventing.Messenger.prototype.createChannelCallback_ = function (channelName, successCallback) {
    var me = this;
    var callback = function (results) {
        if (successCallback && typeof successCallback === 'function') {
            successCallback();
        }
        me.publish(channelName, results || {});
    };
    return callback;
};