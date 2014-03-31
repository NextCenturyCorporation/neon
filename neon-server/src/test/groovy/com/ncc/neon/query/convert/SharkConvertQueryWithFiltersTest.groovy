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
import com.ncc.neon.query.shark.SharkConversionStrategy


/*
 Tests the SharkConversionStrategy.convertQueryWithFilterState()
  correctly converts Query objects into shark queries
*/
class SharkConvertQueryWithFiltersTest extends SharkConvertQueryTest{

    @Override
    protected def convertQuery(query) {
        SharkConversionStrategy conversionStrategy = new SharkConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.FILTERED_DATA)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${COLUMN_NAME} = '${COLUMN_VALUE}'".toLowerCase()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ((${FIELD_NAME} = '${COLUMN_VALUE}' or ${FIELD_NAME_2} = '${COLUMN_VALUE}') and ${COLUMN_NAME} = '${COLUMN_VALUE}')".toLowerCase()
    }

}
