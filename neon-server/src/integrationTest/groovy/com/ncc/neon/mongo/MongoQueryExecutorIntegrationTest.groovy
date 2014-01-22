package com.ncc.neon.mongo

import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.DistanceUnit
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.mongo.MongoQueryExecutor
import com.ncc.neon.util.DateUtils
import com.ncc.neon.util.LatLon
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
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
        MongoQueryExecutor.metaClass.getMongo = { MongoIntegrationTestContext.MONGO }
    }

    @AfterClass
    static void afterClass() {
        MongoQueryExecutor.metaClass = null
    }

    @Autowired
    MongoQueryExecutor mongoQueryExecutor

    @Override
    protected def getQueryExecutor(){
        mongoQueryExecutor
    }

    @Override
    protected def convertRowValueToBasicJavaType(def val) {
        if (val instanceof ObjectId) {
            return val.toString()
        }
        return super.convertRowValueToBasicJavaType(val)
    }

    @Override
    protected def jsonObjectToMap(jsonObject) {
        def map = [:]
        jsonObject.keys().each { key ->
            def value = jsonObject.get(key)
            if (key =~ AbstractQueryExecutorIntegrationTest.DATE_FIELD_REGEX) {
                map[key] = DateUtils.parseDate(value)
            } else if (value instanceof JSONArray) {
                map[key] = jsonArrayToList(value)
            } else if (value instanceof JSONObject) {
                map[key] = jsonObjectToMap(value)
            } else if (value instanceof String && ObjectId.isValid(value)){
                map[key] = new ObjectId(value)
            } else {
                map[key] = value
            }
        }
        return map
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

        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
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

        def result = queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
        assertOrderedQueryResult(expected, result)
    }

    @Test(expected = NeonConnectionException)
    void "exception thrown rather than trying to create an empty database"() {
        def query = new Query(filter: new Filter(databaseName: "nonexistentdb", tableName: "nonexistentable"))
        queryExecutor.execute(query, QueryOptions.FILTERED_DATA)
    }

}
