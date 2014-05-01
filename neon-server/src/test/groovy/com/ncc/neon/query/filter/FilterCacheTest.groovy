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


class FilterCacheTest {

    private FilterCache filterCache
    private DataSet dataSet

    @Before
    void before() {
        filterCache = new FilterCache()
        dataSet = new DataSet(databaseName: "testDB", tableName: "testTable")
    }

    @Test
    void "add a simple filter and see that it can be retrieved"() {
        Filter filter = addEmptyFilter("filterA")
        assertEmptyFilterCanBeRetrieved(filter)
    }

    @Test
    void "add two filters with the same key creates an AND where clause"() {
        SingularWhereClause singularWhereClause1 = new SingularWhereClause(lhs: "column1", operator: "=", rhs: "value1")
        SingularWhereClause singularWhereClause2 = new SingularWhereClause(lhs: "column2", operator: "=", rhs: "value2")

        addBothWhereClausesToFilterCacheUnderTheSameKey(singularWhereClause1, singularWhereClause2)

        List<Filter> filters = filterCache.getFiltersForDataset(dataSet)
        assert filters
        assert filters.size() == 1
        assert filters[0].whereClause instanceof AndWhereClause
        assert filters[0].whereClause.whereClauses == [singularWhereClause1, singularWhereClause2]
    }

    private void addBothWhereClausesToFilterCacheUnderTheSameKey(SingularWhereClause where1, SingularWhereClause where2) {
        Filter filter1 = createFilterFromWhereClause(where1)
        Filter filter2 = createFilterFromWhereClause(where2)
        addFilter("filterA", filter1)
        addFilter("filterA", filter2)
    }

    @Test
    void "add two filters with the different keys creates multiple filters"() {
        SingularWhereClause singularWhereClause1 = new SingularWhereClause(lhs: "column1", operator: "=", rhs: "value1")
        SingularWhereClause singularWhereClause2 = new SingularWhereClause(lhs: "column2", operator: "=", rhs: "value2")

        addBothWhereClausesToFilterCacheWithDifferentKeys(singularWhereClause1, singularWhereClause2)

        List<Filter> filters = filterCache.getFiltersForDataset(dataSet)
        assert filters
        assert filters.size() == 2
        assert filters.find{ it.whereClause == singularWhereClause1}
        assert filters.find{ it.whereClause == singularWhereClause2}
    }

    private void addBothWhereClausesToFilterCacheWithDifferentKeys(SingularWhereClause where1, SingularWhereClause where2) {
        Filter filter1 = createFilterFromWhereClause(where1)
        Filter filter2 = createFilterFromWhereClause(where2)
        addFilter("filterA", filter1)
        addFilter("filterB", filter2)
    }


    @Test
    void "removing a filter works for one filter"() {
        Filter filter = addEmptyFilter("filterA")
        assertEmptyFilterCanBeRetrieved(filter)
        FilterKey removed = filterCache.removeFilter("filterA")
        assert filter.is(removed.filter)
        assert removed.id == "filterA"
        assert filterCache.getFiltersForDataset(dataSet) == []
    }

    @Test
    void "clearing a filter works for one filter"() {
        Filter filter = addEmptyFilter("emptyFilter")
        assertEmptyFilterCanBeRetrieved(filter)
        filterCache.clearAllFilters()
        assert filterCache.getFiltersForDataset(dataSet) == []
    }

    @Test
    void "get filter keys for dataset"() {
        Filter filterA = addEmptyFilter("filterA")
        Filter filterB = addEmptyFilter("filterB")
        List<FilterKey> filterKeys = filterCache.getFilterKeysForDataset(dataSet)

        // no guaranteed order for the filters so sort them first
        Collections.sort(filterKeys, [compare: {key1,key2 -> key1.id <=> key2.id}] as Comparator)

        assert filterKeys[0].id == "filterA"
        assert filterKeys[0].filter == filterA

        assert filterKeys[1].id == "filterB"
        assert filterKeys[1].filter == filterB
    }

    private createFilterFromWhereClause(WhereClause whereClause, DataSet ds = dataSet) {
        return new Filter(databaseName: ds.databaseName, tableName: ds.tableName, whereClause: whereClause)
    }

    private void assertEmptyFilterCanBeRetrieved(Filter filter) {
        List<Filter> filters = filterCache.getFiltersForDataset(dataSet)

        assert filters
        assert filters.size() == 1
        assert filters[0] == filter
    }

    private Filter addEmptyFilter(String id) {
        Filter filter = createFilterFromWhereClause(null)
        addFilter(id, filter)
        return filter
    }

    private void addFilter(String id, Filter filter) {
        FilterKey filterKey = new FilterKey(id:id, filter: filter)
        filterCache.addFilter(filterKey)
    }

}
