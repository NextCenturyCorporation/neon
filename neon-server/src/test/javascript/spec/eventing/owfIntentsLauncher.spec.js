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
describe('intents launcher', function () {

    // ensure this exists since we'll be mocking it out
    neon.namespace("OWF.Intents");
    var origStartActivity = OWF.Intents.startActivity;

    var launcher;

    beforeEach(function () {
        launcher = new neon.eventing.OWFIntentsLauncher();
    });

    afterEach(function () {
        OWF.Intents.startActivity = origStartActivity;
    });

    it('should allow metadata to be added for a data type', function () {
        var dataType1 = 'dataType1';
        var metadata1 = {key1: 'val1', key2: 'val2'};
        var metadata2 = {key3: 'val3'};
        launcher.addMetadataForDataType(dataType1, metadata1);
        launcher.addMetadataForDataType(dataType1, metadata2);
        expect(launcher.metadata_[dataType1]).toEqual({key1: 'val1', key2: 'val2', key3: 'val3'})

    });


    it('should create intent data', function () {
        var dataSourceName = 'testDataSource';
        var datasetId = 'testDatasetId';
        var dataType = 'dataType1';
        var metadata = {key1: 'val1', key2: 'val2'};
        launcher.addMetadataForDataType(dataType, metadata);
        var intent = {dataTypes: [dataType]};
        var intentData = launcher.createIntentData_(intent, dataSourceName, datasetId);
        var data = intentData.data;

        expect(data).toEqual({dataSourceName: dataSourceName, datasetId: datasetId, key1: 'val1', key2: 'val2'});
        expect(intentData.dataType).toEqual(dataType);

    });

    it('should launch multiple intents', function () {
        var launched = [];
        var dataType1 = 'dataType1';
        var dataType2 = 'dataType2';
        var metadata1 = {key1: 'val1', key2: 'val2'};
        var metadata2 = {key3: 'val3'};
        var action1 = 'action1';
        var action2 = 'action2';
        launcher.addMetadataForDataType(dataType1, metadata1);
        launcher.addMetadataForDataType(dataType2, metadata2);
        var intent1 = {action: action1, dataTypes: [dataType1]};
        var intent2 = {action: action2, dataTypes: [dataType2]};
        var intents = [intent1, intent2];

        OWF.Intents.startActivity = function (intent, data, callback) {
            launched.push({intent: {action: intent.action, dataType: intent.dataType}, data: data});
            callback('destWidget');
        };

        var dataSourceName = 'testDataSource';
        var datasetId = 'testDatasetId';
        launcher.launchIntents_(intents, dataSourceName, datasetId);
        var expectedData1 = {dataSourceName: dataSourceName, datasetId: datasetId, key1: 'val1', key2: 'val2'};
        var expectedData2 = {dataSourceName: dataSourceName, datasetId: datasetId, key3: 'val3'};

        var expectedIntent1Data = {intent: {action: action1, dataType: dataType1}, data: expectedData1};
        var expectedIntent2Data = {intent: {action: action2, dataType: dataType2}, data: expectedData2};

        expect(launched).toBeEqualArray([expectedIntent1Data, expectedIntent2Data]);

    });

});