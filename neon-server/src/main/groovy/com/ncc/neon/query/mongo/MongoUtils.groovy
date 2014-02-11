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

import org.bson.types.ObjectId


/**
 * Utility methods for working with mongodb data
 */
class MongoUtils {

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

}
