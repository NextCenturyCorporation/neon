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

package com.ncc.neon.query.convert

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import org.junit.Before
import org.junit.Test

/**
 * Sets up unit tests to be run against both spark SQL and mongo conversion strategies
 */
abstract class AbstractConversionTest {

    protected static final String DATABASE_NAME = "database"
    protected static final String TABLE_NAME = "table"
    protected static final String FIELD_NAME = "field"
    protected static final String FIELD_NAME_2 = "field2"
    protected static final String FIELD_VALUE = "value"
    protected static final int LIMIT_AMOUNT = 5
    protected static final int SKIP_AMOUNT = 2

    protected FilterState filterState
    protected SelectionState selectionState
    private Filter simpleFilter
    protected Query simpleQuery

    @Before
    void before() {
        simpleFilter = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME)
        simpleQuery = new Query(filter: simpleFilter)
        filterState = new FilterState()
        selectionState = new SelectionState()
    }

    @Test(expected = NullPointerException)
    void "test converting a query requires a filter"() {
        Query query = new Query()
        convertQuery(query)
    }

    @Test
    void "test converting a query with just a dataset populated"() {
        def query = convertQuery(simpleQuery)
        assertSimplestConvertQuery(query)
    }

    @Test
    void "test converting a query with a filter in the FilterState"() {
        givenFilterStateHasOneFilter()
        def query = convertQuery(simpleQuery)
        assertQueryWithWhereClause(query)
    }

    @Test
    void "test converting a query with a filter in the FilterState but ignore filters"() {
        givenFilterStateHasOneFilter()
        def query = convertQuery(simpleQuery,new QueryOptions(ignoreFilters: true, selectionOnly: false))
        assertSimplestConvertQuery(query)
    }

    @Test
    void "test converting a query with a selection"() {
        givenSelectionStateHasOneFilter()
        def query = convertQuery(simpleQuery,new QueryOptions(ignoreFilters: false, selectionOnly: true))
        assertQueryWithWhereClause(query)
    }

    @Test
    void "test converting a compound query with a selection"() {
        givenSelectionStateHasOneFilter()
        givenQueryHasOrWhereClause()
        def query = convertQuery(simpleQuery,new QueryOptions(ignoreFilters: false, selectionOnly: true))
        assertQueryWithOrWhereClauseAndFilter(query)
    }

    @Test
    void "test selection not used"() {
        givenSelectionStateHasOneFilter()
        givenQueryHasSimpleWhereClause()
        def query = convertQuery(simpleQuery,new QueryOptions(ignoreFilters: false, selectionOnly: false))
        assertQueryWithWhereClause(query)
    }

    @Test
    void "test converting a compound query with a filter in the FilterState"() {
        givenFilterStateHasOneFilter()
        givenQueryHasOrWhereClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithOrWhereClauseAndFilter(query)
    }

    @Test
    void "test select clause populated"() {
        givenQueryHasFields()
        def query = convertQuery(simpleQuery)
        assertSelectClausePopulated(query)
    }

    @Test
    void "test sort clause populated"() {
        givenQueryHasSortClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithSortClause(query)
    }

    @Test
    void "test limit clause populated"() {
        givenQueryHasLimitClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithLimitClause(query)
    }

    @Test
    void "test offset clause populated"() {
        givenQueryHasSkipClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithOffsetClause(query)
    }


    @Test
    void "test distinct clause populated"() {
        givenQueryHasDistinctClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithDistinctClause(query)
    }

    @Test
    void "test aggregate clause populated"() {
        givenQueryHasAggregateClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithAggregateClause(query)
    }

    @Test
    void "test group by clause populated"() {
        givenQueryHasGroupByPopulated()
        def query = convertQuery(simpleQuery)
        assertQueryWithGroupByClauses(query)
    }

    @Test
    void "test a filter with no where clause"() {
        givenFilterStateHasAnEmptyFilter()
        def query = convertQuery(simpleQuery)
        assertQueryWithEmptyFilter(query)
    }

    @Test
    void "test query where null"() {
        givenQueryHasWhereNullClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithWhereNullClause(query)
    }

    @Test
    void "test query where not null"() {
        givenQueryHasWhereNotNullClause()
        def query = convertQuery(simpleQuery)
        assertQueryWithWhereNotNullClause(query)
    }

    @Test
    void "test ignoring specific filter"() {
        givenFilterStateHasOneFilter()
        def query = convertQuery(simpleQuery, new QueryOptions(ignoredFilterIds: ["filterA"] as HashSet))
        assertSimplestConvertQuery(query)

    }

    private def convertQuery(query, queryOptions = QueryOptions.DEFAULT_OPTIONS) {
        return doConvertQuery(query, queryOptions)
    }

    protected abstract def doConvertQuery(query, queryOptions)

    protected abstract void assertSelectClausePopulated(query)

    protected abstract void assertSimplestConvertQuery(query)

    protected abstract void assertQueryWithWhereClause(query)

    protected abstract void assertQueryWithSortClause(query)

    protected abstract void assertQueryWithLimitClause(query)

    protected abstract void assertQueryWithOffsetClause(query)

    protected abstract void assertQueryWithDistinctClause(query)

    protected abstract void assertQueryWithAggregateClause(query)

    protected abstract void assertQueryWithGroupByClauses(query)

    protected abstract void assertQueryWithOrWhereClauseAndFilter(query)

    protected abstract void assertQueryWithWhereNullClause(query)

    protected abstract void assertQueryWithWhereNotNullClause(query)

    protected abstract void assertQueryWithEmptyFilter(query)

    private void givenFilterStateHasAnEmptyFilter() {
        Filter filter = new Filter(databaseName: simpleFilter.databaseName, tableName: simpleFilter.tableName)
        FilterKey filterKey = new FilterKey(id: "emptyFilter", filter: filter)
        filterState.addFilter(filterKey)
    }

    private void givenFilterStateHasOneFilter() {
        SingularWhereClause whereClause = new SingularWhereClause(lhs: FIELD_NAME, operator: "=", rhs: FIELD_VALUE)
        Filter filterWithWhere = new Filter(databaseName: simpleFilter.databaseName, tableName: simpleFilter.tableName, whereClause: whereClause)
        FilterKey filterKey = new FilterKey(id: "filterA", filter: filterWithWhere)
        filterState.addFilter(filterKey)
    }

    private void givenSelectionStateHasOneFilter() {
        SingularWhereClause whereClause = new SingularWhereClause(lhs: FIELD_NAME, operator: "=", rhs: FIELD_VALUE)
        Filter filterWithWhere = new Filter(databaseName: simpleFilter.databaseName, tableName: simpleFilter.tableName, whereClause: whereClause)
        FilterKey filterKey = new FilterKey(id: "selectionA", filter: filterWithWhere)
        selectionState.addFilter(filterKey)
    }

    private void givenQueryHasFields() {
        simpleQuery.fields = [FIELD_NAME, FIELD_NAME_2]
    }

    private void givenQueryHasOrWhereClause() {
        SingularWhereClause clause1 = new SingularWhereClause(lhs: FIELD_NAME, operator: "=", rhs: FIELD_VALUE)
        SingularWhereClause clause2 = new SingularWhereClause(lhs: FIELD_NAME_2, operator: "=", rhs: FIELD_VALUE)
        OrWhereClause orWhereClause = new OrWhereClause(whereClauses: [clause1, clause2])

        simpleQuery.filter.whereClause = orWhereClause
    }

    private void givenQueryHasWhereNullClause() {
        simpleQuery.filter.whereClause = new SingularWhereClause(lhs: FIELD_NAME, operator: "=", rhs: null)
    }

    private void givenQueryHasWhereNotNullClause() {
        simpleQuery.filter.whereClause = new SingularWhereClause(lhs: FIELD_NAME, operator: "!=", rhs: null)
    }

    private void givenQueryHasSimpleWhereClause() {
        simpleQuery.filter.whereClause = new SingularWhereClause(lhs: FIELD_NAME, operator: '=' , rhs: FIELD_VALUE)
    }

    private void givenQueryHasSortClause() {
        simpleQuery.sortClauses = [new SortClause(fieldName: FIELD_NAME, sortOrder: SortOrder.ASCENDING)]
    }

    private void givenQueryHasLimitClause() {
        simpleQuery.limitClause = new LimitClause(limit: LIMIT_AMOUNT)
    }

    private void givenQueryHasSkipClause() {
        simpleQuery.offsetClause = new OffsetClause(offset: SKIP_AMOUNT)
    }

    private void givenQueryHasDistinctClause() {
        simpleQuery.isDistinct = true
    }

    private void givenQueryHasAggregateClause() {
        simpleQuery.aggregates = [new AggregateClause(name: "${FIELD_NAME}_sum", operation: "sum", field: FIELD_NAME)]
    }

    private void givenQueryHasGroupByPopulated() {
        simpleQuery.groupByClauses = [new GroupByFieldClause(field: "${FIELD_NAME_2}"),
                new GroupByFunctionClause(name: "${FIELD_NAME}_sum", operation: "sum", field: FIELD_NAME)]
    }

}
