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

import org.bson.types.ObjectId

/**
 * Utility methods for working with mongodb data
 */
class MongoUtils {

    static final GET_FIELD_NAMES_LIMIT = 1000

    /**
     * Converts an oid object (typically created from a json mapping) to a mongo  {@link org.bson.types.ObjectId}.
     * If the oid object is already an ObjectId, it is returned as-is.
     * @param oid
     * @return
     */
    static def toObjectId(oid) {
        return oid instanceof ObjectId ? oid : new ObjectId(oid)
    }

    /**
     * Converts a collection of oid objects (typically created from json mappings) to mongo ObjectIds
     * @param oids
     * @return
     */
    static def oidsToObjectIds(oids) {
        def objectIds = []
        oids.each {
            objectIds << toObjectId(it)
        }
        return objectIds
    }

    /**
     * Returns the names of the fields from the given field name (or nested field name) that are array fields using results from the given collection.
     * @param collection
     * @param name
     * @return The set of array field names
     */
    static Set<String> getArrayFields(def collection, String name) {
        Map<String, Boolean> isArrayField = [:]
        def results = collection.find().limit(GET_FIELD_NAMES_LIMIT)
        while(results.hasNext()) {
            def result = results.next()
            name.split(/\./).each { field ->
                if(result) {
                    result = result instanceof List ? result.get("0")?.get(field) : result.get(field)
                    if(result && result instanceof List) {
                        isArrayField[field] = true
                    }
                }
            }
        }
        return isArrayField.keySet()
    }
}
