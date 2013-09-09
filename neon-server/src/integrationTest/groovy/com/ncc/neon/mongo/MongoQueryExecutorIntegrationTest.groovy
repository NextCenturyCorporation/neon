package com.ncc.neon.mongo

import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.DistanceUnit
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.util.LatLon
import org.bson.types.ObjectId
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

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
@ActiveProfiles("mongo-integrationtest")
class MongoQueryExecutorIntegrationTest extends AbstractQueryExecutorIntegrationTest {

    @BeforeClass
    static void beforeClass() {
        insertData()
    }

    @AfterClass
    static void afterClass() {
        deleteData()
    }

    @Override
    protected def rowToMap(row) {
        return row.defaultRow
    }

    @Override
    protected def convertRowValueToBasicJavaType(def val) {
        if (val instanceof ObjectId) {
            return val.toString()
        }
        return super.convertRowValueToBasicJavaType(val)
    }

    static void insertData() {
        def db = MongoIntegrationTestContext.MONGO.getDB(DATABASE_NAME)
        def collection = db.getCollection(TABLE_NAME)
        def dbList = parseJSON("/mongo-json/${ALL_DATA_FILENAME}")
        collection.insert(dbList)
        collection.ensureIndex(new BasicDBObject("location", "2dsphere"))
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static def parseJSON(resourcePath) {
        return JSON.parse(MongoQueryExecutorIntegrationTest.getResourceAsStream(resourcePath).text)
    }

    private static void deleteData() {
        def db = MongoIntegrationTestContext.MONGO.getDB(DATABASE_NAME)
        db.dropDatabase()
    }

    @Test
    void "query near location"() {
        def withinDistance = new WithinDistanceClause(
                locationField: "location",
                center: new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d),
                distance: 35d,
                distanceUnit: DistanceUnit.MILE
        )
        def expected = rows(2, 0)
        def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: withinDistance))

        def result = queryExecutor.execute(query, false)
        assertOrderedQueryResult(expected, result)
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
        def whereClause = new AndWhereClause(whereClauses: [withinDistance, dcStateClause])
        def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: whereClause))

        def result = queryExecutor.execute(query, false)
        assertOrderedQueryResult(expected, result)
    }

    // TODO: NEON-554 Once the ID field issue is figured out for hive, move these up to the abstract test
    @Test
    void "set selection WHERE"() {
        def dcStateFilter = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: new SingularWhereClause(lhs: 'state', operator: '=', rhs: 'DC'))
        queryExecutor.setSelectionWhere(dcStateFilter)

        def result = queryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        def dcStateRecords = rows(1, 2, 5)
        assertUnorderedQueryResult(dcStateRecords, result)
    }

    @Test
    void "add remove selection ids"() {
        def expected = rows(1, 2, 5)
        def ids = expected.collect { it._id }
        queryExecutor.addSelectedIds(ids)

        def result = queryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertUnorderedQueryResult(expected, result)

        // remove the items from the expectations since they will be removed from the selected ids
        def removedId1 = ids.remove(2)
        expected.remove(2)

        def removedId2 = ids.remove(0)
        expected.remove(0)

        queryExecutor.removeSelectedIds([removedId1, removedId2])
        result = queryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertUnorderedQueryResult(expected, result)
    }

    @Test
    void "clear selection"() {
        def ids = rows(1, 2).collect { it._id }

        // adding ids already been tested, so we can be confident the ids are added properly
        queryExecutor.addSelectedIds(ids)

        queryExecutor.clearSelection()
        def result = queryExecutor.getSelectionWhere(ALL_DATA_FILTER)

        // no results should be returned from the selection query since the selection was cleared
        assert !result.iterator().hasNext()

    }

    @Test
    void "set selection by id"() {
        def expected = rows(1, 2)
        def ids = expected.collect { it._id }
        queryExecutor.setSelectedIds(ids)
        def result = queryExecutor.getSelectionWhere(ALL_DATA_FILTER)
        assertUnorderedQueryResult(expected, result)
    }

}
