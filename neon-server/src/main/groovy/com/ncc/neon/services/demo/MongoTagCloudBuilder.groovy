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

import com.mongodb.DB
import com.mongodb.DBObject
import groovy.transform.CompileStatic
import org.jongo.Aggregate
import org.jongo.Jongo
import org.jongo.MongoCollection
import org.jongo.ResultHandler

/**
 * Builds a tag/frequency count from array fields in a mongo database
 */
@CompileStatic
class MongoTagCloudBuilder {

    // TODO: Extract methods - this class is a mess

    private MongoTagCloudBuilder() {}

    // TODO: Convert this to use the mongo java driver and then factor in other neon filters/selection

    @SuppressWarnings("MethodSize") // ignore since Jongo requires us to break the query into some longer parts. that makes the method longer, but it's still straight forward@SuppressWarnings("MethodSize") // ignore since Jongo requires us to break the query into some longer parts (and this is a demo class). that makes the method longer, but it's still straight forward
    @SuppressWarnings("ExplicitCallToAndMethod")  // codenarc falsely reports this when using the "and" method in Jongo
    static Map<String, Integer> getTagCounts(DB database, String collectionName, String arrayField, int limit) {
        Jongo jongo = new Jongo(database)
        MongoCollection collection = jongo.getCollection(collectionName)

        // TODO: Add a match on the other neon filters

        // get only the array field since that's all we need for the tag cloud
        String arrayProjection = '{$project:{"' + arrayField + '":1}}'

        // unwind the array to aggregate on the individual entries
        String unwind = '{$unwind:"$' + arrayField + '"}'

        // count the tags
        String group = '{"$group": {"_id": "$' + arrayField + '", "count": {"$sum": 1}}}'

        // highest count first
        String sort = '{"$sort":{"count":-1}}'

        // the query is already sorted, so use a linkedhashmap instead of a sorted map since the insertion order will
        // be sorted
        Map<String, Integer> tagCounts = [:]
        Aggregate aggregate = collection.aggregate(arrayProjection).and(unwind).and(group).and(sort)
        if (limit > 0) {
            aggregate = aggregate.and('{"$limit":' + limit + '}')
        }
        aggregate.map(new ResultHandler<Void>() {
            @Override
            public Void map(DBObject result) {
                tagCounts[(String) result.get("_id")] = ((Number) result.get("count")).intValue()
                return null
            }
        })
        return tagCounts

    }

}
