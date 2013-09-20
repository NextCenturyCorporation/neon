package com.ncc.neon.hive

import com.ncc.neon.AbstractQueryExecutorIntegrationTest
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionState
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.HiveConnection
import com.ncc.neon.query.jdbc.JdbcClient
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
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
@ContextConfiguration(classes = HiveIntegrationTestContext)
@ActiveProfiles("hive-integrationtest")
class HiveQueryExecutorIntegrationTest extends AbstractQueryExecutorIntegrationTest {

    private static final def FIELD_TYPES = [_id: "string", firstname: "string", lastname: "string", city: "string", state: "string", salary: "int", hiredate: "timestamp"]

    /** a separate connection used for inserting/deleting test data */
    private static JdbcClient jdbcClient

    @BeforeClass
    static void beforeClass() {
        jdbcClient = new HiveConnection().connect(new ConnectionInfo(connectionUrl: HiveIntegrationTestContext.HOST_STRING, dataStoreName: DataSources.hive.name()))
        // make sure we clean up just in case something was left over
        deleteData()
        insertData()
    }

    @AfterClass
    static void afterClass() {
        deleteData()
    }

    @Override
    protected ConnectionState createConnectionState() {
        ConnectionState connectionState = new ConnectionState()
        ConnectionInfo info = new ConnectionInfo(dataStoreName: DataSources.hive.name(), connectionUrl: HiveIntegrationTestContext.HOST_STRING)
        connectionState.createConnection(info)
        return connectionState
    }

    protected String getResultsJsonFolder() {
        return "hive-json/"
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static void insertData() {
        Configuration conf = new Configuration()
        def hdfsUrl = System.getProperty("hdfs.url", "hdfs://localhost:8020")
        conf.set("fs.defaultFS", hdfsUrl)
        FileSystem fileSystem = FileSystem.get(conf)

        File testDataFile = new File(HiveQueryExecutorIntegrationTest.getResource("/hive-csv/data.csv").toURI())
        File fieldsFile = new File(HiveQueryExecutorIntegrationTest.getResource("/hive-csv/fields.csv").toURI())
        def destFolder = "/tmp/neonintegrationtest-${new Random().nextInt(Integer.MAX_VALUE)}/"
        def destFolderPath = new Path(destFolder)
        fileSystem.mkdirs(destFolderPath)
        fileSystem.deleteOnExit(destFolderPath)
        copyTestDataFile(fileSystem, testDataFile, destFolder)
        def tableScript = createTableScript(fieldsFile, destFolder)
        jdbcClient.execute("create database ${DATABASE_NAME}")
        jdbcClient.execute(tableScript)
    }

    private static def createTableScript(fieldsFile, destFolder) {
        def fields = fieldsFile.text.split(",").collect { field ->
            // fields staring with _ need to be escaped, otherwise not extra characters necessary
            def escapeChar = field.startsWith("_") ? '`' : ""
            def fieldType = FIELD_TYPES[field]
            // this can happen if our test data changes but the mappings are not updated.
            // this gives a useful error message.
            if (!fieldType) {
                throw new Error("Missing field type for ${field}")
            }
            return "${escapeChar}${field}${escapeChar} ${fieldType}"
        }.join(",")

        def script = new StringBuilder()
        script.append("create external table ${DATABASE_NAME}.${TABLE_NAME} (")
        script.append(fields)
        script.append(") row format delimited fields terminated by ',' location '${destFolder}'")
        return script.toString()
    }

    private static void copyTestDataFile(fileSystem, testDataFile, destFolder) {
        def src = new Path(testDataFile.absolutePath)
        def destName = "${destFolder}hivequeryexecutorintegrationtest.txt"
        def dest = new Path(destName)
        fileSystem.copyFromLocalFile(src, dest)
    }

    private static void deleteData() {
        jdbcClient.execute("drop table if exists ${DATABASE_NAME}.${TABLE_NAME}")
        jdbcClient.execute("drop database if exists ${DATABASE_NAME}")
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