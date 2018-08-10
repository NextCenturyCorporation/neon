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

describe('messaging using the OWF event bus', function() {
    afterEach(function() {
        neon.mock.owf.clearChannels();
    });

    it('publish/subscribe OWF messages', function() {
        OWF.getInstanceId = function() {
            return uuid.v4();
        };

        neon.eventing.eventBusTestUtils.testPublishSubscribe(new neon.eventing.owf.OWFEventBus());
    });

    it('supports multiple subscribers', function() {
        OWF.getInstanceId = function() {
            return uuid.v4();
        };

        //Previously:
        // as mentioned in the owfEventBus publish/subscribe docs, OWF only supports a single
        // subscriber per channel
        // https://groups.google.com/forum/#!searchin/ozone-developers/eventing/ozone-developers/MbQXWy8vXiA/Q3GwJuW3grQJ

        //The OWF channel subscribe has been wrapped to allow for multiple handlers

        var eventBus = new neon.eventing.owf.OWFEventBus();
        var channel = 'aChannel';
        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        eventBus.subscribe(channel, callback1);
        eventBus.publish(channel, 'aMessage');

        //this is added as a second subscription handler
        var subscriber = eventBus.subscribe(channel, callback2);
        eventBus.publish(channel, 'bMessage');

        //verify that unsubsribe removes all handlers
        eventBus.unsubscribe(subscriber);
        eventBus.publish(channel, 'cMessage');

        expect(callback1).toHaveBeenCalledTimes(2);
        expect(callback2).toHaveBeenCalledTimes(1);
    });
});
