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

/**
 A utility that allows a user to publish and subscribe to OWF events in Neon.
 Each widget should call registerForNeonEvents() on initialization in order to subscribe
 to Neon's filter, selection, and active callback events.
 @class neon.eventing.messaging
 @static
 **/

neon.eventing.messaging = (function () {

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
    function subscribe(channel, callback){
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
        registerForNeonEvents: registerForNeonEvents,
        subscribe: subscribe,
        publish: publish
    };

})();
