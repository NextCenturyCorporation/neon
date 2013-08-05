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
    void "create filter from unique field values"() {
        def queryExecutor = createQueryExecutor()

        def dataStoreName = "dataStoreName"
        def databaseName = "testDatasetId"
        def subfilter = new Filter(dataStoreName: dataStoreName, databaseName: databaseName)
        def field = "field1"
        def operator = "notin"

        // the resulting filter should be "field1 notin [a,c]" since a and c are the unique field values for field1
        // from the queryExecutor
        def filter = FilterFactory.createFieldFilter(queryExecutor, subfilter, field, operator)
        verifyFilter(filter, dataStoreName, databaseName, field, operator)
    }

    private static def verifyFilter(filter, dataStore, databaseName, field, operator) {
        def whereClause = filter.whereClause

        assert whereClause instanceof SingularWhereClause
        assert whereClause.lhs == field
        assert whereClause.operator == operator
        // these values are take from the query executor's result
        assert whereClause.rhs == (["a", "c"] as Set)
        assert filter.dataStoreName == dataStore
        assert filter.databaseName == databaseName
    }

    /**
     * Creates a stub query executor that returns some canned values for field1 and field2
     * @return
     */
    private static def createQueryExecutor() {
        // not the values for field2 (b and d) are not actually used. they are provided to show that only the
        // values from the field1 are used
        def values = [
                createFieldMap("a", "b"),
                createFieldMap("c", "d")
        ]
        def iterator = createRowIterator(values)
        def queryResult = [iterator: { iterator }] as QueryResult
        return [execute: { query, filtered -> queryResult }] as QueryExecutor
    }

    /**
     * Wraps a list's iterator with an iterator that returns Rows
     * @param list
     */
    private static def createRowIterator(list) {
        def delegate = list.iterator()

        // this iterator wraps the values list in a Row
        def iterator = [
                hasNext: { delegate.hasNext() },
                next: {
                    def map = delegate.next()
                    def row = [getFieldValue: { field -> map[field] }] as Row
                    return row
                }
        ] as Iterator
        return iterator
    }

    private static def createFieldMap(field1Val, field2Val) {
        return [field1: field1Val, field2: field2Val]
    }
}
