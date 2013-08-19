package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionState
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.filter.*
import com.ncc.neon.query.transform.ValueStringReplaceTransform
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

    private static final String UUID_STRING = "1af29529-86bb-4f2c-9928-7f4484b9cc49"
    private QueryService queryService
    private FilterKey filterKey
    private DataSet dataSet

    @Before
    void before() {
        queryService = new QueryService()
        dataSet = new DataSet(databaseName: "testDB", tableName: "testTable")
        filterKey = new FilterKey(uuid: UUID.fromString(UUID_STRING),
                dataSet: dataSet)
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
    void "register for filter key"() {
        def queryExecutor = [registerForFilterKey: { ds ->
            assert ds.is(dataSet)
            return filterKey
        }] as QueryExecutor
        setQueryServiceConnection(queryExecutor)

        FilterEvent event = queryService.registerForFilterKey(dataSet)
        assert event.dataSet == dataSet
        assert event.uuid == filterKey.uuid.toString()
    }

    @Test
    void "add filter"() {
        def filter = new Filter(databaseName: dataSet.databaseName, tableName: dataSet.tableName)
        def queryExecutor = [addFilter: { k, f -> assert f.is(filter) }] as QueryExecutor
        setQueryServiceConnection(queryExecutor)
        FilterContainer container = new FilterContainer(filterKey: filterKey, filter: filter)
        queryService.addFilter(container)
    }

    @Test
    void "remove filter"() {
        // arbitrary string (but constant for each test run)
        def queryExecutorMock = new MockFor(QueryExecutor)
        queryExecutorMock.demand.removeFilter { id -> assert id == filterKey }
        def queryExecutor = queryExecutorMock.proxyInstance()
        setQueryServiceConnection(queryExecutor)

        queryService.removeFilter(filterKey)
        queryExecutorMock.verify(queryExecutor)
    }

    @Test
    void "get selection WHERE"() {
        def inputJson = /[ { "key1" : "val1" }, { "key2": "val2" }]/

        def filter = [] as Filter
        def queryExecutorMock = createQueryExecutorMockForGetSelection(filter, inputJson)
        def queryExecutor = queryExecutorMock.proxyInstance()
        setQueryServiceConnection(queryExecutor)

        def result = queryService.getSelectionWhere(filter, null, null)
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
        setQueryServiceConnection(queryExecutor)

        def result = queryService.getSelectionWhere(filter, transform, null)
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
        setQueryServiceConnection(queryExecutor)

        def result = queryService.getSelectionWhere(filter, transform, ["val1", "25"])
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
        setQueryServiceConnection(executor)
        def query = [toString: { "mock query" }] as Query
        def outputJson = queryService.executeQuery(query, false, transform, transformParams)
        def jsonObject = new JSONObject(outputJson)
        return jsonObject.getJSONArray("data")
    }

    private void setQueryServiceConnection(QueryExecutor executor) {
        ConnectionState connectionState = new ConnectionState()
        connectionState.queryExecutor = executor
        queryService.connectionState = connectionState
    }

    private static assertKeyValue(array, index, key, value) {
        def jsonObject = array.getJSONObject(index)
        assert jsonObject.get(key) == value
    }
}
