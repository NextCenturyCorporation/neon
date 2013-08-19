package com.ncc.neon.query.filter

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
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
class FilterStateTest {

    private static final String UUID_STRING = "84bc5064-c837-483b-8454-c8c72abe45f8"
    FilterState filterState
    private FilterKey filterKey
    private DataSet dataSet

    @Before
    void before() {
        filterState = new FilterState()
        dataSet = new DataSet(databaseName: "testDB", tableName: "testTable")
        filterKey = new FilterKey(uuid: UUID.fromString(UUID_STRING), dataSet: dataSet)
    }

    @Test
    void "add a simple filter and see that it can be retrieved"() {
        Filter filter = addEmptyFilter()
        assertEmptyFilterCanBeRetrieved(filter)
    }

    @Test
    void "add two filters with the same key creates an AND where clause"() {
        SingularWhereClause singularWhereClause1 = new SingularWhereClause(lhs: "column1", operator: "=", rhs: "value1")
        SingularWhereClause singularWhereClause2 = new SingularWhereClause(lhs: "column2", operator: "=", rhs: "value2")

        addBothWhereClausesToFilterStateUnderTheSameKey(singularWhereClause1, singularWhereClause2)

        List<Filter> filters = filterState.getFiltersForDataset(dataSet)
        assert filters
        assert filters.size() == 1
        assert filters[0].whereClause instanceof AndWhereClause
        assert filters[0].whereClause.whereClauses == [singularWhereClause1, singularWhereClause2]
    }

    private void addBothWhereClausesToFilterStateUnderTheSameKey(SingularWhereClause where1, SingularWhereClause where2) {
        Filter filter1 = createFilterFromWhereClause(where1)
        Filter filter2 = createFilterFromWhereClause(where2)
        filterState.addFilter(filterKey, filter1)
        filterState.addFilter(filterKey, filter2)
    }

    @Test
    void "add two filters with the different keys creates multiple filters"() {
        SingularWhereClause singularWhereClause1 = new SingularWhereClause(lhs: "column1", operator: "=", rhs: "value1")
        SingularWhereClause singularWhereClause2 = new SingularWhereClause(lhs: "column2", operator: "=", rhs: "value2")

        addBothWhereClausesToFilterStateWithDifferentKeys(singularWhereClause1, singularWhereClause2)

        List<Filter> filters = filterState.getFiltersForDataset(dataSet)
        assert filters
        assert filters.size() == 2
        assert filters[0].whereClause == singularWhereClause1
        assert filters[1].whereClause == singularWhereClause2
    }

    private void addBothWhereClausesToFilterStateWithDifferentKeys(SingularWhereClause where1, SingularWhereClause where2) {
        Filter filter1 = createFilterFromWhereClause(where1)
        Filter filter2 = createFilterFromWhereClause(where2)
        filterState.addFilter(new FilterKey(uuid: UUID.randomUUID(), dataSet: dataSet), filter1)
        filterState.addFilter(new FilterKey(uuid: UUID.randomUUID(), dataSet: dataSet), filter2)
    }


    @Test
    void "removing a filter works for one filter"() {
        Filter filter = addEmptyFilter()
        assertEmptyFilterCanBeRetrieved(filter)
        filterState.removeFilter(filterKey)
        assert filterState.getFiltersForDataset() == []
    }

    @Test
    void "clearing a filter works for one filter"() {
        Filter filter = addEmptyFilter()
        assertEmptyFilterCanBeRetrieved(filter)
        filterState.clearAllFilters()
        assert filterState.getFiltersForDataset() == []
    }

    private createFilterFromWhereClause(WhereClause whereClause, DataSet ds = dataSet) {
        return new Filter(databaseName: ds.databaseName, tableName: ds.tableName, whereClause: whereClause)
    }

    private void assertEmptyFilterCanBeRetrieved(Filter filter) {
        List<Filter> filters = filterState.getFiltersForDataset(dataSet)

        assert filters
        assert filters.size() == 1
        assert filters[0] == filter
    }

    private Filter addEmptyFilter() {
        Filter filter = createFilterFromWhereClause(null)
        filterState.addFilter(filterKey, filter)
        return filter
    }

}
