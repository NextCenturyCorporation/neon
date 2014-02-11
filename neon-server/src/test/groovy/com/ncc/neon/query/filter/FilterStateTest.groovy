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

package com.ncc.neon.query.filter

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import org.junit.Before
import org.junit.Test


class FilterStateTest {

    private static final String UUID_STRING = "84bc5064-c837-483b-8454-c8c72abe45f8"
    private FilterState filterState
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
        assert filters.find{ it.whereClause == singularWhereClause1}
        assert filters.find{ it.whereClause == singularWhereClause2}
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
