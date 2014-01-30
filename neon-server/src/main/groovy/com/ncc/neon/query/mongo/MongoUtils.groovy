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
