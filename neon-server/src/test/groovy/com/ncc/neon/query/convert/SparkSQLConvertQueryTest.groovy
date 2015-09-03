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

package com.ncc.neon.query.convert

import com.ncc.neon.query.sparksql.SparkSQLConversionStrategy

/*
 Tests the SparkSQLConversionStrategy.convertQuery()
 correctly converts Query objects into Spark SQL queries
*/

class SparkSQLConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def doConvertQuery(query, queryOptions) {
        SparkSQLConversionStrategy conversionStrategy = new SparkSQLConversionStrategy(filterState: filterState, selectionState: selectionState)
        conversionStrategy.convertQuery(query, queryOptions)
    }

    @Override
    void assertSimplestConvertQuery(query) {
        assertStandardSparkSQLStatement(query)
    }

    @Override
    void assertQueryWithWhereClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${FIELD_NAME} = '${FIELD_VALUE}'"
    }

    @Override
    protected void assertQueryWithSortClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} order by ${FIELD_NAME} ASC".toLowerCase()
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} limit $LIMIT_AMOUNT".toLowerCase()
    }

    @Override
    protected void assertQueryWithOffsetClause(query) {
        // OFFSET is not actually implemented in Spark SQL, so it is not included in the query. Neon will adjust the
        // query to include enough results to get the offset and then manually advance the cursor to the correct
        // position.
        assertStandardSparkSQLStatement(query)
    }


    @Override
    protected void assertQueryWithDistinctClause(query) {
        assert query.toLowerCase() == "select DISTINCT * from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
        assert query.toLowerCase() == "select sum(${FIELD_NAME}) as ${FIELD_NAME}_sum from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
        assert query.toLowerCase() == "select ${FIELD_NAME_2}, (pmod(from_unixtime(unix_timestamp(${FIELD_NAME}),'u'),7)+1) as ${FIELD_NAME}_dayOfWeek from ${DATABASE_NAME}.${TABLE_NAME} group by ${FIELD_NAME_2}, (pmod(from_unixtime(unix_timestamp(${FIELD_NAME}),'u'),7)+1)".toLowerCase()
    }

    @Override
    protected void assertQueryWithOrWhereClauseAndFilter(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ((${FIELD_NAME} = '${FIELD_VALUE}' or ${FIELD_NAME_2} = '${FIELD_VALUE}') AND ${FIELD_NAME} = '${FIELD_VALUE}')".toLowerCase()
    }

    @Override
    protected void assertQueryWithWhereNullClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${FIELD_NAME} is null"
    }

    @Override
    protected void assertQueryWithWhereNotNullClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${FIELD_NAME} is not null"
    }

    @Override
    protected void assertQueryWithWhereContainsFooClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${FIELD_NAME} like '%foo%'"
    }

    @Override
    protected void assertQueryWithWhereNotContainsFooClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${FIELD_NAME} not like '%foo%'"
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        assertStandardSparkSQLStatement(query)
    }

    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.toLowerCase() == "select $FIELD_NAME, $FIELD_NAME_2 from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    private void assertStandardSparkSQLStatement(query){
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

}
