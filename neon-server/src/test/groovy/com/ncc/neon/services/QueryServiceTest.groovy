/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.services

import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.*
import org.junit.Before
import org.junit.Test

class QueryServiceTest {

    private static final String HOST = "aHost"
    private static final String DATABASE_TYPE = DataSources.mongo.toString()

    private QueryService queryService

    @Before
    void before() {
        queryService = new QueryService()
        QueryExecutor executor = [execute: { query, options -> new TabularQueryResult([["key1": "val1"], ["key2": 2]]) }] as QueryExecutor
        queryService.queryExecutorFactory = [getExecutor: { connection ->
            assert connection.host == HOST
            assert connection.dataSource == DataSources.valueOf(DATABASE_TYPE)
            executor }] as QueryExecutorFactory
    }

    @Test
    void "execute query"() {
        QueryResult result = queryService.executeQuery(HOST, DATABASE_TYPE, new Query())
        assert result.data == [["key1": "val1"], ["key2": 2]]
    }

    @Test
    void "execute query group"() {
        QueryGroup queryGroup = new QueryGroup(queries: [new Query()])
        QueryResult result = queryService.executeQueryGroup(HOST, DATABASE_TYPE, queryGroup)
        assert result.data == [["key1": "val1"], ["key2": 2]]
    }
}
