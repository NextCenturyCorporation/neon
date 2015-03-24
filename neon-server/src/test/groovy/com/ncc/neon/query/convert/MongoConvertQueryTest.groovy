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
import com.ncc.neon.query.mongo.MongoConversionStrategy
/*
 Tests the MongoConversionStrategy.convertQuery()
 correctly converts Query objects into MongoQuery objects
*/
class MongoConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def doConvertQuery(query, queryOptions) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        conversionStrategy.convertQuery(query, queryOptions)
    }

    // For many of these query clause tests, the conversion strategy does not need to do anything so it just asserts
    // the "standard query asserts." This is because the mongo query worker classes handle the actual clauses

    @Override
    void assertSimplestConvertQuery(query) {
        standardQueryAsserts(query)
    }

    @Override
    void assertQueryWithWhereClause(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(FIELD_NAME, FIELD_VALUE)
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithSortClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithOffsetClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithDistinctClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithOrWhereClauseAndFilter(query) {
        assert query.query == simpleQuery
        BasicDBObject orClause = createOrClause()
        BasicDBObject filterClause = new BasicDBObject(FIELD_NAME, FIELD_VALUE)
        BasicDBObject compoundClause = new BasicDBObject('$and',[orClause,filterClause])

        assert query.whereClauseParams == compoundClause
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithWhereNullClause(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(FIELD_NAME, null)
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithWhereNotNullClause(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(FIELD_NAME, new BasicDBObject('$ne', null))
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithWhereContainsFooClause(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(FIELD_NAME, new BasicDBObject('$regex', 'foo'))
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        standardQueryAsserts(query)
    }

    protected BasicDBObject createOrClause() {
        BasicDBObject simpleClause1 = new BasicDBObject(FIELD_NAME, FIELD_VALUE)
        BasicDBObject simpleClause2 = new BasicDBObject(FIELD_NAME_2, FIELD_VALUE)
        return new BasicDBObject('$or', [simpleClause1, simpleClause2])
    }

    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()

        def selectParams = new BasicDBObject().append(FIELD_NAME, 1).append(FIELD_NAME_2, 1)
        assert query.selectParams == selectParams
    }

    private void standardQueryAsserts(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()
        assert query.selectParams == new BasicDBObject()

    }

}
