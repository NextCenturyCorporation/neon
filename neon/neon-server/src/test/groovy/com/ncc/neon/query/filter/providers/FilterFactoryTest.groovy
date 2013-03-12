package com.ncc.neon.query.filter.providers

import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.Row
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter
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

class FilterFactoryTest {


    @Test
    void "create filter from unique field values query"() {
        def queryExecutor = createQueryExecutor()

        def dataSourceName = "dataSourceName"
        def datasetId = "testDatasetId"
        def subfilter = new Filter(dataSourceName: dataSourceName, datasetId: datasetId)
        def field = "field1"
        def operator = 'notin'
        def filter = FilterFactory.createFieldFilter(queryExecutor, subfilter, field, operator)
        def inClause = filter.whereClause

        assert inClause instanceof SingularWhereClause
        assert inClause.lhs == field
        assert inClause.operator == operator
        // these values are take from the query executor's result
        assert inClause.rhs == (["a","c"] as Set)
        assert filter.dataSourceName == dataSourceName
        assert filter.datasetId == datasetId
    }

    private static def createQueryExecutor() {
        def values = [
                createFieldMap("a", "b"),
                createFieldMap("c", "d")
        ]
        def delegate =  values.iterator()
        // this iterator wraps the results in a Row that has a getFieldValue method
        def iterator = [
            hasNext : {delegate.hasNext()},
            next : {
                def map = delegate.next()
                def row = [ getFieldValue: {field -> map[field] } ] as Row
                return row
            }
        ] as Iterator

        def queryResult = [ iterator : { iterator } ] as QueryResult
        return [ execute : { query, filtered -> queryResult } ] as QueryExecutor
    }

    private static def createFieldMap(field1Val, field2Val) {
        return [field1: field1Val, field2: field2Val]
    }
}
