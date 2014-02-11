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

package com.ncc.neon.transform
import com.ncc.neon.IntegrationTestContext
import com.ncc.neon.mongo.MongoTestClient
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.Transform
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.mongo.MongoQueryExecutor
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class TransformIntegrationTest {

    private MongoQueryExecutor mongoQueryExecutor

    static final String DATABASE_NAME = 'neonintegrationtest'

    static final String TABLE_NAME = 'records'

    /** a filter that just includes all of the data (no WHERE clause) */
    static final Filter ALL_DATA_FILTER = new Filter(databaseName: DATABASE_NAME, tableName: TABLE_NAME)

    static final Transform TRANSFORM = new Transform(transformName: SalaryTransformer.name)
    static final Transform BAD_TRANSFORM = new Transform(transformName: "blah")

    /** a simple query that returns all of the data */
    static final Query TRANSFORM_ALL_DATA_QUERY = new Query(filter: ALL_DATA_FILTER, transform: TRANSFORM)

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setMongoQueryExecutor(MongoQueryExecutor mongoQueryExectuor) {
        this.mongoQueryExecutor = mongoQueryExectuor
        this.mongoQueryExecutor.metaClass.getMongo = { MongoTestClient.mongoClient }
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
