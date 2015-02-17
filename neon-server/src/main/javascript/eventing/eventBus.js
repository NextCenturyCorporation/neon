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
 * The neon event bus that is used to coordinate messages between widgets. This implementation
 * is used when not running in an OWF environment (the {{#crossLink "neon.eventing.owf.OWFEventBus"}}{{/crossLink}}
 exists for that).
 * @class neon.eventing.EventBus
 * @constructor
 */
neon.eventing.EventBus = function () {
    // postal.js has channels and topics. channels provide ways to group multiple topics. neon only
    // uses one postal channel, and each neon channel will correspond to a postal topic
    this.channel_ = postal.channel();
};

/**
 * Publishes a message to the channel. The message can be any type of object. Those subscribed to
 * the channel will be notified of the message.
 * @param {String} channel The name of the channel to publish the message to
 * @param {Object} message The message
 * @param {String} messengerId The id of the owning neon messenger
 * @method publish
 *
 */
neon.eventing.EventBus.prototype.publish = function (channel, message, messengerId) {
    var data = { payload: message, sender: messengerId};
    this.channel_.publish(channel, data);
};

/**
 * Subscribes to messages on the channel. The callback will be invoked synchronously
 * when a message is received on the channel.
 * @param {String} channel The name of the channel to subscribe to
 * @param {Function} callback The callback to invoke when a message is published to the channel.
 * @param {String|Object} The message published to the given channel
 * @param {String} messengerId The id of the owning neon messenger
 * @method subscribe
 * @return {Object} The subscription to the channel. Pass this to unsubscribe to stop receiving
 * messages for this subscription.
 */
neon.eventing.EventBus.prototype.subscribe = function (channel, callback, messengerId) {
    return this.channel_.subscribe(channel, function (data) {
        if(data.sender !== messengerId) {
            callback(data.payload);
        }
    });
};

/**
 * Unsubscribes the subscription created from the subscribe method.
 * @param {Object} subscription The subscription to remove from the bus
 * @method unsubscribe
 */
neon.eventing.EventBus.prototype.unsubscribe = function(subscription) {
    subscription.unsubscribe();
};
