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

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.column.ColumnMetadata
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.TableQueryResult
import com.ncc.neon.result.ClientData
import com.ncc.neon.result.MetadataResolver
import org.junit.Before
import org.junit.Test



class QueryServiceTest {

    private QueryService queryService

    @Before
    void before() {
        queryService = new QueryService()
        QueryExecutor executor = [execute: { query, options -> new TableQueryResult([["key1": "val1"], ["key2": 2]]) }] as QueryExecutor
        queryService.queryExecutorFactory = [getExecutor: { executor }] as QueryExecutorFactory

        def metadata = [new ColumnMetadata(columnName: "key1", text: true), new ColumnMetadata(columnName: "key2", numeric: true)]
        queryService.metadataResolver = [resolveQuery: { new ColumnMetadataList(metadata) }] as MetadataResolver
    }

    @Test
    void "execute query"() {
        ClientData clientData = queryService.executeQuery("", new Query())
        assert clientData.data
        assert clientData.data == [["key1": "val1"], ["key2": 2]]

        assert clientData.metadata
        assert clientData.metadata == ["key1": ["text": true], "key2": ["numeric": true]]
    }

    @Test
    void "execute query group"() {
        QueryGroup queryGroup = new QueryGroup(queries: [new Query()])
        ClientData clientData = queryService.executeQueryGroup("", queryGroup)
        assert clientData.data
        assert clientData.data == [["key1": "val1"], ["key2": 2]]

        assert clientData.metadata
        assert clientData.metadata == ["key1": ["text": true], "key2": ["numeric": true]]
    }
}
