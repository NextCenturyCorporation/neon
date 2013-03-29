package com.ncc.neon.services

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.providers.FilterProvider
import com.ncc.neon.query.transform.ValueStringReplaceTransform
import com.ncc.neon.util.AssertUtils
import groovy.mock.interceptor.MockFor
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

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
class QueryServiceTest {

    private def queryService

    @Before
    void before() {
        queryService = new QueryService()
    }

    @Test
    void "execute query"() {
        def inputJson = /[ { "key1" : "val1" }, { "key2": "val2" }]/
        def array = executeQuery(inputJson)
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", "val1")
        assertKeyValue(array, 1, "key2", "val2")
    }

    @Test
    void "execute query with no-arg transform"() {
        def inputJson = /[{"key1":"val1"},{"replaceMyValue":"abc"}]/
        def array = executeQuery(inputJson, ValueStringReplaceTransform.name)
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", "val1")
        // 10 is the default value
        assertKeyValue(array, 1, "replaceMyValue", 10)
    }

    @Test
    void "execute query with transform with args"() {
        def inputJson = /[{"key1":"val1"},{"notReplaced":"abc"}]/
        def array = executeQuery(inputJson, ValueStringReplaceTransform.name, ["val1", "25"])
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", 25)
        assertKeyValue(array, 1, "notReplaced", "abc")
    }


    @Test
    void "add filter"() {
        // arbitrary string (but constant for each test run)
        def filterId = UUID.fromString("1af29529-86bb-4f2c-9928-7f4484b9cc49")
        def filter = [] as Filter
        def filterProvider = [provideFilter: { filter }] as FilterProvider
        def queryExecutor = [addFilter: { f -> assert f.is(filter); filterId }] as QueryExecutor
        queryService.queryExecutor = queryExecutor

        def filterEvent = queryService.addFilter(filterProvider)
        AssertUtils.assertEqualCollections([filterId.toString()], filterEvent.addedIds)
        assert filterEvent.removedIds.isEmpty()
    }

    @Test
    void "remove filter"() {
        // arbitrary string (but constant for each test run)
        def filterId = UUID.fromString("1af29529-86bb-4f2c-9928-7f4484b9cc49")
        def queryExecutorMock = new MockFor(QueryExecutor)
        queryExecutorMock.demand.removeFilter { id -> assert id == filterId }
        def queryExecutor = queryExecutorMock.proxyInstance()
        queryService.queryExecutor = queryExecutor

        def event = queryService.removeFilter(filterId.toString())
        queryExecutorMock.verify(queryExecutor)

        assert event.addedIds.isEmpty()
        AssertUtils.assertEqualCollections([filterId.toString()], event.removedIds)
    }

    @Test
    void "replace filter"() {
        // arbitrary strings (but constant for each test run)
        def addId = UUID.fromString("1af29529-86bb-4f2c-9928-7f4484b9cc48")
        def removeId = UUID.fromString("1af29529-86bb-4f2c-9928-7f4484b9cc49")
        def filter = [] as Filter
        def filterProvider = [provideFilter: { filter }] as FilterProvider

        def queryExecutorMock = new MockFor(QueryExecutor)
        queryExecutorMock.demand.removeFilter { id -> assert id == removeId }
        queryExecutorMock.demand.addFilter { f -> assert f.is(filter); addId }

        def queryExecutor = queryExecutorMock.proxyInstance()
        queryService.queryExecutor = queryExecutor

        def event = queryService.replaceFilter(removeId.toString(), filterProvider)
        queryExecutorMock.verify(queryExecutor)
        AssertUtils.assertEqualCollections([addId.toString()], event.addedIds)
        AssertUtils.assertEqualCollections([removeId.toString()], event.removedIds)
    }


    @Test
    void "get selection WHERE"() {
        def inputJson = /[ { "key1" : "val1" }, { "key2": "val2" }]/

        def filter = [] as Filter
        def queryExecutorMock = createQueryExecutorMockForGetSelection(filter, inputJson)
        def queryExecutor = queryExecutorMock.proxyInstance()
        queryService.queryExecutor = queryExecutor

        def result = queryService.getSelectionWhere(filter, null,  null)
        queryExecutorMock.verify(queryExecutor)

        // in this case the input and output is the same because there is no transform
        def array = new JSONObject(result).getJSONArray("data")
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", "val1")
        assertKeyValue(array, 1, "key2", "val2")

    }

    @Test
    void "get selection WHERE with no-arg transform"() {
        def inputJson = /[{"key1":"val1"},{"replaceMyValue":"abc"}]/
        def filter = [] as Filter
        def transform = ValueStringReplaceTransform.name
        def queryExecutorMock = createQueryExecutorMockForGetSelection(filter, inputJson)
        def queryExecutor = queryExecutorMock.proxyInstance()
        queryService.queryExecutor = queryExecutor

        def result = queryService.getSelectionWhere(filter, transform,  null)
        queryExecutorMock.verify(queryExecutor)

        def array = new JSONObject(result).getJSONArray("data")
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", "val1")
        // 10 is the default value
        assertKeyValue(array, 1, "replaceMyValue", 10)
    }


    @Test
    void "get selection WHERE with transform with args"() {
        def inputJson = /[{"key1":"val1"},{"notReplaced":"abc"}]/
        def filter = [] as Filter
        def transform = ValueStringReplaceTransform.name
        def queryExecutorMock = createQueryExecutorMockForGetSelection(filter, inputJson)
        def queryExecutor = queryExecutorMock.proxyInstance()
        queryService.queryExecutor = queryExecutor

        def result = queryService.getSelectionWhere(filter, transform, ["val1","25"])
        queryExecutorMock.verify(queryExecutor)

        def array = new JSONObject(result).getJSONArray("data")
        assert array.length() == 2
        assertKeyValue(array, 0, "key1", 25)
        assertKeyValue(array, 1, "notReplaced", "abc")
    }

    /**
     * Creates a mock query executor that simulates a getSelectionWhere and returns this json
     * @param filter
     * @param inputJson
     * @return
     */
    private def createQueryExecutorMockForGetSelection(filter, inputJson) {
        def queryExecutorMock = new MockFor(QueryExecutor)
        def result = [toJson: { inputJson }] as QueryResult
        queryExecutorMock.demand.getSelectionWhere { f -> assert f.is(filter); result }
        return queryExecutorMock
    }

    private def executeQuery(inputJson, transform = null, transformParams = null) {
        def queryResult = [toJson: { inputJson }] as QueryResult
        def executor = [execute: { query, includeFiltered -> queryResult }] as QueryExecutor
        queryService.queryExecutor = executor
        def query = [toString: { "mock query" }] as Query
        def outputJson = queryService.executeQuery(query, false, transform, transformParams)
        def jsonObject = new JSONObject(outputJson)
        return jsonObject.getJSONArray("data")
    }


    private static assertKeyValue(array, index, key, value) {
        def jsonObject = array.getJSONObject(index)
        assert jsonObject.get(key) == value
    }
}
