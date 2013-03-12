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

// some of this class is tested in the integration query.spec.js. This covers the unit tests.
describe('query', function() {

    // test the filter wrapping private methods since they are only accessed through methods in the
    // integration test (much of which is mocked)

    it('should wrap a filter in a filter provider', function() {
        var filter = new neon.query.Filter();
        var wrapped = neon.query.wrapFilterInProvider_(filter);
        expect(wrapped).toBeInstanceOf(neon.query.FilterProvider);
        expect(wrapped.filter).toBe(filter);
    });

    it('should pass the filter provider through directly', function() {
        var providerClass = function() {};
        providerClass.prototype = new neon.query.FilterProvider();
        var provider = new providerClass();
        var wrapped = neon.query.wrapFilterInProvider_(provider);
        expect(wrapped).toBe(provider);
    });

    it('should wrap a string group by clause in a single field clause', function() {
        var fieldName = 'test_field';
        var query = new neon.query.Query();
        query.groupBy(fieldName);
        expect(query.groupByClauses.length).toEqual(1);
        var wrapped = query.groupByClauses[0];
        verifySingleFieldClause_(fieldName, wrapped);
    });

    it('should pass a field function clause through directly', function() {
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(1);
        verifyGroupByFunctionClause_(groupByFunctionClause, query.groupByClauses[0]);
    });

    it('should allow multiple group by clauses', function() {
        var fieldName = 'test_field';
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(fieldName, groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(2);
        verifySingleFieldClause_(fieldName, query.groupByClauses[0]);
        verifyGroupByFunctionClause_(groupByFunctionClause, query.groupByClauses[1]);
    });

    function verifySingleFieldClause_(fieldName, actual) {
        expect(actual).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(actual.field).toBe(fieldName);
    }

    function verifyGroupByFunctionClause_(expected, actual) {
        expect(actual).toBe(expected);
    }

});