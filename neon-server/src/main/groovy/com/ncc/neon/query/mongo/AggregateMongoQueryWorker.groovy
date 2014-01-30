package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class AggregateMongoQueryWorker extends AbstractMongoQueryWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMongoQueryWorker)
    AggregateMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        def match = new BasicDBObject('$match', mongoQuery.whereClauseParams)
        def additionalClauses = MongoAggregationClauseBuilder.buildAggregateClauses(mongoQuery.query.aggregates, mongoQuery.query.groupByClauses)
        if (mongoQuery.query.sortClauses) {
            additionalClauses << new BasicDBObject('$sort', createSortDBObject(mongoQuery.query.sortClauses))
        }
        if (mongoQuery.query.offsetClause) {
            additionalClauses << new BasicDBObject('$skip',mongoQuery.query.offsetClause.offset)
        }
        if (mongoQuery.query.limitClause) {
            additionalClauses << new BasicDBObject('$limit', mongoQuery.query.limitClause.limit)
        }
        LOGGER.debug("Executing aggregate query: {} -- {}", match, additionalClauses)
        def results = getCollection(mongoQuery).aggregate(match, additionalClauses as DBObject[]).results()
        return new MongoQueryResult(results)
    }

}
