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

package com.ncc.neon.hive
import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.IntegrationTestContext
import com.ncc.neon.connect.*
import com.ncc.neon.query.hive.HiveQueryExecutor
import com.ncc.neon.util.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.sql.Timestamp

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class HiveQueryExecutorIntegrationTest extends AbstractQueryExecutorIntegrationTest {

    private static final String HOST_STRING = System.getProperty("hive.host", "localhost:10000")


    /** a separate connection used for inserting/deleting test data */
    private static JdbcClient jdbcClient

    private static final ConnectionInfo CONNECTION_INFO = new ConnectionInfo(connectionUrl: HOST_STRING, dataSource: DataSources.hive)
    private static final ConnectionClientFactory CONNECTION_FACTORY = new JdbcConnectionClientFactory("org.apache.hive.jdbc.HiveDriver", "hive2")

    @BeforeClass
    static void beforeClass() {
        jdbcClient = CONNECTION_FACTORY.createConnectionClient(CONNECTION_INFO)
    }

    @AfterClass
    static void afterClass() {
        CONNECTION_FACTORY.dataSource?.close()
    }

    HiveQueryExecutor hiveQueryExecutor


    @SuppressWarnings("JUnitPublicNonTestMethod")
    @Autowired
    void setHiveQueryExecutor(HiveQueryExecutor hiveQueryExecutor) {
        this.hiveQueryExecutor = hiveQueryExecutor
        this.hiveQueryExecutor.connectionManager.connect(CONNECTION_INFO)
    }

    @Override
    protected def getQueryExecutor(){
        hiveQueryExecutor
    }

    protected String getResultsJsonFolder() {
        return "hive-json/"
    }

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
            } else {
                map[key] = value
            }
        }
        return map
    }

    @Override
    protected def convertRowValueToBasicJavaType(def val) {
        if (val instanceof Timestamp) {
            // even though Timestamp is a Date, we just want to return a Date class since that's what we're comparing against
            return new Date(val.time)
        }
        return super.convertRowValueToBasicJavaType(val)
    }

}