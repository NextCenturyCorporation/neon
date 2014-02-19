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

describe('messaging using the standard neon event bus', function () {

    afterEach(function() {
        postal.utils.reset();
    });

    it('publish/subscribe message', function () {
        neon.eventing.eventBusTestUtils.testPublishSubscribe(new neon.eventing.EventBus());
    });

    it('can unsubscribe individual subscribers', function() {
        var eventBus = new neon.eventing.EventBus();
        var channel = 'aChannel';
        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        var subscriber1 = eventBus.subscribe(channel, callback1);
        var subscriber2 = eventBus.subscribe(channel, callback2);

        eventBus.publish(channel, 'aMessage');
        eventBus.unsubscribe(subscriber2);
        eventBus.publish(channel, 'bMessage');
        eventBus.unsubscribe(subscriber1);
        eventBus.publish(channel, 'cMessage');

        expect(callback1.callCount).toEqual(2);
        expect(callback2.callCount).toEqual(1);
    });

});
