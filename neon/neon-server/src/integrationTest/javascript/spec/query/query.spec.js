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
 * This is a basic smoke test to make sure the javascript objects are properly mapped
 * to the server side groovy objects when passed via the web services
 */
describe('query mapping', function () {
    var dataSourceName = 'test-dataSource';
    var datasetId = 'test-dataset';

    it('should get fields', function () {
        var callback = jasmine.createSpy('fields');
        neon.query.getFieldNames(dataSourceName, datasetId, callback);
        waitsFor(function () {
            return callback.wasInvoked();
        });
        runs(function () {
            expect(callback).toHaveBeenCalled();
        });
    });

    it('should execute a query', function () {
        var callback = jasmine.createSpy('query');
        // verify that the different clauses are properly mapped to the server side. if there
        // is an error in the mapping, the callback will not be invoked
        var query = new neon.query.Query()
            .selectFrom(dataSourceName, datasetId)
            .where('test_field', '=', 'test_value')
            .groupBy('DayOfWeek', new neon.query.GroupByFunctionClause(neon.query.MONTH, 'test_date', 'month'))
            .aggregate(neon.query.COUNT, 'some_field', 'count');
        neon.query.executeQuery(query, callback);
        waitsFor(function () {
            return callback.wasInvoked();
        });
        runs(function () {
            expect(callback).toHaveBeenCalled();
        });
    });

    it('should execute invoke the error callback on failure', function () {
        var successCallback = jasmine.createSpy('successCallback');
        var errorCallback = jasmine.createSpy('successCallback');
        // by passing in a non query object, this should cause a mapping error and trigger the error callback (though we
        // still need a few of the valid query parameters for the executeQuery method to properly work)
        neon.query.executeQuery({ filter: {dataSourceName: dataSourceName, datasetId: datasetId}, aField: "aValue"}, successCallback, errorCallback);
        waitsFor(function () {
            return errorCallback.wasInvoked();
        });
        runs(function () {
            expect(errorCallback).toHaveBeenCalled();
            expect(successCallback).not.toHaveBeenCalled();
        });
    });


    it('should add a filter', function () {
        var callback = jasmine.createSpy('filter');
        var filter = new neon.query.Filter().selectFrom(dataSourceName, datasetId).where('someAttr', '=', 'someValue');
        neon.query.addFilter(filter, callback);
        waitsFor(function () {
            return callback.wasInvoked();
        });
        runs(function () {
            expect(callback).toHaveBeenCalled();
        });
    });
});