/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.query.mongo

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient

import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HeatmapMongoQueryWorker extends AbstractMongoQueryWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeatmapMongoQueryWorker)

    protected HeatmapBoundsQuery boundingBox

    HeatmapMongoQueryWorker(MongoClient mongo, HeatmapBoundsQuery boundingBox) {
        super(mongo)
        this.boundingBox = boundingBox
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        DBObject andList = new BasicDBList()
        andList.add(mongoQuery.whereClauseParams)
        andList.add(buildDefinedClause())
        andList.add(buildBoundsClause())
        BasicDBObject whereAnd = new BasicDBObject('$and', andList)

        def match = new BasicDBObject('$match', whereAnd)
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

        results = extractHeatmapBuckets(results.collect { it.toMap() } as List)

        return new TabularQueryResult(results)
    }

    private List<Map<String, Object>> extractHeatmapBuckets(List<Map<String, Object>> data) {
        def maxCount = 0
        data.each {
            if(it.count > maxCount) {
                maxCount = it.count
            }
        }

        List<Map<String, Object>> results = []

        data.each{
            def bucket = [:]
            def point = [:]
            point['lat'] = it['_id'].lat
            point['lon'] = it['_id'].lon
            bucket['point'] = point
            bucket['count'] = it.count
            bucket['percentage'] = maxCount > 0 ? (it.count / maxCount) : 0

            results.add(bucket)
        }

        return results
    }

    private DBObject buildDefinedClause() {
        DBObject notNull = new BasicDBObject('$ne', null)
        DBObject notEmptyString = new BasicDBObject('$ne', "")

        DBObject latNotNull = new BasicDBObject(boundingBox.latField, notNull)
        DBObject latNotEmptyString = new BasicDBObject(boundingBox.latField, notEmptyString)
        DBObject lonNotNull = new BasicDBObject(boundingBox.lonField, notNull)
        DBObject lonNotEmptyString = new BasicDBObject(boundingBox.latField, notEmptyString)

        DBObject defined = new BasicDBList()
        defined.add(latNotNull)
        defined.add(latNotEmptyString)
        defined.add(lonNotNull)
        defined.add(lonNotEmptyString)

        return new BasicDBObject('$and', defined)
    }

    private DBObject buildBoundsClause() {
        DBObject latMin = new BasicDBObject(boundingBox.latField, new BasicDBObject('$gte', boundingBox.minLat))
        DBObject latMax = new BasicDBObject(boundingBox.latField, new BasicDBObject('$lte', boundingBox.maxLat))
        DBObject lonMin = new BasicDBObject(boundingBox.lonField, new BasicDBObject('$gte', boundingBox.minLon))
        DBObject lonMax = new BasicDBObject(boundingBox.lonField, new BasicDBObject('$lte', boundingBox.maxLon))
        DBObject bounds = new BasicDBList()
        bounds.add(latMin)
        bounds.add(latMax)
        bounds.add(lonMin)
        bounds.add(lonMax)

        return new BasicDBObject('$and', bounds)
    }

    def buildAggregations(HeatmapBoundsQuery boundingBox) {
        float minLon = boundingBox.minLon
        float minLat = boundingBox.minLat
        float maxLon = boundingBox.maxLon
        float maxLat = boundingBox.maxLat

        float latRange = maxLat - minLat
        float boxLat = (latRange / boundingBox.gridCount)
        float latModifier = (boxLat / 2)

        float lonRange = maxLon - minLon
        float boxLon = (lonRange / boundingBox.gridCount)
        float lonModifier = (boxLon / 2)

        DBObject lat = new BasicDBObject('$subtract', [('$' + boundingBox.latField), minLat])
        DBObject latDiv = new BasicDBObject('$divide', [lat, boxLat])
        DBObject modLat = new BasicDBObject('$mod', [latDiv, 1])
        DBObject floorLat = new BasicDBObject('$subtract', [latDiv, modLat])
        DBObject latBox = new BasicDBObject('$multiply', [floorLat, boxLat])
        DBObject latPoint = new BasicDBObject('$add', [new BasicDBObject('$add', [latBox, minLat]), latModifier])

        DBObject lon = new BasicDBObject('$subtract', ['$' + boundingBox.lonField, minLon])
        DBObject lonDiv = new BasicDBObject('$divide', [lon, boxLon])
        DBObject modLon = new BasicDBObject('$mod', [lonDiv, 1])
        DBObject floorLon = new BasicDBObject('$subtract', [lonDiv, modLon])
        DBObject lonBox = new BasicDBObject('$multiply', [floorLon, boxLon])
        DBObject lonPoint = new BasicDBObject('$add', [new BasicDBObject('$add', [lonBox, minLon]), lonModifier])

        DBObject idField = new BasicDBObject()
        idField.put("lat", latPoint)
        idField.put("lon", lonPoint)
        DBObject groupFields = new BasicDBObject("_id", idField)
        groupFields.append("count", new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)

        return [group]
    }
}
