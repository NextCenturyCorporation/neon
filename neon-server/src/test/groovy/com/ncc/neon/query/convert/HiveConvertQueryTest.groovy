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

import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.hive.HiveConversionStrategy


/*
 Tests the HiveConversionStrategy.convertQuery()
 correctly converts Query objects into hive queries
*/

class HiveConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def convertQuery(query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.ALL_DATA)
    }

    @Override
    void assertSimplestConvertQuery(query) {
        assertStandardHiveQLStatement(query)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assertStandardHiveQLStatement(query)
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
        // OFFSET is not actually implemented in Hive, so it is not included in the query. Neon will adjust the
        // query to include enough results to get the offset and then manually advance the cursor to the correct
        // position.
        assertStandardHiveQLStatement(query)
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
        assert query.toLowerCase() == "select ${FIELD_NAME_2}, sum(${FIELD_NAME}) as ${FIELD_NAME}_sum from ${DATABASE_NAME}.${TABLE_NAME} group by ${FIELD_NAME_2}, sum(${FIELD_NAME})".toLowerCase()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where (${FIELD_NAME} = '${COLUMN_VALUE}' or ${FIELD_NAME_2} = '${COLUMN_VALUE}')".toLowerCase()
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        assertStandardHiveQLStatement(query)
    }


    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.toLowerCase() == "select $FIELD_NAME, $FIELD_NAME_2 from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    private void assertStandardHiveQLStatement(query){
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

}
