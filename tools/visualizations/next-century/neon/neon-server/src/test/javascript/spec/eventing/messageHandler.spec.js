/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */
describe('message handler', function () {


    beforeEach(function() {
        neon.mock.clearChannels();
    });

    function testPublishedMessageReceived_(channelName, callbackName, params, context) {
        // were the ones that sent the message
        var expectedParams = {};
        _.extend(expectedParams,  params);

        var callback = jasmine.createSpy(channelName);
        var callbacks = {};
        callbacks[callbackName] = callback;
        callbacks.context = context;
        var messageHandler = new neon.eventing.MessageHandler(callbacks);
        messageHandler.publishMessage(channelName, params);

        // the parameters should have a source appended to it with the message handler's id so objects know if they
        expectedParams._source = messageHandler.id;
        expect(callback).toHaveBeenCalledWith(expectedParams);
    }

    it('should be notified when a message is published to the selection changed channel', function() {
        testPublishedMessageReceived_(neon.eventing.Channels.SELECTION_CHANGED, 'selectionChanged', {id:"selectionChanged"});
    });

    it('should be notified when a message is published to the filters changed channel', function() {
        testPublishedMessageReceived_(neon.eventing.Channels.FILTERS_CHANGED, 'filtersChanged', {id:"filtersChanged"});
    });

    it('should be notified when a message is published to the active dataset changed channel', function() {
        testPublishedMessageReceived_(neon.eventing.Channels.ACTIVE_DATASET_CHANGED, 'activeDatasetChanged', {id:"activeDatasetChanged"});
    });


    it('should use the passed in context', function() {
        // for this test, the callback should act on the the context object
        var called = false;
        var context = {};
        context.callMe = function() { called = true; };
        var callback = function() {
            this.callMe();
        };
        var messageHandler = new neon.eventing.MessageHandler({
            filtersChanged: callback,
            context: context
        });
        // params are unused in this test
        messageHandler.publishMessage(neon.eventing.Channels.FILTERS_CHANGED, {});
        expect(called).toBe(true);
    });
});
