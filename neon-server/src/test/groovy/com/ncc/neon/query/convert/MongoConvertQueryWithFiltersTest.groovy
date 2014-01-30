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
