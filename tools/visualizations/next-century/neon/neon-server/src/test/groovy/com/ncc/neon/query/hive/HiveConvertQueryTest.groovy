package com.ncc.neon.query.hive

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import org.junit.After
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
 *
 * 
 * @author tbrooks
 */

class HiveConvertQueryTest {

    private FilterState filterState
    private Filter simpleFilter
    private Query simpleQuery

    @Before
    void setup() {
        simpleFilter = new Filter(databaseName: "database", tableName: "test")
        simpleQuery = new Query(filter: simpleFilter)
        filterState = new FilterState()
    }

    @After
    void teardown(){
        simpleFilter = new Filter(databaseName: "database", tableName: "test")
        simpleQuery = new Query(filter: simpleFilter)
        filterState = new FilterState()
    }

    @Test(expected = NullPointerException)
    void "a query to be converted must have a filter"() {
        Query query = new Query()
        whenExecutingConvertQuery(query)
    }

    @Test
    void testSimplestConvertQuery() {
        String hiveQuery = whenExecutingConvertQuery(simpleQuery)
        assert hiveQuery == "select * from database.test"
    }

    @Test
    void "test covertQuery does not care about the FilterState"() {
        givenFilterStateHasOneFilter()
        String hiveQuery = whenExecutingConvertQuery(simpleQuery)
        assert hiveQuery == "select * from database.test"
    }

    private void givenFilterStateHasOneFilter() {
        SingularWhereClause whereClause = new SingularWhereClause(lhs: "column", operator: "=", rhs: "test")
        Filter filterWithWhere = new Filter(databaseName: simpleFilter.databaseName, tableName: simpleFilter.tableName, whereClause: whereClause)
        filterState.addFilter(filterWithWhere)
    }

    private String whenExecutingConvertQuery(Query query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState)
        conversionStrategy.convertQuery(query)
    }

}
