package com.ncc.neon.hive

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.jdbc.JdbcClient
import com.ncc.neon.query.jdbc.JdbcQueryExecutor
import com.ncc.neon.util.AssertUtils
import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

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

/**
 * Created with IntelliJ IDEA.
 * User: mtamayo
 * Date: 4/28/13
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
//@RunWith(SpringJUnit4ClassRunner)
//@ContextConfiguration(classes = HiveIntegrationTestContext)
//@WebAppConfiguration
//@ActiveProfiles("hive-integrationtest")
class HiveQueryExecutorIntegrationTest {

    private static final String DATASOURCE_NAME = 'integrationTest'

    private static final String DATASET_ID = 'records'

    /** all of the data in the test file */
    private static final ALL_DATA = loadTestData("/hive/testData.csv")

    /** a filter that just includes all of the data (no WHERE clause) */
    private static final ALL_DATA_FILTER = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID)

    /** a simple query that returns all of the data */
    private static final ALL_DATA_QUERY = new Query(filter: ALL_DATA_FILTER)

//    @Autowired
    private JdbcQueryExecutor jdbcQueryExecutor

    // TODO: Figure out how to inject this or initialize it in a better way
    @SuppressWarnings("FieldName")
    private static final JdbcClient jdbcClient = new JdbcClient("org.apache.hive.jdbc.HiveDriver", "hive2", "default", "localhost:10000")

//    @BeforeClass
    static void beforeClass() {
        insertData()
    }

//    @AfterClass
    static void afterClass() {
        deleteData()
    }

//    @Test
    void "field names"() {
        def fieldNames = jdbcQueryExecutor.getFieldNames(DATASOURCE_NAME, DATASET_ID)
        def expected = ['id', 'donation_date', 'amount', 'charity_id', 'charity_name', 'donor_id', 'donor_city', 'donor_state', 'donor_firstname', 'donor_lastname']
        AssertUtils.assertEqualCollections(expected, fieldNames)
    }

//    @Test
    void "query all"() {
        def result = jdbcQueryExecutor.execute(ALL_DATA_QUERY, false)
        assertQueryResult(ALL_DATA, result)
    }

//    @Test
    void "query WHERE"() {
        def whereClause = new AndWhereClause(whereClauses: [new SingularWhereClause(lhs: "donor_state", operator: "=", rhs: "CA"), new SingularWhereClause(lhs: "donor_city", operator: "=", rhs: "Fresno")])
        def filter = new Filter(dataSourceName: DATASOURCE_NAME, datasetId: DATASET_ID, whereClause: whereClause)
        def result = jdbcQueryExecutor.execute(new Query(filter: filter), false)
        def expected = rows(4, 6, 8)
        assertQueryResult(expected, result)
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static List<Map> loadTestData(String filename) {
        def dataset = []
        new File(HiveQueryExecutorIntegrationTest.getResource(filename).toURI()).splitEachLine(",") { fields ->
            dataset << [
                "id": fields[0].toInteger(),
                "donation_date": fields[1].toLong(),
                "amount": fields[2].toDouble(),
                "charity_id": fields[3].toInteger(),
                "charity_name": fields[4],
                "donor_id": fields[5].toInteger(),
                "donor_city": fields[6],
                "donor_state": fields[7],
                "donor_firstname": fields[8],
                "donor_lastname": fields[9]
            ]
        }
        return dataset
    }

    private static def assertQueryResult(expected, actual) {
        int actualCount = 0

        actual.eachWithIndex { row, index ->
            def actualRow = row.jdbcRow
            def expectedRow = expected[index]
            assert expectedRow == actualRow: "Row ${index}"
            actualCount++
        }

        // the "actual" value's size cannot be determined ahead of time
        assert expected.size() == actualCount
    }

    @SuppressWarnings('CoupledTestCase') // this method incorrectly throws this codenarc error
    private static void insertData() {

        File testTableDdlFile = new File(HiveQueryExecutorIntegrationTest.getResource("/hive/createTestTable.sql").toURI())
        File testTableSourceDataFile = new File(HiveQueryExecutorIntegrationTest.getResource("/hive/testData.csv").toURI())
        File testTableTempDataFile = File.createTempFile("hive", ".csv")
        FileUtils.copyFile(testTableSourceDataFile, testTableTempDataFile)

        jdbcClient.execute("create database ${DATASOURCE_NAME}")
        jdbcClient.execute(testTableDdlFile.text)
        jdbcClient.execute("load data local inpath '${testTableTempDataFile.absolutePath}' overwrite into table ${DATASOURCE_NAME}.${DATASET_ID}")
    }

    private static void deleteData() {
        jdbcClient.execute("drop table ${DATASOURCE_NAME}.${DATASET_ID}")
        jdbcClient.execute("drop database ${DATASOURCE_NAME}")
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