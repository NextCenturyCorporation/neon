package com.ncc.neon.services

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.column.DefaultColumnMetadata
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.TableQueryResult
import com.ncc.neon.result.ClientData
import com.ncc.neon.result.MetadataResolver
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
        QueryExecutor executor = [execute: { query, options -> new TableQueryResult([["key1": "val1"], ["key2": 2]]) }] as QueryExecutor
        queryService.queryExecutorFactory = [getExecutor: { executor }] as QueryExecutorFactory

        def metadata = [new DefaultColumnMetadata(columnName: "key1", text: true), new DefaultColumnMetadata(columnName: "key2", numeric: true)]
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
