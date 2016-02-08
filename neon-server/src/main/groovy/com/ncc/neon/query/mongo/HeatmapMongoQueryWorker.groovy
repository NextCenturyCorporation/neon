package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.result.QueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by jwilliams on 2/7/16.
 */
class HeatmapMongoQueryWorker extends AbstractMongoQueryWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMongoQueryWorker)

    protected HeatmapBoundsQuery boundingBox;

    protected final int gridCount = 10

    HeatmapMongoQueryWorker(MongoClient mongo, HeatmapBoundsQuery boundingBox) {
        super(mongo)
        this.boundingBox = boundingBox
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        def match = new BasicDBObject('$match', mongoQuery.whereClauseParams)
        def additionalClauses = buildAggregations(boundingBox)

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

        DBObject lat = new BasicDBObject('$subtract', [('$' + boundingBox.latField), minLat])
        DBObject latDiv = new BasicDBObject('$divide', [lat, boxLat])
        DBObject modLat = new BasicDBObject('$mod', [latDiv, 1])
        DBObject floorLat = new BasicDBObject('$subtract', [latDiv, modLat])
        DBObject latBox = new BasicDBObject('$multiply', [floorLat, boxLat])
        DBObject latPoint = new BasicDBObject('$add', [latBox, latModifier])

        DBObject lon = new BasicDBObject('$subtract', ['$' + boundingBox.lonField, minLon])
        DBObject lonDiv = new BasicDBObject('$divide', [lon, boxLon])
        DBObject modLon = new BasicDBObject('$mod', [lonDiv, 1])
        DBObject floorLon = new BasicDBObject('$subtract', [lonDiv, modLon])
        DBObject lonBox = new BasicDBObject('$multiply', [floorLon, boxLon])
        DBObject lonPoint = new BasicDBObject('$add', [lonBox, lonModifier])

        DBObject idField = new BasicDBObject()
        idField.put("lat", latPoint)
        idField.put("lon", lonPoint)
        DBObject groupFields = new BasicDBObject("_id", idField)
        groupFields.append("count", new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)

        return [group]
    }
}
