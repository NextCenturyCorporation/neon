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
describe('messenger', function () {

    var databaseName = 'mockDatabaseName';
    var tableName = 'mockTableName';

    // store the isRunningInOWF method because we modify it in some tests
    var isRunningInOWF = neon.util.owfUtils.isRunningInOWF;


    afterEach(function () {
        neon.util.owfUtils.isRunningInOWF = isRunningInOWF;
        postal.utils.reset();
    });

    it('uses the OWF event bus when in OWF', function () {
        neon.util.owfUtils.isRunningInOWF = function () {
            return true;
        };
        var eventBus = neon.eventing.createEventBus_();
        expect(eventBus).toBeInstanceOf(neon.eventing.owf.OWFEventBus);
    });

    it('uses the neon event bus when not in OWF', function () {
        neon.util.owfUtils.isRunningInOWF = function () {
            return false;
        };
        var eventBus = neon.eventing.createEventBus_();
        expect(eventBus).toBeInstanceOf(neon.eventing.EventBus);
    });

    it('does not receive its own messages', function () {
        var channel = 'aChannel';
        var messenger = new neon.eventing.Messenger();
        var callback = jasmine.createSpy();
        messenger.subscribe(channel, callback);
        messenger.publish(channel, 'message');
        expect(callback).not.toHaveBeenCalled();
    });

    it('multiple messengers receive published message', function () {
        var channel = 'aChannel';
        var message = 'message';

        var messenger1 = new neon.eventing.Messenger();
        var callback1 = jasmine.createSpy();

        var messenger2 = new neon.eventing.Messenger();
        var callback2 = jasmine.createSpy();

        var messenger3 = new neon.eventing.Messenger();
        var callback3 = jasmine.createSpy();


        messenger1.subscribe(channel, callback1);
        messenger2.subscribe(channel, callback2);
        messenger3.subscribe(channel, callback3);

        messenger1.publish(channel, message);

        expect(callback1).not.toHaveBeenCalled();
        expect(callback2).toHaveBeenCalledWith(message);
        expect(callback3).toHaveBeenCalledWith(message);
    });

    it('should be notified when a message is published to the selection changed channel', function () {
        testGlobalNeonEventReceived(neon.eventing.channels.SELECTION_CHANGED, 'selectionChanged', {id: "selectionChanged"});
    });

    it('should be notified when a message is published to the filters changed channel', function () {
        testGlobalNeonEventReceived(neon.eventing.channels.FILTERS_CHANGED, 'filtersChanged', {id: "filtersChanged"});
    });

    it('should be notified when a message is published to the active dataset changed channel', function () {
        testGlobalNeonEventReceived(neon.eventing.channels.ACTIVE_DATASET_CHANGED, 'activeDatasetChanged', {id: "activeDatasetChanged"});
    });

    function testGlobalNeonEventReceived(channelName, callbackName, message) {
        var messenger = new neon.eventing.Messenger();
        var callback = jasmine.createSpy(channelName);
        var callbacks = {};
        callbacks[callbackName] = callback;
        messenger.registerForNeonEvents(callbacks);
        new neon.eventing.Messenger().publish(channelName, message);
        expect(callback).toHaveBeenCalledWith(message);
    }

    it('should publish add filter results', function () {
        var filterKey = {
            uuid: "84bc5064-c837-483b-8454-c8c72abe45f8",
            dataSet: {
                databaseName: databaseName,
                tableName: tableName
            }
        };
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'addFilter',
            [filterKey, filter]
        );
    });

    it('should publish remove filter results', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'removeFilter',
            ["filterId"]
        );
    });

    it('should publish clear filter results', function () {

        testResultsPublishedToChannel(
            neon.eventing.channels.FILTERS_CHANGED,
            'clearFilters',
            []
        );
    });

    it('should publish add selection', function () {
        var filterKey = {
            uuid: "84bc5064-c837-483b-8454-c8c72abe45f8",
            dataSet: {
                databaseName: databaseName,
                tableName: tableName
            }
        };
        var filter = new neon.query.Filter().selectFrom(databaseName, tableName);
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'addSelection',
            [filterKey, filter]
        );
    });

    it('should publish remove selection', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'removeSelection',
            ["filterId"]
        );
    });

    it('should publish clear selection', function () {
        testResultsPublishedToChannel(
            neon.eventing.channels.SELECTION_CHANGED,
            'clearSelection',
            []
        );
    });

    /**
     * Executes a query method and tests that the results are published to the specified channel
     * @param channel The channel the message should be published to
     * @param methodName The method executed on the messenger. A method with the same name on the query
     * executor will be mocked out since the messenger just delegates to the query executor.
     * @param args The args to that query method
     */
    function testResultsPublishedToChannel(channel, methodName, args) {
        var callback = jasmine.createSpy();


        // not all of the real methods actually return any results , but for this test it doesn't matter
        var mockResults = {mock: "results"};
        var delegateSpy = spyOn(neon.query, methodName).andCallThrough();
        neon.mock.AjaxMockUtils.mockNextAjaxCall(mockResults);

        var subscriber = new neon.eventing.Messenger();
        subscriber.subscribe(channel, callback);

        var publisher = new neon.eventing.Messenger();
        neon.eventing.Messenger.prototype[methodName].apply(publisher, args);

        expect(callback).toHaveBeenCalledWith(mockResults);

        // verify that the correct delegate method on the query executor was actually called
        expect(delegateSpy).toHaveBeenCalled();
    }

});