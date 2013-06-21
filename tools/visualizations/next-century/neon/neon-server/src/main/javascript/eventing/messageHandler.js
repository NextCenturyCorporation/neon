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
 * This class is used for sending and receiving messages on OWF channels
 *
 * @namespace neon.eventing
 * @class MessageHandler
 * @param callbackOpts An associative array mapping of function callbacks
 * @example
 *     { selectionChanged: selectionChangedCallback, filtersChanged: filtersChangedCallback }
 * @constructor
 */
neon.eventing.MessageHandler = function (callbackOpts) {

    var me = this;
    var registerChannels = function() {
        _.each([
            {channel: neon.eventing.Channels.SELECTION_CHANGED, callback: me.selectionChanged},
            {channel: neon.eventing.Channels.FILTERS_CHANGED, callback: me.filtersChanged},
            {channel: neon.eventing.Channels.ACTIVE_DATASET_CHANGED, callback: me.activeDatasetChanged}

        ],
            function (channelConfig) {
                OWF.Eventing.subscribe(channelConfig.channel,
                    function (sender, message) {
                        channelConfig.callback.call(me.context, message);
                    }
                );
            });
    };

    this.id =  uuid.v4();

    /**
     * The callback function that is invoked when a the selection of items changes
     * @property selectionChanged
     * @type {Function}
     */
    this.selectionChanged = function (message) {
    };

    /**
     * The callback function that is invoked when the currently applied filters change
     * @property filtersChanged
     * @type {Function}
     */
    this.filtersChanged = function (message) {
    };

    /**
     * The callback function that is invoked when the active dataset changes
     * @property activeDatasetChanged
     * @type {Function}
     */
    this.activeDatasetChanged = function (message) {
    };

    /**
     * The *this* context used for the function callbacks
     * @property context
     * @type {Object}
     */
    this.context = null;

    _.extend(this, callbackOpts);

    registerChannels();

};



/**
 * Publishes the message to a channel with the specified name
 * @method publishMessage
 * @param channel The name of the channel to publish the message
 * @param message The message to send. Note that the message gets a _source property added to it with
 * the id of the message handler that sent it so receivers can ignore their own messages if desired
 */
neon.eventing.MessageHandler.prototype.publishMessage = function (channel, message) {
    message._source = this.id;
    OWF.Eventing.publish(channel, message);
};