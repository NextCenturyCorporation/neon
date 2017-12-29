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

package com.ncc.neon.elasticsearch

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.IntegrationTestContext
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.LimitClause
import com.ncc.neon.query.elasticsearch.ElasticSearchRestQueryExecutor
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.util.AssertUtils

/**
 * Integration test that verifies the neon server properly translates elasticsearch queries.
 * These tests parallel the acceptance tests in the javascript client query acceptance tests
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class ElasticSearchQueryExecutorIntegrationTest extends AbstractQueryExecutorIntegrationTest {
    private static final String HOST_STRING = System.getProperty("elasticsearch.host")
    private static final int NUMBER_OF_SCROLL_RECORDS = 20000

    private ElasticSearchRestQueryExecutor elasticSearchQueryExecutor

    protected String getResultsJsonFolder() {
        return "elasticsearch-json/"
    }

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setElasticSearchQueryExecutor(ElasticSearchRestQueryExecutor elasticSearchQueryExecutor) {
        this.elasticSearchQueryExecutor = elasticSearchQueryExecutor
    }

    @Before
    void before() {
        // Establish the connection, or skip the tests if no host was specified
        Assume.assumeTrue(HOST_STRING != null && HOST_STRING != "")
        this.elasticSearchQueryExecutor.connectionManager.currentRequest = new ConnectionInfo(host: HOST_STRING, dataSource: DataSources.elasticsearch)
    }

    protected ElasticSearchRestQueryExecutor getQueryExecutor() {
        return elasticSearchQueryExecutor
    }

    @Override
    protected def convertRowValueToBasicJavaType(def val) {
        return super.convertRowValueToBasicJavaType(val)
    }

    @Override
    protected def jsonObjectToMap(def jsonObject, def parseDates) {
        def map = [:]
        jsonObject.keys().each { key ->
            def value = jsonObject.get(key)
            if (parseDates && key =~ AbstractQueryExecutorIntegrationTest.DATE_FIELD_REGEX) {
                DateTimeFormatter formatIn = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                map[key] = formatIn.withZoneUTC().parseDateTime(value).toString()
            } else if (value instanceof JSONArray) {
                map[key] = jsonArrayToList(value)
            } else if (value instanceof JSONObject) {
                map[key] = jsonObjectToMap(value, parseDates)
            } else {
                map[key] = value
            }
        }
        return map
    }

    @Test
    void "query with index wildcards"() {
        def wildcardFilter = new Filter(databaseName: DATABASE_NAME.substring(0, DATABASE_NAME.length() - 2) + '*',
                tableName: TABLE_NAME)
        def query = new Query(filter: wildcardFilter)
        def result = queryExecutor.execute(query, QueryOptions.DEFAULT_OPTIONS)
        assertUnorderedQueryResult(getAllData(), result)
    }

    @Test
    void "field names with wildcards"() {
        def fieldNames = queryExecutor.getFieldNames(DATABASE_NAME.substring(0, DATABASE_NAME.length() - 2) + '*',
                TABLE_NAME.substring(0, TABLE_NAME.length() - 2) + '*')
        def expected = getNestedObjects(getAllData()[0], null)
        AssertUtils.assertEqualCollections(expected, fieldNames)
    }

    @Test
    void "show tables with wildcard"() {
        def tables = queryExecutor.showTables(DATABASE_NAME.substring(0, DATABASE_NAME.length() - 2) + '*')
        assert tables.contains(TABLE_NAME)
    }

    @Test
    void "field types with wildcards"() {
        def fieldTypes = queryExecutor.getFieldTypes(DATABASE_NAME.substring(0, DATABASE_NAME.length() - 2) + '*',
                TABLE_NAME.substring(0, TABLE_NAME.length() - 2) + '*')
        def expected = getAllTypes()

        //AssertUtils.assertEqualCollections(expected, fieldTypes)
        compareRowUnordered(expected, fieldTypes, "Returned values, ${fieldTypes}, did not match expected values, ${expected}")
    }

    @Test
    void "query uses scroll to get all the data"() {
        def result = queryExecutor.execute(
                new Query(
                        filter: new Filter(databaseName: DATABASE_NAME, tableName: "many-records"),
                        // Ask for more records than are there
                        limitClause: new LimitClause(limit: NUMBER_OF_SCROLL_RECORDS + 10000)
                ),
                QueryOptions.DEFAULT_OPTIONS)
        assert result.data.size() == NUMBER_OF_SCROLL_RECORDS
    }

    @Test
    void "query uses scroll to get less than all the data"() {
        final int MANY_RECORDS = 15000
        def result = queryExecutor.execute(
                new Query(
                        filter: new Filter(databaseName: DATABASE_NAME, tableName: "many-records"),
                        // Ask for more records than are there
                        limitClause: new LimitClause(limit: MANY_RECORDS)
                ),
                QueryOptions.DEFAULT_OPTIONS)
        assert result.data.size() == MANY_RECORDS
    }
}
