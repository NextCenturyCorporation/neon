package com.ncc.neon.query.convert
import com.mongodb.BasicDBObject
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.mongo.MongoConversionStrategy


/*
 Tests the MongoConversionStrategy.convertQuery()
 correctly converts Query objects into MongoQuery objects
*/
class MongoConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def convertQuery(query) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.ALL_DATA)
    }

    // For many of these query clause tests, the conversion strategy does not need to do anything so it just asserts
    // the "standard query asserts." This is because the mongo query worker classes handle the actual clauses and
    // attach them to the dbcursors.

    @Override
    void assertSimplestConvertQuery(query) {
        standardQueryAsserts(query)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        standardQueryAsserts(query)
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
    protected void assertQueryWithOrWhereClause(query) {
        assert query.query == simpleQuery
        BasicDBObject orClause = createOrClause()

        assert query.whereClauseParams == orClause
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        standardQueryAsserts(query)
    }

    protected BasicDBObject createOrClause() {
        BasicDBObject simpleClause1 = new BasicDBObject(FIELD_NAME, COLUMN_VALUE)
        BasicDBObject simpleClause2 = new BasicDBObject(FIELD_NAME_2, COLUMN_VALUE)
        return new BasicDBObject('$or', [simpleClause1, simpleClause2])
    }

    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()

        def selectParams = new BasicDBObject().append(FIELD_NAME, 1).append(FIELD_NAME_2, 1)
        assert query.selectParams == selectParams
    }

    private void standardQueryAsserts(query){
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()
        assert query.selectParams == new BasicDBObject()

    }

}
