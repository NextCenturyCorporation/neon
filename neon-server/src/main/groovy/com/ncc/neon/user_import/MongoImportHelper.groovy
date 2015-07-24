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

package com.ncc.neon.user_import

import org.apache.commons.io.IOUtils
import org.apache.commons.io.LineIterator
import org.springframework.stereotype.Component

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFCell

import com.monitorjbl.xlsx.StreamingReader
import com.monitorjbl.xlsx.exceptions.MissingSheetException

import javax.annotation.Resource

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
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

    @Resource
    MongoImportHelperProcessor mongoImportHelperProcessor
    
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
        List guesses = fieldGuessesToList(dbFile.get("programGuesses"))
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
            return [complete: false, numFinished: -1, failed: [], jobID: uuid] // If the GridFS file doesn't exist, return a numFinished value of -1.
        }
        boolean complete = dbFile.get("complete")
        int numFinished = mongo.getDB(ImportUtilities.makeUglyName(dbFile.get("userName"),
            dbFile.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME).count()
        List failedFields = dbFile.get("failedFields")
        if(complete) {
            gridFS.remove(dbFile)
        }
        mongo.close()
        return [complete: complete, numFinished: numFinished, failedFields: failedFields, jobID: uuid]
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

    // Helper method, converts a map of field names to type guesses into a list of FieldTypePairs.
    private List fieldGuessesToList(Map fieldGuesses) {
        List keys = fieldGuesses.keySet() as List
        List toReturn = []
        keys.each { key ->
            toReturn.add(new FieldTypePair(name: key, type: fieldGuesses.get(key)))
        }
        return toReturn
    }

    // Helper method, gets a user-given database by identifier (this would be ugly name, for the moment).
    private DB getDatabase(MongoClient mongo, Map identifier) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = metaCollection.findOne(identifier as BasicDBObject)
        return mongo.getDB(metaRecord.get("databaseName"))
    }
}