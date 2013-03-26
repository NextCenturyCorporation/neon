package com.ncc.neon.mongo

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.mongo.MongoQueryExecutor
import com.ncc.neon.util.AssertUtils
import com.ncc.neon.util.LatLon
import org.bson.BSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

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
 * Integration test that verifies the neon server properly translates mongo queries.
 * These tests parallel the acceptance tests in the javascript client query acceptance tests
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = MongoIntegrationTestContext)
@WebAppConfiguration
class MongoQueryExecutorIntegrationTest {

    private static final String DATASOURCE_NAME = 'integrationTest'

    private static final String DATASET_ID = 'records'

    /** all of the data in the test file */
    private static final ALL_DATA = readJson('data.json')

    /** a filter that just includes all of the data (no WHERE clause) */
    private static final ALL_DATA_FILTER = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID)

    /** a simple query that returns all of the data */
    private static final ALL_DATA_QUERY = new Query(filter: ALL_DATA_FILTER)


    @Autowired
    private MongoQueryExecutor mongoQueryExecutor

    @BeforeClass
    static void beforeClass() {
        insertData()
    }

    @AfterClass
    static void afterClass() {
        deleteData()
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static def readJson(def fileName) {
        def data = []
        def dbList = parseJSON("/mongo-json/${fileName}")
        dbList.each {
            data << bsonToMap(it)
        }
        return data
    }

    /**
     * Converts the bson object read from a mongo database to a map with standard java types (specifically it
     * expands out the nested BSON object types)
     * @param bson
     * @return
     */
    private static def bsonToMap(bson) {
        // extract out the bson specific encoding so it is easier to perform test comparisons
        return bson.collectEntries { key, value ->
            if (value instanceof BSONObject && value instanceof Map) {
                return [(key): bsonToMap(value)]
            }
            return [(key): value]
        }
    }


    private static void insertData() {
        def db = MongoIntegrationTestContext.MONGO.getDB(DATASOURCE_NAME)
        def collection = db.getCollection(DATASET_ID)
        def dbList = parseJSON("/mongo-json/data.json")
        collection.insert(dbList)
        collection.ensureIndex(new BasicDBObject("location","2dsphere"))
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static def parseJSON(resourcePath) {
        return JSON.parse(MongoQueryExecutorIntegrationTest.getResourceAsStream(resourcePath).text)
    }

    private static void deleteData() {
        def db = MongoIntegrationTestContext.MONGO.getDB(DATASOURCE_NAME)
        db.dropDatabase()
    }

    @Test
    void "field names"() {
        def fieldNames = mongoQueryExecutor.getFieldNames(DATASOURCE_NAME, DATASET_ID)
        def expected = ['_id', 'firstname', 'lastname', 'city', 'state', 'salary', 'hiredate','location']
        AssertUtils.assertEqualCollections(expected, fieldNames)
    }

    @Test
    void "query all"() {
        def result = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)
        assertQueryResult(ALL_DATA, result)
    }

    @Test
    void "query WHERE"() {
        def whereStateClause = new OrWhereClause(whereClauses: [new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'VA'), new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC')])
        def salaryAndStateClause = new AndWhereClause(whereClauses: [new SingularWhereClause(lhs: 'salary', operator: '>=', rhs: 100000), whereStateClause])

        def filter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: salaryAndStateClause)
        def expected = rows(0, 2, 4)
        def result = mongoQueryExecutor.execute(new Query(filter: filter), false)
        assertQueryResult(expected, result)
    }

    @Test
    void "group by and sort"() {
        def groupByStateClause = new GroupByFieldClause(field: 'state')
        def groupByCityClause = new GroupByFieldClause(field: 'city')
        def sortByStateClause = new SortClause(fieldName: 'state', sortOrder: SortOrder.ASCENDING)
        def sortByCityClause = new SortClause(fieldName: 'city', sortOrder: SortOrder.DESCENDING)
        def salaryAggregateClause = new AggregateClause(name: 'salary_sum', operation: 'sum', field: 'salary')
        def expected = readJson('groupByStateAsc_cityDesc_aggregateSalary.json')
        def result = mongoQueryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                groupByClauses: [groupByStateClause, groupByCityClause],
                aggregates: [salaryAggregateClause],
                sortClauses: [sortByStateClause, sortByCityClause]), false)
        assertQueryResult(expected, result)
    }

    @Test
    void "distinct"() {
        def distinctStateClause = new DistinctClause(fieldName: 'state')
        def expected = ["DC", "MD", "VA"]
        def result = mongoQueryExecutor.execute(new Query(filter: ALL_DATA_FILTER,
                distinctClause: distinctStateClause), false).mongoIterable

        AssertUtils.assertEqualCollections(expected, result)
    }

    @Test
    void "set selection WHERE"() {
        def dcStateFilter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))
        mongoQueryExecutor.setSelectionWhere(dcStateFilter);

        def result = mongoQueryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        def dcStateRecords = rows(1, 2, 5)
        assertQueryResult(dcStateRecords, result)
    }

    @Test
    void "set selection by id"() {
        def expected = rows(1, 2)
        def ids = expected.collect { it._id }
        mongoQueryExecutor.setSelectedIds(ids)
        def result = mongoQueryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertQueryResult(expected, result)
    }

    @Test
    void "add remove selection ids"() {
        def expected = rows(1, 2, 5)
        def ids = expected.collect { it._id }
        mongoQueryExecutor.addSelectedIds(ids)

        def result = mongoQueryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertQueryResult(expected, result)

        // remove the items from the expectations since they will be removed from the selected ids
        def removedId1 = ids.remove(2)
        expected.remove(2)

        def removedId2 = ids.remove(0)
        expected.remove(0)

        mongoQueryExecutor.removeSelectedIds([removedId1, removedId2])
        result = mongoQueryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertQueryResult(expected, result)
    }

    @Test
    void "clear selection"() {
        def ids = rows(1, 2).collect { it._id }

        // adding ids already been tested, so we can be confident the ids are added properly
        mongoQueryExecutor.addSelectedIds(ids)

        mongoQueryExecutor.clearSelection()
        def result = mongoQueryExecutor.getSelectionWhere(ALL_DATA_FILTER)

        // no results should be returned from the selection query since the selection was cleared
        assert !result.iterator().hasNext()

    }

    @Test
    @SuppressWarnings('MethodSize') // In this case, allow the long method because it is necessary to add a filter before removing the filter and having this all in one method maeks the test read more smoothly
    void "apply and remove filter"() {
        def dcStateFilter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // apply a filter and make sure only that data is returned
        def dcFilterId = mongoQueryExecutor.addFilter(dcStateFilter)
        def dcStateResult = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)
        def dcStateRecords = rows(1, 2, 5)
        assertQueryResult(dcStateRecords, dcStateResult)

        // verify that if the query is supposed to include the filtered data, all data is returned
        def allDataResult = mongoQueryExecutor.execute(ALL_DATA_QUERY, true)
        assertQueryResult(ALL_DATA, allDataResult)

        // apply another filter and make sure both are applied
        def salaryFilter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 85000))
        def salaryFilterId = mongoQueryExecutor.addFilter(salaryFilter)

        def dcStateWithSalaryFilterRecords = rows(2, 5)
        def dcStateWithSalaryResult = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)
        assertQueryResult(dcStateWithSalaryFilterRecords, dcStateWithSalaryResult)

        // remove each filter and re-execute the queries
        mongoQueryExecutor.removeFilter(salaryFilterId)
        dcStateResult = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)
        assertQueryResult(dcStateRecords, dcStateResult)

        mongoQueryExecutor.removeFilter(dcFilterId)
        allDataResult = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)
        assertQueryResult(ALL_DATA, allDataResult)
    }

    @Test
    void "clear filters"() {
        def dcStateFilter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))

        // addFilter is tested separately, so we can be confident the filter is added properly
        mongoQueryExecutor.addFilter(dcStateFilter)

        // clear the filters, and there should be no filters applied
        mongoQueryExecutor.clearFilters()

        def result = mongoQueryExecutor.execute(ALL_DATA_QUERY, false)

        assertQueryResult(ALL_DATA, result)
    }

    @Test
    void "group by derived field"() {
        def groupByMonthClause = new GroupByFunctionClause(name: 'hire_month', operation: 'month', field: 'hiredate')
        def salaryAggregateClause = new AggregateClause(name: 'salary_sum', operation: 'sum', field: 'salary')
        def sortByMonth = new SortClause(fieldName: 'hire_month', sortOrder: SortOrder.ASCENDING)

        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID),
                groupByClauses: [groupByMonthClause], aggregates: [salaryAggregateClause], sortClauses: [sortByMonth])

        def result = mongoQueryExecutor.execute(query, false)
        def expected = readJson('groupByDerivedField.json')

        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE less than"() {
        def whereLessThan = new SingularWhereClause(lhs: 'salary', operator: '<', rhs: 61000)
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereLessThan))

        def expected = rows(3, 7)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE less than or equal"() {
        def whereLessThanOrEqual = new SingularWhereClause(lhs: 'salary', operator: '<=', rhs: 60000)
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereLessThanOrEqual))
        def expected = rows(3, 7)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE greater than"() {
        def whereGreaterThan = new SingularWhereClause(lhs: 'salary', operator: '>', rhs: 118000)
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereGreaterThan))
        def expected = rows(2)

        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE greater than or equal"() {
        def whereGreaterThanOrEqual = new SingularWhereClause(lhs: 'salary', operator: '>=', rhs: 118000)
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereGreaterThanOrEqual))
        def expected = rows(2, 4)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE not equal"() {
        def whereNotEqual = new SingularWhereClause(lhs: 'state', operator: '!=', rhs: 'VA')
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereNotEqual))
        def expected = rows(1, 2, 5, 6)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE IN"() {
        def whereIn = new SingularWhereClause(lhs: 'state', operator: 'in', rhs: ['MD', 'DC'])
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereIn))
        def expected = rows(1, 2, 5, 6)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query WHERE not IN"() {
        def whereIn = new SingularWhereClause(lhs: 'state', operator: 'notin', rhs: ['VA', 'DC'])
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereIn))
        def expected = rows(6)
        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected, result)
    }

    @Test
    void "query with limit"() {
        def result = mongoQueryExecutor.execute(new Query(filter: ALL_DATA_FILTER, limitClause: new LimitClause(limit: 2)), false)

        // should be limited to 2 results
        def iterator = result.iterator()
        assert iterator.hasNext()
        iterator.next()
        assert iterator.hasNext()
        iterator.next()

        assert !iterator.hasNext()
    }

    @Test
    void "query near location"() {
        def withinDistance = new WithinDistanceClause(
                locationField: "location",
                center: new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d),
                distance: 35d,
                distanceUnit: DistanceUnit.MILE
        )
        def expected = rows(2,0)
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: withinDistance))

        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected,result)
    }

    @Test
    void "query near location and filter on attributes"() {
        def withinDistance = new WithinDistanceClause(
                locationField: "location",
                center: new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d),
                distance: 35d,
                distanceUnit: DistanceUnit.MILE
        )
        def expected = rows(2)
        def dcStateClause = new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC')
        def whereClause = new AndWhereClause(whereClauses: [withinDistance,dcStateClause])
        def query = new Query(filter: new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereClause))

        def result = mongoQueryExecutor.execute(query, false)
        assertQueryResult(expected,result)

    }

    private static def assertQueryResult(expected, actual) {
        int actualCount = 0
        // we know mongo returns BSON objects, so convert them to a map for easier testing
        actual.eachWithIndex { row, index ->
            def actualRow = row.mongoRow.toMap()
            def expectedRow = expected[index]
            assert expectedRow == actualRow: "Row ${index}"
            actualCount++
        }

        // the "actual" value's size cannot be determined ahead of time
        assert expected.size() == actualCount
    }

    /**
     * Returns the data from {@link #ALL_DATA} with the specified indices
     * @param indices The indices whose rows are being returned
     */
    private static def rows(int ... indices) {
        def data = []
        indices.each {
            data << ALL_DATA[it]
        }
        data
    }
}