

/**
 A utility that allows a user to publish and subscribe to OWF events in Neon.
 Each widget should call registerForNeonEvents() on initialization in order to subscribe
 to Neon's filter, selection, and active callback events.
 @class neon.eventing.messaging
 @static
 **/

neon.eventing.messaging = (function () {

    /**
     * Gets the id of the iframe in which this widget exists
     * @method getIframeId
     * @return the iframe id
     */
    function getIframeId() {
        return OWF.getIframeId();
    }

    /**
     * Returns a uniqueId for a widget for this session.
     * @method getInstanceId
     * @return A unique identifier for this widget.
     */

    function getInstanceId() {
        return OWF.getInstanceId();
    }

    /**
     * Set the path to the rpc_relay.uncompressed.html file. The default is
     * 'js/eventing/rpc_replay.uncompressed.html'
     * @param relayPath {string} The path to the OWF rpc_relay file.
     * @method setRelayFile
     */
    function setRelayFile(relayPath) {
        OWF.relayFile = relayPath;
    }

    /**
     * Publish to an OWF channel
     * @param channel {string} The channel to which one can publish a message
     * @param message {object} The payload of the publication.
     * @method publish
     */

    function publish(channel, message) {
        OWF.Eventing.publish(channel, message);
    }

    /**
     * Subscribe to an OWF channel.
     * @param channel {string} The channel to which the callback is registered
     * @param callback {function} A function to execute when the channel receives the message. The function
     * can contain two parameters, the message that was published, and the sender that published the message.
     * The sender object will be equal to the OWF.getIframeId() of the publishing widget.
     * @method subscribe
     */
    function subscribe(channel, callback) {
        // We reverse the sender and message parameters here because typical usage requires the message
        // but not the sender. Callbacks can omit the second parameter if they do not need the sender.
        OWF.Eventing.subscribe(channel, function (sender, message) {
            callback(message, sender);
        });
    }

    /**
     * Subscribe to Neon's global events.
     * @param neonCallbacks {object} An object containing callback functions to Neon's events. Each function can contain
     * two parameters, the message published, and the sender that published the message.
     * <ul>
     *     <li>selectionChanged - function to execute when the selection has changed</li>
     *     <li>filtersChanged - function to execute when the filters have been changed</li>
     *     <li>activeDatasetChanged - function to execute when the active dataset has changed</li>
     * </ul>
     * @method registerForNeonEvents
     */
    function registerForNeonEvents(neonCallbacks) {
        var globalChannelConfigs = createGlobalChannelSubscriptions(neonCallbacks);
        _.each(globalChannelConfigs, function (channelConfig) {
            subscribe(channelConfig.channel, function (sender, message) {
                    if (channelConfig.callback && typeof channelConfig.callback === 'function') {
                        channelConfig.callback(sender, message);
                    }
                }
            );
        });
    }

    function createGlobalChannelSubscriptions(neonCallbacks) {
        neonCallbacks = neonCallbacks || {};
        return [
            {channel: neon.eventing.channels.SELECTION_CHANGED, callback: neonCallbacks.selectionChanged},
            {channel: neon.eventing.channels.FILTERS_CHANGED, callback: neonCallbacks.filtersChanged},
            {channel: neon.eventing.channels.ACTIVE_DATASET_CHANGED, callback: neonCallbacks.activeDatasetChanged}
        ];
    }

    return {
        getIframeId: getIframeId,
        getInstanceId: getInstanceId,
        setRelayFile: setRelayFile,
        registerForNeonEvents: registerForNeonEvents,
        subscribe: subscribe,
        publish: publish
    };

})();
