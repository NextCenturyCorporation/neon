/*
 * Copyright 2014 Next Century Corporation
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

package com.ncc.neon.services.demo

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBObject
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Builds a tag/frequency count from array fields in a mongo database
 */
@CompileStatic
@Component
class MongoTagCloudBuilder {

    private MongoTagCloudBuilder() {}

    // TODO: Add a match on the other neon filters

    @Autowired
    private MongoNeonHelper mongoNeonHelper


    @SuppressWarnings("MethodSize")
    Map<String, Integer> getTagCounts(DB database, String collectionName, String arrayField, int limit) {
        List<BasicDBObject> aggregationOps = []

        DBObject matchQuery = mongoNeonHelper.mergeWithNeonFilters(new BasicDBObject(), database.name, collectionName)
        if ( !((BasicDBObject)matchQuery).isEmpty()) {
            DBObject match = new BasicDBObject('$match',matchQuery)
            aggregationOps << match
        }

        DBObject project = new BasicDBObject('$project', new BasicDBObject(arrayField, 1))
        aggregationOps << project

        DBObject unwind = new BasicDBObject('$unwind', '$' + arrayField)
        aggregationOps << unwind

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
        Map<String, Integer> tagCounts = [:]


        Iterator<DBObject> results = database.getCollection(collectionName).aggregate(aggregationOps[0],
                aggregationOps[1..aggregationOps.size()-1].toArray(new DBObject[0])).results().iterator()
        while (results.hasNext()) {
            DBObject row = results.next()
            String tag = (String) row.get('_id')
            int count = ((Number) row.get('count')).intValue()
            tagCounts[tag] = count
        }

        return tagCounts

    }

}
