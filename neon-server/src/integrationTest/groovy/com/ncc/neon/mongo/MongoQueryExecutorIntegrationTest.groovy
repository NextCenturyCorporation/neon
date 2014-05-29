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

package com.ncc.neon.mongo
import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.IntegrationTestContext
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.DistanceUnit
import com.ncc.neon.query.clauses.GeoIntersectionClause
import com.ncc.neon.query.clauses.GeoWithinClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.mongo.MongoQueryExecutor
import com.ncc.neon.util.DateUtils
import com.ncc.neon.util.LatLon
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Integration test that verifies the neon server properly translates mongo queries.
 * These tests parallel the acceptance tests in the javascript client query acceptance tests
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class MongoQueryExecutorIntegrationTest extends AbstractQueryExecutorIntegrationTest {

	// TODO: NEON-565 another duplication of mongo.host in here
	private static final String HOST_STRING = System.getProperty("mongo.host", "localhost")

	private MongoQueryExecutor mongoQueryExecutor

	@SuppressWarnings('JUnitPublicNonTestMethod')
	@Autowired
	public void setMongoQueryExecutor(MongoQueryExecutor mongoQueryExectuor) {
		this.mongoQueryExecutor = mongoQueryExectuor
	}

	@Before
	void before() {
		this.mongoQueryExecutor.connectionManager.currentRequest = new ConnectionInfo(host: HOST_STRING, dataSource: DataSources.mongo)
	}


	@Override
	protected MongoQueryExecutor getQueryExecutor(){
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
		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
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

		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
		assertOrderedQueryResult(expected, result)
	}

	@Test
	void "query intersection point"() {
		def latLonArray = new LatLon[1][1]
		latLonArray[0][0] = new LatLon(latDegrees: 11.92d, lonDegrees: 19.55d)
		def intersection = new GeoIntersectionClause(
			locationField: "location",
			points: latLonArray,
			geometryType: "Point"
		)
		
		def expected = rows(2)
		def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: intersection))
		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
		assertOrderedQueryResult(expected, result)
	}
	
	@Test
	void "query intersection line"() {
		def latLonArray = new LatLon[1][2]
		latLonArray[0][0] = new LatLon(latDegrees: 11.92d, lonDegrees: 19.55d)
		latLonArray[0][1] = new LatLon(latDegrees: 20d, lonDegrees: 19.55d)
		def intersection = new GeoIntersectionClause(
			locationField: "location",
			points: latLonArray,
			geometryType: "Line"
		)
		
		def expected = rows(2)
		def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: intersection))
		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
		assertOrderedQueryResult(expected, result)
	}
	
	@Test
	void "query intersection polygon"() {
		def latLonArray = new LatLon[1][5]
		latLonArray[0][0] = new LatLon(latDegrees: 12d, lonDegrees: 19d)
		latLonArray[0][1] = new LatLon(latDegrees: 12d, lonDegrees: 20d)
		latLonArray[0][2] = new LatLon(latDegrees: 11d, lonDegrees: 20d)
		latLonArray[0][3] = new LatLon(latDegrees: 11d, lonDegrees: 19d)
		latLonArray[0][4] = new LatLon(latDegrees: 12d, lonDegrees: 19d)
		def intersection = new GeoIntersectionClause(
			locationField: "location",
			points: latLonArray,
			geometryType: "Polygon"
		)
		
		def expected = rows(2,0)
		def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: intersection))
		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
		assertOrderedQueryResult(expected, result)
	}
	 
	@Test
	void "query within polygon"() {
		def latLonArray = new LatLon[1][5]
		latLonArray[0][0] = new LatLon(latDegrees: 12d, lonDegrees: 19d)
		latLonArray[0][1] = new LatLon(latDegrees: 12d, lonDegrees: 20d)
		latLonArray[0][2] = new LatLon(latDegrees: 11d, lonDegrees: 20d)
		latLonArray[0][3] = new LatLon(latDegrees: 11d, lonDegrees: 19d)
		latLonArray[0][4] = new LatLon(latDegrees: 12d, lonDegrees: 19d)
		def within = new GeoWithinClause(
			locationField: "location",
			points: latLonArray
		)
		
		def expected = rows(2,0)
		def query = new Query(filter: new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME, whereClause: within))
		def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
		assertOrderedQueryResult(expected, result)
	}

	@Test(expected = NeonConnectionException)
	void "exception thrown rather than trying to create an empty database"() {
		def query = new Query(filter: new Filter(databaseName: "nonexistentdb", tableName: "nonexistentable"))
		queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
	}

}