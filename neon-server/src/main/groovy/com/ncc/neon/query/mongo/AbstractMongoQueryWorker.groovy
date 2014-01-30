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
        // TODO: NEON-900 and NEON-939 require us to make this fix in here since the metadata connection is always attempted, but we can move it after those issues are fixed
        // this is done in the query worker as opposed to the connection client because there may be cases in
        // the connection client (currently in the metadata) where we don't want to throw an error, rather we do
        // want to create the empty database
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
