package com.ncc.neon.services

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.transform.ValueStringReplaceTransform
import com.ncc.neon.result.MetadataResolver
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

    private QueryService queryService

    @Before
    void before() {
        queryService = new QueryService()
        queryService.metadataResolver = [resolveQuery: { new ColumnMetadataList([])}] as MetadataResolver
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
        queryService.queryExecutorFactory = [create: { executor }] as QueryExecutorFactory
    }

    private static assertKeyValue(array, index, key, value) {
        def jsonObject = array.getJSONObject(index)
        assert jsonObject.get(key) == value
    }
}
