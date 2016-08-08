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

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.BasicDBObject

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile

import com.ncc.neon.userimport.types.ImportUtilities
import com.ncc.neon.userimport.types.FieldTypePair
import com.ncc.neon.userimport.types.FieldType

import org.junit.Before
import org.junit.AfterClass
import org.junit.Test
import org.junit.Assume


class MongoImportHelperTest {

    private MongoImportHelper mongoImportHelper

    private static final String HOST = System.getProperty("mongo.host")
    private static final String USERNAME = "testUsername"
    private static final String PRETTY_NAME = "testPrettyName"
    private static final String UUID = "1234"
    private static final Map FT_PAIRS_MAP = ["name": FieldType.STRING as String, "mother": [FieldType.OBJECT as String, ["name": FieldType.STRING as String, "age": FieldType.INTEGER as String]]]
    private static final List FT_PAIRS_LIST = [new FieldTypePair(
            name: "name",
            type: FieldType.STRING as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "mother",
            type: FieldType.OBJECT as String,
            objectFTPairs: [
                new FieldTypePair(
                    name: "name",
                    type: FieldType.STRING as String,
                    objectFTPairs: null
                ), new FieldTypePair(
                    name: "age",
                    type: FieldType.INTEGER as String,
                    objectFTPairs: null
                )
            ]
        )
    ]

    @Before
    void before() {
        Assume.assumeTrue(HOST != null && HOST != "")
        mongoImportHelper = new MongoImportHelper()
        mongoImportHelper.mongoImportHelperProcessor = [
            processTypeGuesses: { host, uuid ->
                MongoClient mongo = new MongoClient(host)
                GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
                GridFSDBFile dbFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
                dbFile.put("programGuesses", FT_PAIRS_MAP)
                dbFile.save()
                mongo.close()
            }, processLoadAndConvert: { host, uuid, dateFormat, fieldTypePairs ->
                MongoClient mongo = new MongoClient(host)
                GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
                GridFSDBFile dbFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
                dbFile.put("complete", true)
                if(dateFormat) {
                    dbFile.put("numCompleted", 0)
                    dbFile.put("failedFields", ["name": FieldType.STRING as String])
                } else {
                    dbFile.put("numCompleted", 1)
                    dbFile.put("failedFields", [:])
                }
                DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
                DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
                DBObject metaRecord = new BasicDBObject()
                metaRecord.append("userName", dbFile.get("userName"))
                metaRecord.append("prettyName", dbFile.get("prettyName"))
                metaRecord.append("databaseName", dbFile.get("userName") + "~" + dbFile.get("prettyName"))
                metaCollection.insert(metaRecord)
                dbFile.save()
                DBCollection collection = mongo.getDB(dbFile.get("userName") + "~" + dbFile.get("prettyName")).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
                collection.insert(new BasicDBObject())
                mongo.close()
            }, getDatabaseName: { userName, prettyName ->
                return userName + "~" + prettyName
            }
        ] as ImportHelperProcessor
    }

    @AfterClass
    static void after() {
        Assume.assumeTrue(HOST != null && HOST != "")
        MongoClient mongo = new MongoClient(HOST)
        String dbName = USERNAME + ImportUtilities.SEPARATOR + PRETTY_NAME
        List<String> dbs = mongo.getDatabaseNames()
        if(dbs.contains(dbName)) {
            DB databaseToDrop = mongo.getDB(dbName)
            if(databaseToDrop) {
                databaseToDrop.dropDatabase()
            }
        }
        if(dbs.contains(ImportUtilities.MONGO_META_DB_NAME)) {
            DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
            DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
            metaCollection.remove([userName: USERNAME, prettyName: PRETTY_NAME] as BasicDBObject)
            if(!metaCollection.getCount()) {
                metaDatabase.dropDatabase()
            }
        }
        if(dbs.contains(ImportUtilities.MONGO_UPLOAD_DB_NAME)) {
            GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
            List<GridFSDBFile> dbFiles = gridFS.find([userName: USERNAME, prettyName: PRETTY_NAME] as BasicDBObject)
            if(dbFiles.size()) {
                for(dbFile in dbFiles) {
                    gridFS.remove(dbFile)
                }
            }
        }
        mongo.close()
    }

    @Test
    void "upload file"() {
        Map result = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        assert result.jobID
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: result.jobID] as BasicDBObject)
        mongo.close()
        assert dbFile.get("identifier") == result.jobID
        assert dbFile.get("userName") == USERNAME
        assert dbFile.get("prettyName") == PRETTY_NAME
        assert dbFile.get("fileType") == "csv"
        assert dbFile.get("programGuesses") == FT_PAIRS_MAP
    }

    @Test
    void "check type guess status"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        Map result = mongoImportHelper.checkTypeGuessStatus(HOST, jobIDObj.jobID)
        assert result.complete
        assert result.guesses[0].name == FT_PAIRS_LIST[0].name
        assert result.guesses[0].type == FT_PAIRS_LIST[0].type
        assert result.guesses[0].objectFTPairs == FT_PAIRS_LIST[0].objectFTPairs
        assert result.guesses[1].name == FT_PAIRS_LIST[1].name
        assert result.guesses[1].type == FT_PAIRS_LIST[1].type
        assert result.guesses[1].objectFTPairs[0].name == FT_PAIRS_LIST[1].objectFTPairs[0].name
        assert result.guesses[1].objectFTPairs[0].type == FT_PAIRS_LIST[1].objectFTPairs[0].type
        assert result.guesses[1].objectFTPairs[0].objectFTPairs == FT_PAIRS_LIST[1].objectFTPairs[0].objectFTPairs
        assert result.guesses[1].objectFTPairs[1].name == FT_PAIRS_LIST[1].objectFTPairs[1].name
        assert result.guesses[1].objectFTPairs[1].type == FT_PAIRS_LIST[1].objectFTPairs[1].type
        assert result.guesses[1].objectFTPairs[1].objectFTPairs == FT_PAIRS_LIST[1].objectFTPairs[1].objectFTPairs
        assert result.jobID == jobIDObj.jobID
    }

    @Test
    void "load and convert fields"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        Map result = mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        assert result.jobID == jobIDObj.jobID
    }

    @Test
    void "check import status completed"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        Map result = mongoImportHelper.checkImportStatus(HOST, jobIDObj.jobID)
        assert result.complete
        assert result.numCompleted == 1
        assert result.failedFields.size() == 0
        assert result.jobID == jobIDObj.jobID
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: jobIDObj.jobID] as BasicDBObject)
        assert dbFile == null
        mongo.close()
    }

    @Test
    void "check import status completed with failed fields"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle(dateFormat: "yyyy"))
        Map result = mongoImportHelper.checkImportStatus(HOST, jobIDObj.jobID)
        assert result.complete
        assert result.numCompleted == 0
        assert result.failedFields[0].name == FT_PAIRS_LIST[0].name
        assert result.failedFields[0].type == FT_PAIRS_LIST[0].type
        assert result.failedFields[0].objectFTPairs == FT_PAIRS_LIST[0].objectFTPairs
        assert result.jobID == jobIDObj.jobID
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: jobIDObj.jobID] as BasicDBObject)
        assert dbFile
        mongo.close()
    }

    @Test
    void "check import status not completed"() {
        Map result = mongoImportHelper.checkImportStatus(HOST, UUID)
        assert !result.complete
        assert result.numCompleted == -1
        assert result.failedFields.size() == 0
        assert result.jobID == UUID
    }

    @Test
    void "drop dataset"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        Map result = mongoImportHelper.dropDataset(HOST, USERNAME, PRETTY_NAME)
        assert result.success
        assert !(mongoImportHelper.doesDatabaseExist(HOST, USERNAME, PRETTY_NAME))
    }

    @Test
    void "drop dataset failed"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        Map result = mongoImportHelper.dropDataset(HOST, USERNAME, "otherPrettyName")
        assert !result.success
    }

    @Test
    void "does database exist success"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        boolean result = mongoImportHelper.doesDatabaseExist(HOST, USERNAME, PRETTY_NAME)
        assert result
    }

    @Test
    void "does database exist failed"() {
        Map jobIDObj = mongoImportHelper.uploadFile(HOST, USERNAME, PRETTY_NAME, "csv", new ByteArrayInputStream("data".getBytes()))
        mongoImportHelper.loadAndConvertFields(HOST, jobIDObj.jobID, new UserFieldDataBundle())
        boolean result = mongoImportHelper.doesDatabaseExist(HOST, USERNAME, "otherPrettyName")
        assert !result
    }

}
