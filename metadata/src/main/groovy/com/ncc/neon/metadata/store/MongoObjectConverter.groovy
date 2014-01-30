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
