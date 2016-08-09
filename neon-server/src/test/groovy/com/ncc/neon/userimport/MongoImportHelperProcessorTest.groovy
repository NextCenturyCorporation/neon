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
import com.mongodb.gridfs.GridFSInputFile
import com.mongodb.gridfs.GridFSDBFile

import com.ncc.neon.userimport.types.ImportUtilities
import com.ncc.neon.userimport.types.FieldTypePair
import com.ncc.neon.userimport.types.FieldType

import com.ncc.neon.userimport.readers.CSVSheetReader
import com.ncc.neon.userimport.readers.ExcelSheetReader
import com.ncc.neon.userimport.readers.SheetReaderFactory

import com.ncc.neon.userimport.exceptions.UnsupportedFiletypeException
import com.ncc.neon.userimport.exceptions.BadSheetException
import org.codehaus.groovy.runtime.NullObject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.After
import org.junit.Test
import org.junit.Assume


class MongoImportHelperProcessorTest {

    private MongoImportHelperProcessor mongoImportHelperProcessor

    private static final String HOST = System.getProperty("mongo.host")
    private static final String USERNAME = "testUsername"
    private static final String PRETTY_NAME = "testPrettyName"
    private static final String UUID = 1234
    private static final Map FT_PAIRS_MAP1 = ["name": FieldType.STRING as String, "age": FieldType.INTEGER as String, "address": [FieldType.OBJECT as String, ["city": FieldType.STRING as String]]]
    private static final Map FT_PAIRS_MAP2 = ["age": [FieldType.OBJECT as String, [:]], "address": [FieldType.OBJECT as String, ["zip": FieldType.DATE as String]]]
    private static final Map FT_PAIRS_MAP3 = ["name": [FieldType.OBJECT as String, [:]], "age": FieldType.DATE as String]
    private static final List FT_PAIRS_LIST1 = [new FieldTypePair(
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
    private static final List FT_PAIRS_LIST2 = [new FieldTypePair(
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
    private static final List FT_PAIRS_LIST2_FAILED_FIELDS = [new FieldTypePair(
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
    private static final List FT_PAIRS_LIST3 = [new FieldTypePair(
            name: "name",
            type: FieldType.LONG as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "age",
            type: FieldType.OBJECT as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "address",
            type: FieldType.DATE as String,
            objectFTPairs: null
        )
    ]
    private static final List FT_PAIRS_LIST3_FAILED_FIELDS = [new FieldTypePair(
            name: "name",
            type: FieldType.OBJECT as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "age",
            type: FieldType.DATE as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "address",
            type: FieldType.OBJECT as String,
            objectFTPairs: null
        )
    ]
    private static final List FT_PAIRS_LIST3_FAILED_FIELDS2 = [new FieldTypePair(
            name: "name",
            type: FieldType.STRING as String,
            objectFTPairs: null
        ), new FieldTypePair(
            name: "age",
            type: FieldType.INTEGER as String,
            objectFTPairs: null
        )
    ]

    @BeforeClass
    static void beforeClass() {
        Assume.assumeTrue(HOST != null && HOST != "")
    }

    @Before
    void before() {
        mongoImportHelperProcessor = new MongoImportHelperProcessor()
        mongoImportHelperProcessor.sheetReaderFactory = [
            getSheetReader: { type ->
                switch(type) {
                    case "csv":
                        return new CSVSheetReader()
                    case "xlsx":
                        return new ExcelSheetReader()
                    default:
                        throw new UnsupportedFiletypeException("Import of that type of file is not supported.")
                }
            }
        ] as SheetReaderFactory
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

    @Test
    void "process type guesses"() {
        createFile("name,age,address\nJoe,23," +
            "\nJanice,null,\"{\"\"city\"\": \"\"Rockville\"\"}\"", "csv")
        mongoImportHelperProcessor.processTypeGuesses(HOST, UUID)
        GridFSDBFile dbFile = getDBFile()
        assert dbFile.get("programGuesses") == FT_PAIRS_MAP1
    }

    @Test
    void "process type guesses with unknown file type"() {
        createFile("data\ntest\ntesting2", "txt")
        mongoImportHelperProcessor.processTypeGuesses(HOST, UUID)
        GridFSDBFile dbFile = getDBFile()
        assert !dbFile.get("programGuesses")
    }

    @Test(expected=BadSheetException)
    void "process type guesses with no data"() {
        createFile("data", "csv")
        mongoImportHelperProcessor.processTypeGuesses(HOST, UUID)
    }

    @Test
    void "process load and convert"() {
        createFile("name,age,address\nJoe,23,\"{\"\"city\"\": \"\"Baltimore\"\", \"\"zip\"\": 12345}\"\nJanice,42,\"{\"\"city\"\": \"\"Rockville\"\", \"\"zip\"\": 54321}\"", "csv")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST1)
        GridFSDBFile dbFile = getDBFile()
        DBObject record = getRecord()
        assert dbFile.get("complete")
        assert dbFile.get("numCompleted") == 2
        assert !dbFile.get("failedFields")
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Double
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  Integer
    }

    @Test
    void "process load and convert with failed fields"() {
        createFile("name,age,address\nJoe,none,\"{\"\"city\"\": \"\"Baltimore\"\", \"\"zip\"\": 12345}\"\nJanice,42,\"{\"\"city\"\": \"\"Rockville\"\", \"\"zip\"\": 54321}\"", "csv")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST2)
        GridFSDBFile dbFile = getDBFile()
        DBObject record = getRecord()
        assert dbFile.get("complete")
        assert dbFile.get("numCompleted") == 2
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  String
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  String
        assert dbFile.get("failedFields") == FT_PAIRS_MAP2
    }

    @Test
    void "process load and convert on existing file"() {
        createFile("name,age,address\nJoe,23,\"{\"\"city\"\": \"\"Baltimore\"\", \"\"zip\"\": 12345}\"\nJanice,42,\"{\"\"city\"\": \"\"Rockville\"\"}\"", "csv")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST2)
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST2_FAILED_FIELDS)
        GridFSDBFile dbFile = getDBFile()
        DBObject record = getRecord()
        assert dbFile.get("complete")
        assert dbFile.get("numCompleted") == 2
        assert record.age.getClass() ==  Integer
        assert record.address.zip.getClass() ==  Integer
        assert !dbFile.get("failedFields")
    }

    @Test
    void "process load and convert on existing file with failed fields"() {
        createFile("name,age,address\nJoe,23,\"{\"\"city\"\": \"\"Baltimore\"\", \"\"zip\"\": null}\"\nJanice,42,\"{\"\"city\"\": \"\"Rockville\"\", \"\"zip\"\": 54321}\"", "csv")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST3)
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST3_FAILED_FIELDS)
        GridFSDBFile dbFile = getDBFile()
        DBObject record = getRecord()
        assert dbFile.get("complete")
        assert dbFile.get("numCompleted") == 2
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  String
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  NullObject
        assert dbFile.get("failedFields") == FT_PAIRS_MAP3

        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST3_FAILED_FIELDS2)
        dbFile = getDBFile()
        record = getRecord()
        assert dbFile.get("complete")
        assert dbFile.get("numCompleted") == 2
        assert record.name.getClass() ==  String
        assert record.age.getClass() ==  Integer
        assert record.address.getClass() ==  BasicDBObject
        assert record.address.city.getClass() ==  String
        assert record.address.zip.getClass() ==  NullObject
        assert !dbFile.get("failedFields")
    }

    @Test
    void "process load and convert with unknown file type"() {
        createFile("data\ntest\ntesting2", "txt")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST1)
        GridFSDBFile dbFile = getDBFile()
        assert !dbFile.get("failedFields")
    }

    @Test(expected=BadSheetException)
    void "process load and convert with no data"() {
        createFile("data", "csv")
        mongoImportHelperProcessor.processLoadAndConvert(HOST, UUID, null, FT_PAIRS_LIST1)
    }

    private void createFile(String input, String fileType) {
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSInputFile inFile = gridFS.createFile(new StringBufferInputStream(input))
        inFile.put("identifier", UUID)
        inFile.put("userName", USERNAME)
        inFile.put("prettyName", PRETTY_NAME)
        inFile.put("fileType", fileType)
        inFile.save()
        mongo.close()
    }

    private GridFSDBFile getDBFile() {
        MongoClient mongo = new MongoClient(HOST)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile dbFile = gridFS.findOne([identifier: UUID] as BasicDBObject)
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

}
