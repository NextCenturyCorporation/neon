package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions

import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import groovy.json.JsonOutput
import org.springframework.stereotype.Component

/**
 * Created by jwilliams on 1/27/16.
 */
@Component
class MongoHeatmapExecutor extends MongoQueryExecutor{
    QueryResult execute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }

        QueryResult result = doExecute(query, options, boundingBox)
        return result
    }

    QueryResult doExecute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        AbstractMongoQueryWorker worker = new HeatmapMongoQueryWorker(mongo, boundingBox)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)

        return getQueryResult(worker, mongoQuery)
    }
}