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
import com.mongodb.DBCollection
import com.mongodb.MongoClient
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.QueryResult



/**
 * Mongo has different operations for distinct, aggregate, and find. Subclasses
 * perform these different operations
 */

abstract class AbstractMongoQueryWorker {

    private final MongoClient mongo

    protected AbstractMongoQueryWorker(MongoClient mongo) {
        this.mongo = mongo
        preventEmptyDBCreation()
    }

    abstract QueryResult executeQuery(MongoQuery query)

    protected DBCollection getCollection(MongoQuery mongoQuery) {
        def db = mongo.getDB(mongoQuery.query.databaseName)
        return db.getCollection(mongoQuery.query.tableName)
    }

    protected static def createSortDBObject(sortClauses) {
        def sortDBObject = new BasicDBObject()
        sortClauses.each {
            sortDBObject.append(it.fieldName, it.sortDirection)
        }
        return sortDBObject
    }

    private void preventEmptyDBCreation() {
        this.mongo.metaClass.getDB = { String dbName ->
            if (!delegate.databaseNames.contains(dbName)) {
                throw new NeonConnectionException("Database ${dbName} does not exist")
            }
            return delegate.&getDB(dbName)
        }
    }

}
