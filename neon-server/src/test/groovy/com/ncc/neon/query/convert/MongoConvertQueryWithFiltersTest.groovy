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
import com.mongodb.BasicDBObject
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.mongo.MongoConversionStrategy


/*
 Tests the MongoConversionStrategy.convertQueryWithFilterState()
 correctly converts Query objects into MongoQuery objects
*/

class MongoConvertQueryWithFiltersTest extends MongoConvertQueryTest{

    @Override
    protected def convertQuery(query) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.FILTERED_DATA)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(COLUMN_NAME, COLUMN_VALUE)
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.query == simpleQuery
        BasicDBObject andClause = createAndClause()

        assert query.whereClauseParams == andClause
        assert query.selectParams == new BasicDBObject()
    }

    private BasicDBObject createAndClause() {
        BasicDBObject orClause = createOrClause()
        BasicDBObject simpleClause = new BasicDBObject(COLUMN_NAME, COLUMN_VALUE)
        return new BasicDBObject('$and', [orClause, simpleClause])
    }

}
