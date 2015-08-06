/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.userimport

import org.springframework.stereotype.Component

import org.springframework.beans.factory.annotation.Autowired

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.BasicDBObject

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSInputFile
import com.mongodb.gridfs.GridFSDBFile

/**
 * Implements methods to add, remove, and convert fields of records in a mongo database.
 */
@Component
class MongoImportHelper implements ImportHelper {

    @Autowired
    private ImportHelperProcessor mongoImportHelperProcessor

    @Override
    Map uploadFile(String host, String userName, String prettyName, String fileType, InputStream stream) {
        String id = UUID.randomUUID().toString()
        MongoClient mongo = new MongoClient(host, 27017)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSInputFile inFile = gridFS.createFile(stream)
        inFile.put("identifier", id)
        inFile.put("userName", userName)
        inFile.put("prettyName", prettyName)
        inFile.put("fileType", fileType)
        inFile.save()
        mongo.close()
        mongoImportHelperProcessor.processTypeGuesses(host, id)
        return [jobID: id]
    }

    @Override
    Map checkTypeGuessStatus(String host, String uuid) {
        MongoClient mongo = new MongoClient(host, 27017)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        List guesses = fieldTypePairMapToList(dbFile.get("programGuesses"))
        mongo.close()
        return (guesses) ? [complete: true, guesses: guesses, jobID: uuid] : [complete: false, guesses: null, jobID: uuid]
    }

    @Override
    Map loadAndConvertFields(String host, String uuid, UserFieldDataBundle bundle) {
        mongoImportHelperProcessor.processLoadAndConvert(host, uuid, bundle.format, bundle.fields)
        return [jobID: uuid]
    }

    @Override
    Map checkImportStatus(String host, String uuid) {
        MongoClient mongo = new MongoClient(host, 27017)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        if(dbFile == null) {
            return [complete: false, numCompleted: -1, failedFields: [], jobID: uuid] // If the GridFS file doesn't exist, return a numCompleted value of -1.
        }
        boolean complete = dbFile.get("complete")
        int numCompleted = dbFile.get("numCompleted") ?: 0
        List failedFields = fieldTypePairMapToList(dbFile.get("failedFields"))
        if(complete && !failedFields) {
            gridFS.remove(dbFile)
        }
        mongo.close()
        return [complete: complete, numCompleted: numCompleted, failedFields: failedFields, jobID: uuid]
    }

    @Override
    Map dropDataset(String host, String userName, String prettyName) {
        MongoClient mongo = new MongoClient(host, 27017)
        DB databaseToDrop = getDatabase(mongo, [userName: userName, prettyName: prettyName])
        if(databaseToDrop.getCollectionNames()) {
            databaseToDrop.dropDatabase()
        }
        else {
            return [success: false]
        }
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        metaCollection.remove([userName: userName, prettyName: prettyName] as BasicDBObject)
        if(!metaCollection.getCount()) {
            metaDatabase.dropDatabase()
        }
        mongo.close()
        return [success: true]
    }

    /**
     * Helper method that converts a map from field names to types guesses into a list of FieldTypePairs. This
     * is necessary because mongo doesn't know how to deserialize FieldTypePairs for storage, and so guesses
     * have to be converted into a map from field name to field type in order to be stored.
     * @param fieldGuesses A map, with field names as keys and field types as their corresponding values.
     * @return A list of FieldTypePairs generated from the given map of field names to field types.
     */
    private List fieldTypePairMapToList(Map fieldGuesses) {
        List keys = fieldGuesses?.keySet() as List
        List toReturn = []
        keys.each { key ->
            toReturn.add(new FieldTypePair(name: key, type: fieldGuesses.get(key)))
        }
        return toReturn
    }

    /**
     * Helper method that takes a MongoClient instance and an identifier map of the form [identifier: (String)],
     * where the string value is the identifier associated with the database to find, and returns the DB associated
     * with the identifier value on the given instance of mongo.
     * @param mongo The instance of mongo on which to search for the database associated with the given identifier.
     * @param identifier A map of the form [identifier: (String)] that can be used to find the desired database.
     *@return the DB for the database associated with the given identifier value.
     */
    private DB getDatabase(MongoClient mongo, Map identifier) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = metaCollection.findOne(identifier as BasicDBObject)
        return mongo.getDB(metaRecord.get("databaseName"))
    }
}