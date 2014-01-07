package com.ncc.neon

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.util.AssertUtils
import com.ncc.neon.util.DateUtils
import org.json.JSONArray
import org.junit.After
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

/**
 * Integration test that verifies the neon server properly translates database specific queries.
 * These tests parallel the acceptance tests in the javascript client query acceptance tests
 *
 * Subclasses implement database specific translations
 */

abstract class AbstractQueryExecutorIntegrationTest {

    static final DATE_FIELD_REGEX = ~/.*date.*/

    static final String DATABASE_NAME = 'neonintegrationtest'

    static final String TABLE_NAME = 'records'

    /** the name of the file that contains all of the data used for the test */
    static final ALL_DATA_FILENAME = 'data.json'

    /** all of the data in the test file. lazy initialized because subclasses modify the input directory. Use the getter to access it */
    private def allData

    /** a filter that just includes all of the data (no WHERE clause) */
    static final ALL_DATA_FILTER = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME)

    /** a simple query that returns all of the data */
    static final ALL_DATA_QUERY = new Query(filter: ALL_DATA_FILTER)

    protected abstract def getQueryExecutor()

    /**
     * Gets the folder that the input json files are stored in. By defalut, the test-data
     * directory is used, but some databases (e.g. hive) will transform the data (such as by
     * removing nested fields) and put it i na folder it can use
     * @return
     */
    protected String getResultsJsonFolder() {
        return ""
    }

    /**
     * Converts a database specific type to a basic java type to use for testing
     * @param val
     * @return
     * TODO: NEON-550 this may not be needed once non database specific types are returned
     */
    protected def convertRowValueToBasicJavaType(def val) {
        return val
    }


    protected def getAllData() {
        if (!allData) {
            allData = readJson(ALL_DATA_FILENAME)
        }
        return allData
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    protected def readJson(def fileName) {
        def jsonArray = new JSONArray(AbstractQueryExecutorIntegrationTest.getResourceAsStream("/${resultsJsonFolder}${fileName}").text)
        def data = []
        jsonArray.length().times { index ->
            def row = jsonArray.get(index)
            data << jsonObjectToMap(row)
        }
        return data
    }

    protected abstract def jsonObjectToMap(jsonObject)

    protected static def jsonArrayToList(jsonArray) {
        def list = []
        jsonArray.length().times { index ->
            def arrayVal = jsonArray.get(index)
            list << arrayVal
        }
        return list
    }

    @After
    void after() {
        queryExecutor.filterState.clearAllFilters()
        queryExecutor.selectionState.clearAllFilters()
    }

    @Test
    void "field names"() {
        def fieldNames = queryExecutor.getFieldNames(DATABASE_NAME, TABLE_NAME)
        def expected = getAllData()[0].keySet()
        AssertUtils.assertEqualCollections(expected, fieldNames.data)
    }

    @Test
    void "field names without a table returns empty collection"() {
        def names = queryExecutor.getFieldNames(DATABASE_NAME, "zsz")
        assert names != null
        assert !names.data
    }

    @Test
    void "query all"() {
        def result = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(getAllData(), result)
    }

    @Test
    void "query WHERE"() {
        def whereStateClause = new OrWhereClause(whereClauses: [new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'VA'), new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC')])
        def salaryAndStateClause = new AndWhereClause(whereClauses: [new SingularWhereClause(lhs: 'salary', operator: '>=', rhs: 100000), whereStateClause])
        def expected = rows(0, 2, 4)
        def query = createQueryWithWhereClause(salaryAndStateClause)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "group by and sort"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def groupByCityClause = new GroupByFieldClause(field: 'city')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def sortByCityClause = new SortClause(fieldName: 'city', sortOrder: SortOrder.DESCENDING)
        def salaryAggregateClause = new AggregateClause(name: 'salary_sum', operation: 'sum', field: 'salary')
        def expected = readJson('groupByStateAsc_cityDesc_aggregateSalary.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause, groupByCityClause],
                aggregates: [salaryAggregateClause],
                sortClauses: [sortByStateClause, sortByCityClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "group by average"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def salaryAverageClause = new AggregateClause(name: 'salary_avg', operation: 'avg', field: 'salary')
        def expected = readJson('groupByStateAsc_avgSalary.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [salaryAverageClause],
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "group by min"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def salaryMinClause = new AggregateClause(name: 'salary_min', operation: 'min', field: 'salary')
        def expected = readJson('groupByStateAsc_minSalary.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [salaryMinClause],
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "group by max"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def salaryMaxClause = new AggregateClause(name: 'salary_max', operation: 'max', field: 'salary')
        def expected = readJson('groupByStateAsc_maxSalary.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [salaryMaxClause],
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "count all fields"() {
        def countClause = new AggregateClause(name: 'counter', operation: 'count', field: '*')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                aggregates: [countClause]), QueryOptions.FILTERED_DATA)
        def expected = readJson('count.json')
        assertOrderedQueryResult(expected, result)
    }

    @Test
    @Ignore("ignored while fixing Hive test")
    void "count field with missing value"() {
        def countClause = new AggregateClause(name: 'counter', operation: 'count', field: 'lastname')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                aggregates: [countClause]), QueryOptions.FILTERED_DATA)
        def expected = readJson('count_missing_field.json')
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "group by count"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def countClause = new AggregateClause(name: 'counter', operation: 'count', field: '*')
        def expected = readJson('groupByStateAsc_count.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [countClause],
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }


    @Test
    void "group by count with limit"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def limitClause = new LimitClause(limit: 2)
        def countClause = new AggregateClause(name: 'counter', operation: 'count', field: '*')
        def expected = readJson('groupByStateAsc_limit.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [countClause],
                limitClause: limitClause,
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "group by count with offset"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def offsetClause = new OffsetClause(offset: 1)
        def countClause = new AggregateClause(name: 'counter', operation: 'count', field: '*')
        def expected = readJson('groupByStateAsc_offset.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause],
                aggregates: [countClause],
                offsetClause: offsetClause,
                sortClauses: [sortByStateClause]), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "distinct"() {
        def expected = readJson('distinct.json')
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER, fields: ['state'], isDistinct: true), QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "distinct with limit"() {
        def expected = readJson('distinct_limit.json')
        def limitClause = new LimitClause(limit: 2)
        // sort so the order is known
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                fields: ['state'],
                isDistinct: true,
                sortClauses: [sortByStateClause],
                limitClause: limitClause), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "distinct with offset"() {
        def expected = readJson('distinct_offset.json')
        def offsetClause = new OffsetClause(offset: 2)
        // sort so the order is known
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                fields: ['state'],
                isDistinct: true,
                sortClauses: [sortByStateClause],
                offsetClause: offsetClause), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "distinct with offset and limit"() {
        def expected = readJson('distinct_offset_limit.json')
        def offsetClause = new OffsetClause(offset: 1)
        def limitClause = new LimitClause(limit: 1)

        // sort so the order is known
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                fields: ['state'],
                isDistinct: true,
                sortClauses: [sortByStateClause],
                offsetClause: offsetClause,
                limitClause: limitClause), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "distinct with offset and limit more than remaining elements"() {
        def expected = readJson('distinct_offset_limit_morethanmax.json')
        def offsetClause = new OffsetClause(offset: 1)

        // the actual number elements available is less than 10
        def limitClause = new LimitClause(limit: 10)

        // sort so the order is known
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                fields: ['state'],
                isDistinct: true,
                sortClauses: [sortByStateClause],
                offsetClause: offsetClause,
                limitClause: limitClause), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "offset greater than total number of results"() {
        def offsetClause = new OffsetClause(offset: 100)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER, offsetClause: offsetClause),
                QueryOptions.FILTERED_DATA)
        assert result.data.isEmpty()
    }


    @Test
    void "add filter"() {
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // apply a filter and make sure only that data is returned
        queryExecutor.filterState.addFilter(filterId, dcStateFilter)
        def dcStateResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)
        def dcStateRecords = rows(1, 2, 5)
        assertUnorderedQueryResult(dcStateRecords, dcStateResult)

        // apply another filter and make sure both are applied
        def salaryFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 85000))
        queryExecutor.filterState.addFilter(filterId, salaryFilter)

        def dcStateWithSalaryFilterRecords = rows(2, 5)
        def dcStateWithSalaryResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(dcStateWithSalaryFilterRecords, dcStateWithSalaryResult)
    }

    @Test
    void "remove filter"() {
        // add some filters that can be removed (the add filters are tested separately)
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))

        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))
        queryExecutor.filterState.addFilter(filterId, dcStateFilter)

        def salaryFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 85000))
        queryExecutor.filterState.addFilter(filterId, salaryFilter)

        queryExecutor.filterState.removeFilter(filterId)
        def allDataResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(getAllData(), allDataResult)
    }

    @Test
    void "ignore filters"() {
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // apply a filter, but execute the query that bypasses the filters, so all data should be returned
        queryExecutor.filterState.addFilter(filterId, dcStateFilter)
        def allDataResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.ALL_DATA)
        assertUnorderedQueryResult(getAllData(), allDataResult)
    }

    @Test
    void "clear filters"() {
        def filterId = new FilterKey(UUID.randomUUID(), new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // addFilter is tested separately, so we can be confident the filter is added properly
        queryExecutor.filterState.addFilter(filterId, dcStateFilter)

        // clear the filters, and there should be no filters applied
        queryExecutor.filterState.clearAllFilters()

        def result = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)

        assertUnorderedQueryResult(getAllData(), result)
    }

    @Test
    void "group by derived field"() {
        def groupByMonthClause = new GroupByFunctionClause(name: 'hire_month', operation: 'month', field: 'hiredate')
        def salaryAggregateClause = new AggregateClause(name: 'salary_sum', operation: 'sum', field: 'salary')
        def sortByMonth = new SortClause(fieldName: 'hire_month', sortOrder: SortOrder.ASCENDING)

        def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME),
                groupByClauses: [groupByMonthClause], aggregates: [salaryAggregateClause], sortClauses: [sortByMonth])

        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        def expected = readJson('groupByMonth.json')

        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE less than"() {
        def whereLessThan = new SingularWhereClause(lhs: 'salary', operator: '<', rhs: 61000)
        def query = createQueryWithWhereClause(whereLessThan)

        def expected = rows(3, 7)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE less than or equal"() {
        def whereLessThanOrEqual = new SingularWhereClause(lhs: 'salary', operator: '<=', rhs: 60000)
        def query = createQueryWithWhereClause(whereLessThanOrEqual)
        def expected = rows(3, 7)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE greater than"() {
        def whereGreaterThan = new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 118000)
        def query = createQueryWithWhereClause(whereGreaterThan)
        def expected = rows(2)

        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE greater than or equal"() {
        def whereGreaterThanOrEqual = new SingularWhereClause(lhs: 'salary', operator: '>=', rhs: 118000)
        def query = createQueryWithWhereClause(whereGreaterThanOrEqual)
        def expected = rows(2, 4)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE not equal"() {
        def whereNotEqual = new SingularWhereClause(lhs: 'state', operator: '!=', rhs: 'VA')
        def query = createQueryWithWhereClause(whereNotEqual)
        def expected = rows(1, 2, 5, 6)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE IN"() {
        def whereIn = new SingularWhereClause(lhs: 'state', operator: 'in', rhs: ['MD', 'DC'])
        def query = createQueryWithWhereClause(whereIn)
        def expected = rows(1, 2, 5, 6)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query WHERE not IN"() {
        def whereNotIn = new SingularWhereClause(lhs: 'state', operator: 'notin', rhs: ['VA', 'DC'])
        def query = createQueryWithWhereClause(whereNotIn)
        def expected = rows(6)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "query with limit"() {
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER, limitClause: new LimitClause(limit: 2)), QueryOptions.FILTERED_DATA)
        // should be limited to 2 results
        assert result.data.size == 2
    }

    @Test
    void "query with offset"() {
        def expected = readJson('offset.json')
        def offsetClause = new OffsetClause(offset: 4)
        // sort so the order is known
        def sortBySalaryClause = new SortClause(fieldName: 'salary', sortOrder: SortOrder.ASCENDING)
        def result = queryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                sortClauses: [sortBySalaryClause],
                offsetClause: offsetClause), QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    // not every date operator combination is tested dates since the other query tests exercise the operators extensively

    @Test
    void "date greater than or equals"() {
        def expected = readJson('dateGreaterThan.json')
        def whereGreaterThanOrEqualToDate = new SingularWhereClause(lhs: 'hiredate', operator: '>=', rhs: DateUtils.tryToParseDate("2012-09-15T00:00:00Z"))
        def query = createQueryWithWhereClause(whereGreaterThanOrEqualToDate)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "date equals"() {
        def expected = rows(0)
        def whereEqualsDate = new SingularWhereClause(lhs: 'hiredate', operator: '=', rhs: DateUtils.tryToParseDate("2012-09-15T00:00:00Z"))
        def query = createQueryWithWhereClause(whereEqualsDate)
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test
    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    @SuppressWarnings('MethodSize') // there is a lot of setup in this method but it is pretty straightforward and would be harder to read if extracted
    void "query group aggregates results"() {
        def whereClause1 = new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'VA')
        def query1 = createQueryWithWhereClause(whereClause1)

        def whereClause2 = new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'MD')
        def query2 = createQueryWithWhereClause(whereClause2)

        def whereClause3 = new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC')
        def query3 = createQueryWithWhereClause(whereClause3)

        def queryGroup = new QueryGroup()
        queryGroup.queries << query1
        queryGroup.queries << query2
        queryGroup.queries << query3

        def expected = readJson('queryGroup.json')
        def queryGroupResult = queryExecutor.execute(queryGroup, QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, queryGroupResult)
    }

    @Test
    void "select a subset of fields"() {
        def fields = ["_id", "firstname", "lastname", "salary"]
        def query = new Query(filter: ALL_DATA_FILTER, fields: fields)
        // one of the lastname values is not in the raw json, so don't include it in
        // the expected data
        def expected = getAllData().collect { row ->
            row.subMap(fields.findAll { row[it] })
        }
        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "select a subset of fields from a group by query"() {
        def groupByMonthClause = new GroupByFunctionClause(name: 'hire_month', operation: 'month', field: 'hiredate')
        def salaryAggregateClause = new AggregateClause(name: 'salary_sum', operation: 'sum', field: 'salary')
        def sortByMonth = new SortClause(fieldName: 'hire_month', sortOrder: SortOrder.ASCENDING)

        def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME),
                groupByClauses: [groupByMonthClause], aggregates: [salaryAggregateClause], sortClauses: [sortByMonth], fields: ["hire_month"])

        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        def expected = readJson('groupByMonth.json')
        assertOrderedQueryResult(expected, result)
    }

    @Test
    void "add selection"() {
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // apply a selection and make sure only that data is returned
        queryExecutor.selectionState.addFilter(filterId, dcStateFilter)
        def dcStateResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_AND_SELECTED_DATA)
        def dcStateRecords = rows(1, 2, 5)
        assertUnorderedQueryResult(dcStateRecords, dcStateResult)

        // apply a filter and make sure both are applied
        def salaryFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 85000))
        queryExecutor.filterState.addFilter(filterId, salaryFilter)

        def dcStateWithSalaryFilterRecords = rows(2, 5)
        def dcStateWithSalaryResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_AND_SELECTED_DATA)
        assertUnorderedQueryResult(dcStateWithSalaryFilterRecords, dcStateWithSalaryResult)
    }

    @Test
    void "remove selection"() {
        // add some filters that can be removed (the add filters are tested separately)
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))

        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))
        queryExecutor.selectionState.addFilter(filterId, dcStateFilter)

        def salaryFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 85000))
        queryExecutor.selectionState.addFilter(filterId, salaryFilter)

        queryExecutor.selectionState.removeFilter(filterId)
        def allDataResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_AND_SELECTED_DATA)
        assertUnorderedQueryResult(getAllData(), allDataResult)
    }

    @Test
    void "ignore selection"() {
        UUID uuid = UUID.randomUUID()
        def filterId = new FilterKey(uuid, new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // apply a filter, but execute the query that bypasses the filters, so all data should be returned
        queryExecutor.selectionState.addFilter(filterId, dcStateFilter)
        def allDataResult = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.ALL_DATA)
        assertUnorderedQueryResult(getAllData(), allDataResult)
    }

    @Test
    void "clear selection"() {
        def filterId = new FilterKey(UUID.randomUUID(), new DataSet(databaseName: DATABASE_NAME, tableName: TABLE_NAME))
        def dcStateFilter = createFilterWithWhereClause(new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // addFilter is tested separately, so we can be confident the filter is added properly
        queryExecutor.selectionState.addFilter(filterId, dcStateFilter)

        // clear the filters, and there should be no filters applied
        queryExecutor.selectionState.clearAllFilters()

        def result = queryExecutor.execute(ALL_DATA_QUERY, QueryOptions.FILTERED_AND_SELECTED_DATA)
        assertUnorderedQueryResult(getAllData(), result)
    }

    protected def assertOrderedQueryResult(expected, actual) {
        assertQueryResult(expected, actual, true)
    }

    protected def assertUnorderedQueryResult(expected, actual) {
        assertQueryResult(expected, actual, false)
    }

    protected def assertQueryResult(expected, actual, ordered) {
        def data = actual.data
        assert expected.size() == data.size()

        // compare row by row for better error messages on failure
        data.eachWithIndex { map, index ->
            // our expected data is json based. fixed-schema databases will have null values for those fields in
            // the actual data, so remove them to make it easier to compare
            // but don't remove the _id field. that is expected in all mongo rows (and it may be empty for aggregations)
            def cleanedRow = map.findAll { it.value || it.key == '_id'}
            if (ordered) {
                compareRowOrdered(expected[index], cleanedRow, "Row ${index}")
            } else {
                compareRowUnordered(expected, cleanedRow, "Could not find a match for row ${index}: ${cleanedRow} in ${expected}")
            }
        }
    }

    private static void compareRowOrdered(expectedRow, actualRow, message) {
        assert expectedRow == actualRow: message
    }

    private static void compareRowUnordered(expectedRows, actualRow, message) {
        def match = false
        expectedRows.each { expectedRow ->
            if (!match && expectedRow == actualRow) {
                match = true
            }
        }
        assert match: message
    }

    /**
     * Returns the data from {@link #allData} with the specified indices
     * @param indices The indices whose rows are being returned
     */
    protected def rows(int ... indices) {
        def allData = getAllData()
        def data = []
        indices.each {
            data << allData[it]
        }
        data
    }

    /**
     * Utility method for creating a simple query that uses a filter that just sets up the database, table and where
     * clause
     * @param whereClause
     * @return
     */
    private static def createQueryWithWhereClause(whereClause) {
        return new Query(filter: createFilterWithWhereClause(whereClause))
    }

    /**
     * Utility method for creating a simple filter that uses a filter that uses the specified where clasue
     * @param whereClause
     * @return
     */
    private static def createFilterWithWhereClause(whereClause) {
        return new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: whereClause)
    }

}