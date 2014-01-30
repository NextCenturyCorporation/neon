package com.ncc.neon.transform

import com.ncc.neon.mongo.MongoIntegrationTestContext
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.Transform
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.mongo.MongoQueryExecutor
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = MongoIntegrationTestContext)
@ActiveProfiles("mongo-integrationtest")
class TransformIntegrationTest {

    @Autowired
    MongoQueryExecutor mongoQueryExecutor

    static final String DATABASE_NAME = 'neonintegrationtest'

    static final String TABLE_NAME = 'records'

    /** a filter that just includes all of the data (no WHERE clause) */
    static final Filter ALL_DATA_FILTER = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME)

    static final Transform TRANSFORM = new Transform(transformName: SalaryTransformer.name)
    static final Transform BAD_TRANSFORM = new Transform(transformName: "blah")

    /** a simple query that returns all of the data */
    static final Query TRANSFORM_ALL_DATA_QUERY = new Query(filter: ALL_DATA_FILTER, transform: TRANSFORM)

    @BeforeClass
    static void beforeClass() {
        MongoQueryExecutor.metaClass.getMongo = { MongoIntegrationTestContext.MONGO }
    }

    @AfterClass
    static void afterClass() {
        MongoQueryExecutor.metaClass = null
    }

    @Test(expected = TransformerNotFoundException)
    void "bad transform throws exception"(){
        Query query = new Query(filter: ALL_DATA_FILTER, transform: BAD_TRANSFORM)
        mongoQueryExecutor.execute(query, QueryOptions.FILTERED_DATA)
    }

    @Test
    void "salary transform alters salaries"() {
        def result = mongoQueryExecutor.execute(TRANSFORM_ALL_DATA_QUERY, QueryOptions.FILTERED_DATA)
        result.data.each{ Map<String, Object> row ->
            String idString = row.get("_id").toString()
            assert row.get("salary") == getExpectedSalary(idString)
        }
    }

    private List getExpectedData() {
        [
                [_id: "5137b623a9f279d831b6fb86", salary: 110000],
                [_id: "5137b623a9f279d831b6fb87", salary: 93500],
                [_id: "5137b623a9f279d831b6fb88", salary: 192500],
                [_id: "5137b623a9f279d831b6fb89", salary: 60500],
                [_id: "5137b623a9f279d831b6fb8a", salary: 129800],
                [_id: "5137b623a9f279d831b6fb8b", salary: 96800],
                [_id: "5137b623a9f279d831b6fb8c", salary: 115500],
                [_id: "5137b623a9f279d831b6fb8d", salary: 66000]
        ]
    }

    private Number getExpectedSalary(String idString){
        Map row = getExpectedData().find{
            it.get("_id") == idString
        }
        return row.get("salary")
    }

}
