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


describe('message handler', function () {

    beforeEach(function() {
        neon.mock.clearChannels();
    });

    function testPublishedMessageReceived(channelName, callbackName, params) {
        // were the ones that sent the message
        var callback = jasmine.createSpy(channelName);
        var callbacks = {};
        callbacks[callbackName] = callback;
        neon.eventing.messaging.registerForNeonEvents(callbacks);
        neon.eventing.messaging.publish(channelName, params);

        // the parameters should have a source appended to it with the message handler's id so objects know if they
        expect(callback).toHaveBeenCalledWith(params, 'owfEventingMockSender');
    }

    it('should be notified when a message is published to the selection changed channel', function() {
        testPublishedMessageReceived(neon.eventing.channels.SELECTION_CHANGED, 'selectionChanged', {id:"selectionChanged"});
    });

    it('should be notified when a message is published to the filters changed channel', function() {
        testPublishedMessageReceived(neon.eventing.channels.FILTERS_CHANGED, 'filtersChanged', {id:"filtersChanged"});
    });

    it('should be notified when a message is published to the active dataset changed channel', function() {
        testPublishedMessageReceived(neon.eventing.channels.ACTIVE_DATASET_CHANGED, 'activeDatasetChanged', {id:"activeDatasetChanged"});
    });

    it('should be notified when a message is published to the active connection changed channel', function() {
        testPublishedMessageReceived(neon.eventing.channels.ACTIVE_CONNECTION_CHANGED, 'activeConnectionChanged', {id:"activeConnectionChanged"});
    });

});
