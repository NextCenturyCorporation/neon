package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.DataSet
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

class MongoConvertQueryWithFiltersTest {

    private Filter simpleFilter
    private Query simpleQuery
    private FilterState filterState

    @Before
    void setup() {
        simpleFilter = new Filter(dataStoreName: DataSet.MONGO, databaseName: "test")
        simpleQuery = new Query(filter: simpleFilter)
        filterState = new FilterState()
    }

    @After
    void teardown(){
        simpleFilter = new Filter(dataStoreName: DataSet.MONGO, databaseName: "test")
        simpleQuery = new Query(filter: simpleFilter)
        filterState = new FilterState()
    }

    @Test(expected = NullPointerException)
    void "a query to be converted must have a filter"() {
        Query query = new Query()
        whenExecutingConvertQueryWithFilters(query)
    }

    @Test
    void testSimplestConvertQueryWithFilters() {
        MongoQuery mongoQuery = whenExecutingConvertQueryWithFilters(simpleQuery)

        assert mongoQuery.query == simpleQuery
        assert mongoQuery.whereClauseParams == new BasicDBObject()
        assert mongoQuery.selectParams == null
    }

    @Test
    void "test covertQueryWithFilters respects the FilterState"() {
        givenFilterStateHasOneFilter()
        MongoQuery mongoQuery = whenExecutingConvertQueryWithFilters(simpleQuery)

        assert mongoQuery.query == simpleQuery
        assert mongoQuery.whereClauseParams == new BasicDBObject("column", "test")
        assert mongoQuery.selectParams == null
    }

    private void givenFilterStateHasOneFilter() {
        SingularWhereClause whereClause = new SingularWhereClause(lhs: "column", operator: "=", rhs: "test")
        Filter filterWithWhere = new Filter(dataStoreName: simpleFilter.dataStoreName, databaseName: simpleFilter.databaseName, whereClause: whereClause)
        filterState.addFilter(filterWithWhere)
    }


    private MongoQuery whenExecutingConvertQueryWithFilters(Query query) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState)
        conversionStrategy.convertQueryWithFilters(query)
    }
}
