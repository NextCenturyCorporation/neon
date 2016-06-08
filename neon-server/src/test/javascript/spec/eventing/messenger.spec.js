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
describe('messenger', function() {
    var databaseName = 'mockDatabaseName';
    var tableName = 'mockTableName';

    // store the isRunningInOWF method because we modify it in some tests
    var isRunningInOWF = neon.util.owfUtils.isRunningInOWF;

    afterEach(function() {
        neon.util.owfUtils.isRunningInOWF = isRunningInOWF;
        postal.utils.reset();
    });

    it('uses the OWF event bus when in OWF', function() {
        neon.util.owfUtils.isRunningInOWF = function() {
            return true;
        };
        var eventBus = neon.eventing.createEventBus_();
        expect(eventBus).toBeInstanceOf(neon.eventing.owf.OWFEventBus);
    });

    it('uses the neon event bus when not in OWF', function() {
        neon.util.owfUtils.isRunningInOWF = function() {
            return false;
        };
        var eventBus = neon.eventing.createEventBus_();
        expect(eventBus).toBeInstanceOf(neon.eventing.EventBus);
    });

    it('does not receive its own messages', function() {
        var channel = 'aChannel';
        var messenger = new neon.eventing.Messenger();
        var callback = jasmine.createSpy();
        messenger.subscribe(channel, callback);
        messenger.publish(channel, 'message');
        expect(callback).not.toHaveBeenCalled();
    });

    it('multiple messengers receive published message', function() {
        var channel = 'aChannel';
        var message = 'message';

        var messenger1 = new neon.eventing.Messenger();
        var callback1 = jasmine.createSpy('callback1');

        var messenger2 = new neon.eventing.Messenger();
        var callback2 = jasmine.createSpy('callback2');

        var messenger3 = new neon.eventing.Messenger();
        var callback3 = jasmine.createSpy('callback3');

        messenger1.subscribe(channel, callback1);
        messenger2.subscribe(channel, callback2);
        messenger3.subscribe(channel, callback3);

        messenger1.publish(channel, message);

        expect(callback1).not.toHaveBeenCalled();
        expect(callback2).toHaveBeenCalledWith(message);
        expect(callback3).toHaveBeenCalledWith(message);
    });

    it('unsubscribes messages from a channel using neon event bus', function() {
        var channel = 'aChannel';
        var message1 = 'message1';
        var message2 = 'message2';

        var messenger1 = new neon.eventing.Messenger();
        var messenger2 = new neon.eventing.Messenger();
        var messenger3 = new neon.eventing.Messenger();

        var callback1 = jasmine.createSpy('callback1');
        var callback2 = jasmine.createSpy('callback2');
        var callback3 = jasmine.createSpy('callback3');

        messenger1.subscribe(channel, callback1);
        messenger1.subscribe(channel, callback2);
        messenger2.subscribe(channel, callback3);

        messenger3.publish(channel, message1);

        expect(callback1).toHaveBeenCalledWith(message1);
        expect(callback2).toHaveBeenCalledWith(message1);
        expect(callback3).toHaveBeenCalledWith(message1);

        messenger1.unsubscribe(channel);
        messenger3.publish(channel, message2);

        expect(callback1).not.toHaveBeenCalledWith(message2);
        expect(callback2).not.toHaveBeenCalledWith(message2);
        expect(callback3).toHaveBeenCalledWith(message2);
    });

    it('subscribes to each Neon event type on events()', function() {
        var messenger1 = new neon.eventing.Messenger();
        var messenger2 = new neon.eventing.Messenger();
        var callback1 = jasmine.createSpy('callback1');
        var callback2 = jasmine.createSpy('callback2');
        var callback3 = jasmine.createSpy('callback3');

        messenger1.events({
            connectToHost: callback1,
            filtersChanged: callback2,
            selectionChanged: callback3
        });
        messenger2.publish(neon.eventing.channels.CONNECT_TO_HOST, {
            id: "connectToHost"
        });
        messenger2.publish(neon.eventing.channels.FILTERS_CHANGED, {
            id: "filtersChanged"
        });
        messenger2.publish(neon.eventing.channels.SELECTION_CHANGED, {
            id: "selectionChanged"
        });

        expect(callback1).toHaveBeenCalledTimes(1);
        expect(callback2).toHaveBeenCalledTimes(1);
        expect(callback3).toHaveBeenCalledTimes(1);
    });

    it('unsubscribes from all Neon event type on removeEvents()', function() {
        var messenger1 = new neon.eventing.Messenger();
        var messenger2 = new neon.eventing.Messenger();
        var callback1 = jasmine.createSpy('callback1');
        var callback2 = jasmine.createSpy('callback2');
        var callback3 = jasmine.createSpy('callback3');

        messenger1.events({
            connectToHost: callback1,
            filtersChanged: callback2,
            selectionChanged: callback3
        });
        messenger1.removeEvents();

        messenger2.publish(neon.eventing.channels.CONNECT_TO_HOST, {
            id: "connectToHost"
        });
        messenger2.publish(neon.eventing.channels.FILTERS_CHANGED, {
            id: "filtersChanged"
        });
        messenger2.publish(neon.eventing.channels.SELECTION_CHANGED, {
            id: "selectionChanged"
        });

        expect(callback1).not.toHaveBeenCalled();
        expect(callback2).not.toHaveBeenCalled();
        expect(callback3).not.toHaveBeenCalled();
    });

    it('should be notified when a message is published to the selection changed channel', function() {
        testGlobalNeonEventReceived(neon.eventing.channels.SELECTION_CHANGED, 'selectionChanged', {
            id: "selectionChanged"
        });
    });

    it('should be notified when a message is published to the filters changed channel', function() {
        testGlobalNeonEventReceived(neon.eventing.channels.FILTERS_CHANGED, 'filtersChanged', {
            id: "filtersChanged"
        });
    });

    it('should be notified when a message is published to the connect to host channel', function() {
        testGlobalNeonEventReceived(neon.eventing.channels.CONNECT_TO_HOST, 'connectToHost', {
            id: "connectToHost"
        });
    });

    function testGlobalNeonEventReceived(channelName, callbackName, message) {
        var messenger = new neon.eventing.Messenger();
        var callback = jasmine.createSpy(channelName);
        var callbacks = {};
        callbacks[callbackName] = callback;
        messenger.events(callbacks);

        var newMessenger = new neon.eventing.Messenger();
        newMessenger.publish(channelName, message);

        expect(callback).toHaveBeenCalledWith(message);
    }

    it('should publish add filter results', function() {
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'addFilter',
            ["filterA", filter]
        );
    });

    it('should publish remove filter results', function() {
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'removeFilter',
            ["filterId"]
        );
    });

    it('should publish clear filter results', function() {
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'clearFilters',
            []
        );
    });

    it('should not publish the filter event when clearing filters silently', function() {
        var channelCallback = jasmine.createSpy('channelCallback');
        var successCallback =  jasmine.createSpy('successCallback');
        neon.mock.AjaxMockUtils.mockNextAjaxCall({});

        var subscriber = new neon.eventing.Messenger();
        subscriber.subscribe(neon.eventing.channels.FILTERS_CHANGED, channelCallback);

        var publisher = new neon.eventing.Messenger();
        publisher.clearFiltersSilently(successCallback);

        expect(channelCallback).not.toHaveBeenCalled();
        expect(successCallback).toHaveBeenCalled();
    });

    it('should publish add selection', function() {
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'addSelection',
            ["filterId", filter]
        );
    });

    it('should publish remove selection', function() {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'removeSelection',
            ["filterId"]
        );
    });

    it('should publish clear selection', function() {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'clearSelection',
            []
        );
    });

    /**
     * Executes a query method and tests that the results are published to the specified channel
     * @param channel The channel the message should be published to
     * @param methodName
     * @param args The args to that query method
     */
    function testResultsPublishedToChannel(channel, methodName, args) {
        var callback = jasmine.createSpy();

        // not all of the real methods actually return any results , but for this test it doesn't matter
        var mockResults = {
            mock: "results"
        };
        neon.mock.AjaxMockUtils.mockNextAjaxCall(mockResults);

        var subscriber = new neon.eventing.Messenger();
        subscriber.subscribe(channel, callback);

        var publisher = new neon.eventing.Messenger();
        neon.eventing.Messenger.prototype[methodName].apply(publisher, args);

        expect(callback).toHaveBeenCalledWith(mockResults);
    }
});
