package com.ncc.neon.metadata.store

import com.mongodb.BasicDBObject
import com.mongodb.DBObject



/**
 * Turns a groovy object into a mongo DbObject and visa-versa.
 */

class MongoObjectConverter {

    private static final String CLASS = "class"

    BasicDBObject convertToMongo(def obj) {
        BasicDBObject document = new BasicDBObject()
        obj.metaClass.properties.each {
            def value = obj[it.name]
            if (it.name == CLASS) {
                value = obj.class.name
            }
            document.append(it.name, value)
        }
        return document
    }

    def convertToObject(DBObject dbObject) {
        def object = MongoObjectConverter.classLoader.loadClass(dbObject.get(CLASS)).newInstance()

        dbObject.each { k, v ->
            if (k == CLASS || k == "_id") {
                return
            }
            object[k] = v
        }
        return object
    }

}
