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


neon.mock.owf.channels = {};
neon.mock.owf.runningInOWF = false;

OWF.Util = {
    isRunningInOWF: function () {
        return neon.mock.owf.runningInOWF;
    }
};

OWF.Eventing.subscribe = function (channelName, callback) {
    neon.mock.owf.channels[channelName] = callback;
};

OWF.Eventing.publish = function (channelName, message) {
    var callback = neon.mock.owf.channels[channelName];
    if (callback) {
        callback('owfEventingMockSender', message, channelName);
    }
};

OWF.Eventing.unsubscribe = function (channelName) {
    delete neon.mock.owf.channels[channelName];
};

neon.mock.owf.clearChannels = function () {
    neon.mock.owf.channels = {};
};