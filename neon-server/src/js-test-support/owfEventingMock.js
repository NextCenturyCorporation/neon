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



neon.mock.channelRegistry = {};

neon.mock.Channel = function() {
    this.callbacks = [];
}

OWF.Util = {
    isRunningInOWF: function(){
        return true;
    }
};

OWF.Eventing.subscribe = function(channelName, callback) {
    if ( !(channelName in neon.mock.channelRegistry) ) {
        neon.mock.channelRegistry[channelName] = new neon.mock.Channel();
    }
    neon.mock.channelRegistry[channelName].callbacks.push(callback)
};

OWF.Eventing.publish = function(channelName, message) {
    var channel = neon.mock.channelRegistry[channelName];
    channel.callbacks.forEach(function(callback) {
        callback.call(null, 'owfEventingMockSender', message);
    });

};

neon.mock.clearChannels = function() {
    neon.mock.channelRegistry = {};
};