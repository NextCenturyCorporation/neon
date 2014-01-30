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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import java.sql.Timestamp
/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
@ActiveProfiles("integrationtest")
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
        String connectionId = this.hiveQueryExecutor.connectionManager.connect(CONNECTION_INFO)
        this.hiveQueryExecutor.connectionManager.currentRequestConnection.connectionId = connectionId
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