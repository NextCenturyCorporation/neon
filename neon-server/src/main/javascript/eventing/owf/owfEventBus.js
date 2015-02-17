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

/**
 * An implementation of an event bus that uses OWF's publish/subscribe messaging
 * @class neon.eventing.owf.OWFEventBus
 * @constructor
 */
neon.eventing.owf.OWFEventBus = function() {
    this.subscriptions = {};
};

// The OWF publish/subscribe methods are "static", but we still encapsulate them in an instance
// of the OWFEventBus to make it easier for messaging implementations to be swapped. They all run
// from their own widget and each call is specific to the widget it is running in.

/**
 * Publishes the message to the specified channel
 * @param {String} channel The name of the channel to publish to
 * @param {Object} message The message to publish to the channel
 * @method publish
 */
neon.eventing.owf.OWFEventBus.prototype.publish = function(channel, message) {
    OWF.Eventing.publish(channel, message);
};

/**
 * Subscribes to the channel so the callback is invoked when a message is published to the channel
 * Note OWF cannot handle multiple channel subscriptions, so this will replace the existing subscription.
 * @param {String} channel The name of the channel to subscribe to
 * @param {Function} callback The callback to invoke when a message is published to the channel. It takes
 * one parameter, the message.
 * @method subscribe
 * @return {Object} The subscription to the channel. Pass this to the unsubscribe method to remove this
 */
neon.eventing.owf.OWFEventBus.prototype.subscribe = function(channel, callback) {
    var subscriptionHandler;

    if(this.subscriptions[channel]) {
        var existing = this.subscriptions[channel];
        subscriptionHandler = function(message) {
            existing(message);
            callback(message);
        };
    } else {
        subscriptionHandler = callback;
    }
    this.subscriptions[channel] = subscriptionHandler;

    OWF.Eventing.subscribe(channel, function(sender, message) {
        if(sender !== OWF.getInstanceId()) {
            subscriptionHandler(message);
        }
    });
    return channel;
};

/**
 * Unsubscribes from the specified channel.
 * Note OWF cannot remove a single channel subscription, so this will unsubscribe from all channels.
 * @param {Object} subscription The subscription to remove.
 * @method unsubscribe
 */
neon.eventing.owf.OWFEventBus.prototype.unsubscribe = function(channel) {
    delete this.subscriptions[channel];
    OWF.Eventing.unsubscribe(channel);
};
