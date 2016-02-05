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
    final int gridCount = 10

    QueryResult execute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }

        def aggregations  = buildAggregations(boundingBox)
        QueryResult result = doExecute(query, options, aggregations)
        return transform(query.transforms, result)
    }

    def buildAggregations(HeatmapBoundsQuery boundingBox) {
        float minLon = boundingBox.minLon
        float minLat = boundingBox.minLat
        float maxLon = boundingBox.maxLon
        float maxLat = boundingBox.maxLat

        float latRange = maxLat - minLat
        float boxLat = (latRange / gridCount)
        float latModifier = (boxLat / 2)

        float lonRange = maxLon - minLon
        float boxLon = (lonRange / gridCount)
        float lonModifier = (boxLon / 2)

        println("======")
        println(boundingBox.latField)
        println("======")


        DBObject lat = new BasicDBObject('$subtract', [("\$${boundingBox.latField}"), minLat])
        DBObject latDiv = new BasicDBObject('$divide', [lat, boxLat])
        DBObject modLat = new BasicDBObject('$mod', [latDiv, 1])
        DBObject floorLat = new BasicDBObject('$subtract', [latDiv, modLat])
        DBObject latBox = new BasicDBObject('$multiply', [floorLat, boxLat])
        DBObject latPoint = new BasicDBObject('$add', [latBox, latModifier])

        DBObject lon = new BasicDBObject('$subtract', ["\$${boundingBox.lonField}", minLon])
        DBObject lonDiv = new BasicDBObject('$divide', [lon, boxLon])
        DBObject modLon = new BasicDBObject('$mod', [lonDiv, 1])
        DBObject floorLon = new BasicDBObject('$subtract', [lonDiv, modLon])
        DBObject lonBox = new BasicDBObject('$multiply', [floorLon, boxLon])
        DBObject lonPoint = new BasicDBObject('$add', [lonBox, lonModifier])

        DBObject idField = new BasicDBObject()
        idField.put("lat", '$lon')
        idField.put("lon", '$lon')
        DBObject groupFields = new BasicDBObject("_id", idField)
        groupFields.append("count", new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)

        DBObject fields = new BasicDBObject()
        fields.put("lat", latPoint)
        fields.put("lon", lonPoint)
        DBObject project = new BasicDBObject('$project', fields)

        return [
            aggregation: group,
            project: project
        ]
    }

    QueryResult doExecute(Query query, QueryOptions options, def aggregations) {
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)

        println(JsonOutput.toJson(aggregations))
        println(JsonOutput.toJson(mongoQuery))
        //FIXME inject aggregations
        //FIXME inject projection

        return getQueryResult(worker, mongoQuery)
    }

}
