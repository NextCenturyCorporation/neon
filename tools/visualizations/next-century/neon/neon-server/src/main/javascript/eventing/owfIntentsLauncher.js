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
 * Used for launching a widget's intents and instructing other widgets to display the data
 * @namespace neon.eventing
 * @class OWFIntentsLauncher
 */

/**
 * @class OWFIntentsLauncher
 * @constructor
 */
neon.eventing.OWFIntentsLauncher = function () {

    this.metadata_ = {};

};

/**
 * Adds any metadata associated with a particular data type. That metadata as part of the message to the
 * widget handling the intent.
 * @method addMetadataForDataType
 * @param {String} dataType
 * @param {Object} metadata Some json metadata
 */
neon.eventing.OWFIntentsLauncher.prototype.addMetadataForDataType = function (dataType, metadata) {
    if (!this.metadata_.hasOwnProperty(dataType)) {
        this.metadata_[dataType] = {};
    }
    _.extend(this.metadata_[dataType], metadata);
};

/**
 * Adds a visual component to select the intents to launch
 * @method addIntentsSelector
 * @param {neon.eventing.OWFIntentsLauncher} launcher The launcher that launches the intents
 * @param {Array} intents An array of the intent descriptions that can be launched
 * @param {Function} databaseNameLookup A function that returns the name of the database to pass with the intent
 * @param {Function} tableNameLookup A function that returns the name of the table to pass with the intent
 * @param {String} parentSelector The CSS selector to find the parent component to which this new component should be added
 *
 */
neon.eventing.OWFIntentsLauncher.addIntentsSelector = function (launcher, intents, databaseNameLookup, tableNameLookup, parentSelector) {

    var selector = neon.eventing.OWFIntentsLauncher.createIntentsSelector_(intents);
    var launchButton = neon.eventing.OWFIntentsLauncher.createLaunchButton_(launcher,selector,intents,databaseNameLookup,tableNameLookup);
    neon.eventing.OWFIntentsLauncher.addSelectListener_(selector, launchButton);

    var parent = $(parentSelector);
    parent.append('<div/>').append(selector);
    parent.append('<div/>').append(launchButton);
};

neon.eventing.OWFIntentsLauncher.createIntentsSelector_ = function(intents) {
    var selector = $('<select/>').attr('name', 'intents').attr('id', 'intents').attr('multiple', '');
    intents.forEach(function (intent, index) {
        selector.append($("<option />").val(index).text(intent.action));
    });
    return selector;
};

neon.eventing.OWFIntentsLauncher.addSelectListener_ = function(selector, button) {
    selector.change(function (evt) {
        if ( selector.val() ) {
            button.removeAttr('disabled');
        }
        else {
            button.attr('disabled','');
        }
    });
};

neon.eventing.OWFIntentsLauncher.prototype.launchIntents_ = function (intents, databaseName, tableName) {
    var me = this;
    if (intents.length > 0) {
        var intent = intents.shift();
        var intentData = this.createIntentData_(intent, databaseName, tableName);
        var action = intent.action;
        OWF.Intents.startActivity(
            {
                action: action,
                dataType: intentData.dataType
            },
            intentData.data,
            function (dest) {
                me.launchIntents_(intents, databaseName, tableName);
            }
        );
    }
};


neon.eventing.OWFIntentsLauncher.createLaunchButton_ = function(launcher, selector, intents, databaseNameLookup, tableNameLookup) {
    var launchButton = $('<input/>').attr('type', 'button').attr('id', 'launch').attr('disabled', '').val('Display Data').click(function () {
        var databaseName = databaseNameLookup.apply(null);
        var tableName = tableNameLookup.apply(null);
        var selectedIntents = selector.val().map(function (intentIndex) {
            return intents[parseInt(intentIndex, 10)];
        });
        launcher.launchIntents_(selectedIntents, databaseName, tableName);
    });
    return launchButton;
};

neon.eventing.OWFIntentsLauncher.prototype.createIntentData_ = function (intent, databaseName, tableName) {
    var data = {};
    data.databaseName = databaseName;
    data.tableName = tableName;
    var dataType = intent.dataTypes[0];
    if (this.metadata_.hasOwnProperty(dataType)) {
        _.extend(data, this.metadata_[dataType]);
    }
    return {"data": data, "dataType": dataType};
};