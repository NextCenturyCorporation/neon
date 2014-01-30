
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

});
