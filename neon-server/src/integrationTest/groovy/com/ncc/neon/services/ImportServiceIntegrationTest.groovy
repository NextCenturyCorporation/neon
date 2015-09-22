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

package com.ncc.neon.services

import com.ncc.neon.IntegrationTestContext

import com.ncc.neon.connect.NeonConnectionException

import org.json.JSONArray
import org.json.JSONObject

import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.After

import org.junit.runner.RunWith

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.springframework.beans.factory.annotation.Autowired

import javax.ws.rs.core.Response

import com.ncc.neon.userimport.ImportHelperFactory
import com.ncc.neon.userimport.UserFieldDataBundle

import com.ncc.neon.userimport.types.FieldTypePair
import com.ncc.neon.userimport.types.FieldType
import com.ncc.neon.userimport.types.ImportUtilities

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.BasicDBObject

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile

/**
 * Integration test that verifies the neon server properly imports data into mongo
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class ImportServiceIntegrationTest {

    private ImportService importService

    private static final String HOST = System.getProperty("mongo.host")
    private static final String DATABASE_TYPE = "mongo"
    private static final String USERNAME = "integrationTesting"
    private static final String PRETTY_NAME = "test"
    private static final List FT_PAIRS_LIST1 = [
        new FieldTypePair(
            name: "name",
            type: FieldType.STRING as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "age",
            type: FieldType.DOUBLE as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "address",
            type: FieldType.OBJECT as String,
            objectFTPairs: [
                new FieldTypePair(
                    name: "city",
                    type: FieldType.STRING as String,
                    objectFTPairs: null
                ),
                new FieldTypePair(
                    name: "zip",
                    type: FieldType.INTEGER as String,
                    objectFTPairs: null
                )
            ]
        )
    ]
    private static final List FT_PAIRS_LIST2 = [
        new FieldTypePair(
            name: "name",
            type: FieldType.STRING as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "age",
            type: FieldType.OBJECT as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "address",
            type: FieldType.OBJECT as String,
            objectFTPairs: [
                new FieldTypePair(
                    name: "city",
                    type: FieldType.STRING as String,
                    objectFTPairs: null
                ),
                new FieldTypePair(
                    name: "zip",
                    type: FieldType.DATE as String,
                    objectFTPairs: null
                )
            ]
        )
    ]
    private static final List FT_PAIRS_LIST2_FAILED_FIELDS = [
        new FieldTypePair(
            name: "age",
            type: FieldType.OBJECT as String,
            objectFTPairs: []
        ), new FieldTypePair(
            name: "address",
            type: FieldType.OBJECT as String,
            objectFTPairs: [
                new FieldTypePair(
                    name: "zip",
                    type: FieldType.DATE as String,
                    objectFTPairs: []
                )
            ]
        )
    ]
    private static final List FT_PAIRS_LIST3 = [
        new FieldTypePair(
            name: "age",
            type: FieldType.INTEGER as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "address",
            type: FieldType.OBJECT as String,
            objectFTPairs: [
                new FieldTypePair(
                    name: "zip",
                    type: FieldType.INTEGER as String,
                    objectFTPairs: null
                )
            ]
        )
    ]

    private ImportHelperFactory importHelperFactory

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setImportHelperFactory(ImportHelperFactory importHelperFactory) {
        this.importHelperFactory = importHelperFactory
    }

    @Before
    void before() {
        // Establish the connection, or skip the tests if no host was specified
        Assume.assumeTrue(HOST != null && HOST != "")
        importService = new ImportService()
        importService.importEnabled = "true"
        importService.importHelperFactory = this.importHelperFactory
    }

    @After
    void after() {
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
            GridFSDBFile dbFile = gridFS.findOne([userName: USERNAME, prettyName: PRETTY_NAME] as BasicDBObject)
            if(dbFile) {
                gridFS.remove(dbFile)
            }
        }
        mongo.close()
    }

    @Test(expected=NeonConnectionException)
    void "upload file to unsupported database type"() {
        importService.uploadFile(HOST, "sparksql", USERNAME, PRETTY_NAME, "xlsx", new FileInputStream("src/test-data/excel-files/test-file.xlsx"))
    }

    @Test
    void "upload file to existing database"() {
        String uuid = uploadFile("xlsx", new FileInputStream("src/test-data/excel-files/test-file-with-objects.xlsx"))
        importExcelFile(uuid, FT_PAIRS_LIST1)

        Response response = importService.uploadFile(HOST, DATABASE_TYPE, USERNAME, PRETTY_NAME, "xlsx", new FileInputStream("src/test-data/excel-files/test-file.xlsx"))
        assert response.getStatus() == 406
    }

    @Test
    void "upload file success"() {
        Response response = importService.uploadFile(HOST, DATABASE_TYPE, USERNAME, PRETTY_NAME, "xlsx", new FileInputStream("src/test-data/excel-files/test-file.xlsx"))
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        String uuid = entity.getString("jobID")
        assert uuid
        GridFSDBFile dbFile = getDBFile(uuid)

        assert dbFile.get("userName") == USERNAME
        assert dbFile.get("prettyName") == PRETTY_NAME
        assert dbFile.get("fileType") == "xlsx"
    }

    @Test
    void "check type guess status"() {
        Response response = importService.uploadFile(HOST, DATABASE_TYPE, USERNAME, PRETTY_NAME, "xlsx", new FileInputStream("src/test-data/excel-files/test-file.xlsx"))
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        String uuid = entity.getString("jobID")

        response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, uuid)
        assert response.getStatus() == 200
        entity = new JSONObject(response.getEntity())
        boolean complete = entity.getBoolean("complete")

        while(!complete) {
            Thread.sleep(1000L)
            assert !entity.getJSONArray("guesses")
            assert entity.getString("jobID") == uuid
            response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, uuid)
            assert response.getStatus() == 200
            entity = new JSONObject(response.getEntity())
            complete = entity.getBoolean("complete")
        }

        assert entity.getString("jobID") == uuid
        checkJSONArray(entity.getJSONArray("guesses"), [new FieldTypePair(
                    name: "name",
                    type: FieldType.STRING as String,
                    objectFTPairs: null
                ), new FieldTypePair(
                    name: "mother",
                    type: FieldType.STRING as String,
                    objectFTPairs: null
                )
            ]
        )
    }

    @Test
    void "load and convert fields from xlsx file"() {
        String uuid = uploadFile("xlsx", new FileInputStream("src/test-data/excel-files/test-file-with-objects.xlsx"))
        JSONObject entity = importExcelFile(uuid, FT_PAIRS_LIST1)

        assert entity.getInt("numCompleted") == 2
        assert entity.getJSONArray("failedFields").length() == 0

        DBObject record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Double
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  Integer
    }

    @Test
    void "load and convert fields when failed from xlsx file"() {
        String uuid = uploadFile("xlsx", new FileInputStream("src/test-data/excel-files/test-file-with-objects.xlsx"))
        JSONObject entity = importExcelFile(uuid, FT_PAIRS_LIST2)

        assert entity.getInt("numCompleted") == 2
        checkJSONArray(entity.getJSONArray("failedFields"), FT_PAIRS_LIST2_FAILED_FIELDS)

        DBObject record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  String
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  String

        entity = importExcelFile(uuid, FT_PAIRS_LIST3)

        assert entity.getInt("numCompleted") == 2
        assert entity.getJSONArray("failedFields").length() == 0

        record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Integer
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  Integer
    }

    @Test
    void "load and convert fields from csv file"() {
        String uuid = uploadFile("csv", new StringBufferInputStream("name,age,address\nJoe,23,\"[city: 'Baltimore', zip: 12345]\"\nJanice,46,\"[city: 'Annapolis', zip: 54321]\""))
        JSONObject entity = importExcelFile(uuid, FT_PAIRS_LIST1)

        assert entity.getInt("numCompleted") == 2
        assert entity.getJSONArray("failedFields").length() == 0

        DBObject record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Double
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  Integer
    }

    @Test
    void "load and convert fields when failed from csv file"() {
        String uuid = uploadFile("csv", new StringBufferInputStream("name,age,address\nJoe,23,\"[city: 'Baltimore', zip: 12345]\"\nJanice,46,\"[city: 'Annapolis', zip: 54321]\""))
        JSONObject entity = importExcelFile(uuid, FT_PAIRS_LIST2)

        assert entity.getInt("numCompleted") == 2
        checkJSONArray(entity.getJSONArray("failedFields"), FT_PAIRS_LIST2_FAILED_FIELDS)

        DBObject record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  String
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  String

        entity = importExcelFile(uuid, FT_PAIRS_LIST3)

        assert entity.getInt("numCompleted") == 2
        assert entity.getJSONArray("failedFields").length() == 0

        record = getRecord()
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Integer
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  Integer
    }

    @Test
    void "drop unknown dataset"() {
        Response response = importService.dropDataset(HOST, "mongo", USERNAME, PRETTY_NAME)
        assert response.getStatus() == 404
    }

    @Test
    void "drop dataset"() {
        String uuid = uploadFile("xlsx", new FileInputStream("src/test-data/excel-files/test-file-with-objects.xlsx"))
        importExcelFile(uuid, FT_PAIRS_LIST1)

        Response response = importService.dropDataset(HOST, "mongo", USERNAME, PRETTY_NAME)
        assert response.getStatus() == 200
        JSONObject entity = new JSONObject(response.getEntity())
        assert entity.getBoolean("success")

        response = importService.dropDataset(HOST, "mongo", USERNAME, PRETTY_NAME)
        assert response.getStatus() == 404
    }

    @Test
    void "import not enabled"() {
        importService.importEnabled = "false"

        Response response = importService.uploadFile(HOST, "mongo", USERNAME, PRETTY_NAME, "xlsx", null)
        assert response.getStatus() == 403

        response = importService.checkTypeGuessStatus(HOST, "mongo", "1234")
        assert response.getStatus() == 403

        response = importService.loadAndConvertFields(HOST, "mongo", "1234", null)
        assert response.getStatus() == 403

        response = importService.checkImportStatus(HOST, "mongo", "1234")
        assert response.getStatus() == 403

        response = importService.dropDataset(HOST, "mongo", USERNAME, PRETTY_NAME)
        assert response.getStatus() == 403
    }

    private GridFSDBFile getDBFile(String uuid) {
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        mongo.close()
        return dbFile
    }

    private DBObject getRecord() {
        MongoClient mongo = new MongoClient(HOST)
        DB metaDatabase = mongo.getDB(USERNAME + ImportUtilities.SEPARATOR + PRETTY_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        DBObject record = metaCollection.findOne()
        mongo.close()
        return record
    }

    private String uploadFile(String fileType, InputStream file) {
        Response response = importService.uploadFile(HOST, DATABASE_TYPE, USERNAME, PRETTY_NAME, fileType, file)
        JSONObject entity = new JSONObject(response.getEntity())
        assert response.getStatus() == 200
        String uuid = entity.getString("jobID")

        response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, uuid)
        assert response.getStatus() == 200
        entity = new JSONObject(response.getEntity())
        boolean complete = entity.getBoolean("complete")

        while(!complete) {
            Thread.sleep(1000L)
            response = importService.checkTypeGuessStatus(HOST, DATABASE_TYPE, uuid)
            assert response.getStatus() == 200
            entity = new JSONObject(response.getEntity())
            complete = entity.getBoolean("complete")
        }

        return uuid
    }

    private JSONObject importExcelFile(String uuid, List fields) {
        Response response = importService.loadAndConvertFields(HOST, DATABASE_TYPE, uuid, new UserFieldDataBundle(
            fields: fields
        ))
        assert response.getStatus() == 200
        JSONObject entity = new JSONObject(response.getEntity())
        assert entity.getString("jobID") == uuid

        response = importService.checkImportStatus(HOST, DATABASE_TYPE, uuid)
        assert response.getStatus() == 200
        entity = new JSONObject(response.getEntity())
        boolean complete = entity.getBoolean("complete")
        int numCompleted = entity.getInt("numCompleted")
        assert entity.getString("jobID") == uuid
        assert entity.getJSONArray("failedFields")

        while(!complete && numCompleted >= 0) {
            Thread.sleep(1000L)
            response = importService.checkImportStatus(HOST, DATABASE_TYPE, uuid)
            assert response.getStatus() == 200
            entity = new JSONObject(response.getEntity())
            complete = entity.getBoolean("complete")
            numCompleted = entity.getInt("numCompleted")
            assert entity.getString("jobID") == uuid
            assert entity.getJSONArray("failedFields")
        }

        return entity
    }

    private void checkJSONArray(JSONArray arrayToCheck, List checkAgainst) {
        assert arrayToCheck.length() == checkAgainst.size()

        for(int i = 0; i < arrayToCheck.length(); i++) {
            assert arrayToCheck.get(i).name == checkAgainst[i].name
            assert arrayToCheck.get(i).type == (checkAgainst[i].type as String)

            if(arrayToCheck.get(i).isNull("objectFTPairs") || arrayToCheck.get(i).objectFTPairs.length() == 0) {
                assert (!checkAgainst[i].objectFTPairs || (checkAgainst[i].objectFTPairs.size() == 0))
            } else {
                checkJSONArray(arrayToCheck.get(i).getJSONArray("objectFTPairs"), checkAgainst[i].objectFTPairs)
            }
        }
    }

}
