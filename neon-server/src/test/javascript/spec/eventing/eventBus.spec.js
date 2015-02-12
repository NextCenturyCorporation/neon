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
        var eventBus = new neon.eventing.EventBus();

        var channel = 'aChannel';
        var message = 'theMessage';
        var callback = jasmine.createSpy();
        eventBus.subscribe(channel, callback, 'messengerId1');
        eventBus.publish(channel, message, 'messengerId2');
        expect(callback).toHaveBeenCalledWith(message);
    });

    it('can unsubscribe individual subscribers', function() {
        var eventBus = new neon.eventing.EventBus();
        var channel = 'aChannel';
        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        var subscriber1 = eventBus.subscribe(channel, callback1, 'messengerId1');
        var subscriber2 = eventBus.subscribe(channel, callback2, 'messengerId1');

        eventBus.publish(channel, 'aMessage', 'messengerId2');
        eventBus.unsubscribe(subscriber2, 'messengerId1');
        eventBus.publish(channel, 'bMessage', 'messengerId2');
        eventBus.unsubscribe(subscriber1, 'messengerId1');
        eventBus.publish(channel, 'cMessage', 'messengerId2');

        expect(callback1.callCount).toEqual(2);
        expect(callback2.callCount).toEqual(1);
    });

    it('can unsubscribe all individual subscribers from a channel', function() {
        var eventBus = new neon.eventing.EventBus();
        var channel = 'aChannel';
        var callback1 = jasmine.createSpy();
        var callback2 = jasmine.createSpy();

        var subscriber1 = eventBus.subscribe(channel, callback1, 'messengerId1');
        var subscriber2 = eventBus.subscribe(channel, callback2, 'messengerId1');

        eventBus.publish(channel, 'aMessage', 'messengerId2');
        eventBus.unsubscribe('aChannel', 'messengerId1');
        eventBus.publish(channel, 'bMessage', 'messengerId2');
        eventBus.publish(channel, 'cMessage', 'messengerId2');

        expect(callback1.callCount).toEqual(1);
        expect(callback2.callCount).toEqual(1);
    });

});
