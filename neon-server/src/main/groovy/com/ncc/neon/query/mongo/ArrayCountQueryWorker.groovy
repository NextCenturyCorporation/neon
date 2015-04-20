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
package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.result.QueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ArrayCountQueryWorker extends AbstractMongoQueryWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateMongoQueryWorker)
    ArrayCountQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        DBObject project = new BasicDBObject('$project', new BasicDBObject(arrayField, 1))
        mongoQuery.query.aggregates << project

        DBObject unwind = new BasicDBObject('$unwind', '$' + arrayField)
        mongoQuery.query.aggregates << unwind

    }

    /****
        MongoQuery ===
        Query query >> {
            Filter filter
            boolean isDistinct = false
            List<String> fields = SelectClause.ALL_FIELDS
            List<AggregateClause> aggregates = []
            List<GroupByClause> groupByClauses = []
            List<SortClause> sortClauses = []
            LimitClause limitClause
            OffsetClause offsetClause
            Transform[] transforms
        }
        DBObject whereClauseParams
        DBObject selectParams
    ****/


    /*
        List<BasicDBObject> aggregationOps = []

        DBObject matchQuery = mongoNeonHelper.mergeWithNeonFilters(new BasicDBObject(), database.name, collectionName)

        if ( !((BasicDBObject)matchQuery).isEmpty()) {
            DBObject match = new BasicDBObject('$match',matchQuery)
            aggregationOps << match
        }
    */

    /*





        DBObject groupFields = new BasicDBObject()
        groupFields.put('_id', '$' + arrayField)
        groupFields.put('count', new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)
        aggregationOps << group

        DBObject sort = new BasicDBObject('$sort', new BasicDBObject('count', -1))
        aggregationOps << sort

        if (limit > 0) {
            aggregationOps << new BasicDBObject('$limit', limit)
        }

        // the query is already sorted, so use a linkedhashmap instead of a sorted map since the insertion order will
        // be sorted
        Map<String, Integer> counts = [:]

        Iterator<DBObject> results = database.getCollection(collectionName).aggregate(aggregationOps[0],
                aggregationOps[1..aggregationOps.size()-1].toArray(new DBObject[0])).results().iterator()
        while (results.hasNext()) {
            DBObject row = results.next()
            String key = (String) row.get('_id')
            int count = ((Number) row.get('count')).intValue()
            counts[key] = count
        }

        List<ArrayCountPair> countList = []
        counts.each { key, count ->
            countList << new ArrayCountPair(key: key, count: count)
        }
    */
}