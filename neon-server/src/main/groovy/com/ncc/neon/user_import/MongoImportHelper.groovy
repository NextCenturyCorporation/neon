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

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFCell

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.BasicDBObject

/**
 * Implements methods to add, remove, and convert fields of records in a mongo database.
 */
@Component
class MongoImportHelper implements ImportHelper {
    
    @Override
    List uploadData(String host, String userName, String prettyName, String fileType, InputStream stream) {
        if(fileType.equalsIgnoreCase("csv")) {
            return uploadCSV(host, userName, prettyName, IOUtils.lineIterator(stream, "UTF-8"))
        }
        else if(fileType.equalsIgnoreCase("xlsx")) {
            return uploadExcel(host, userName, prettyName, stream)
        }
        else {
            return ["Import not supported for this file type."]
        }
    }

    List uploadCSV(String host, String userName, String prettyName, LineIterator reader) {
        MongoClient mongo = new MongoClient(host, 27017)
        DBCollection collection = mongo.getDB(makeUglyName(userName, prettyName)).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        addMetadata(mongo, userName, prettyName)

        String row = ImportUtilities.getNextWholeRow(reader)
        if(!row || !reader.hasNext()) {
            return ["There was no data in this file to import."]
        }
        List fields = ImportUtilities.getCellsFromRow(row)

        row = ImportUtilities.getNextWholeRow(reader)
        while(row) {
            List values = ImportUtilities.getCellsFromRow(row)
            DBObject record = new BasicDBObject()
            for(int x = 0; x < values.size(); x++) {
                record.append(fields[x], values[x])
            }
            collection.insert(record)
            row = ImportUtilities.getNextWholeRow(reader)
        }
        List guesses = guessTypes(collection, fields)
        mongo.close()
        return guesses
    }

    List uploadExcel(String host, String userName, String prettyName, InputStream stream) {
        XSSFWorkbook workbook = new XSSFWorkbook(stream)
        if(workbook.getNumberOfSheets() < 1 || workbook.getSheetAt(0).getLastRowNum() < 1) {
            return ["There was no data in this file to import."]
        }
        XSSFSheet sheet = workbook.getSheetAt(0)
        MongoClient mongo = new MongoClient(host, 27017)
        DBCollection collection = mongo.getDB(makeUglyName(userName, prettyName)).getCollection(sheet.getSheetName())
        addMetadata(mongo, userName, prettyName)
        List fields = []
        XSSFRow row = sheet.getRow(0).each { cell ->
            fields << cell.getStringCellValue()
        }
        for(int x = 1; x <= sheet.getLastRowNum(); x++) {
            DBObject record = new BasicDBObject()
            sheet.getRow(x).each { cell ->
                record.append(fields[cell.getColumnIndex()], cell.getStringCellValue())
            }
            collection.insert(record)
        }
        List guesses = guessTypes(collection, fields)
        workbook.close()
        mongo.close()
        return guesses
    }

    @Override
    boolean dropData(String host, String userName, String prettyName) {
        MongoClient mongo = new MongoClient(host, 27017)
        DB databaseToDrop = getDatabase(mongo, [userName: userName, prettyName: prettyName])
        if(databaseToDrop.getCollectionNames()) {
            databaseToDrop.dropDatabase()
        }
        else {
            return false
        }
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        metaCollection.remove([userName: userName, prettyName: prettyName] as BasicDBObject)
        if(!metaCollection.getCount()) {
            metaDatabase.dropDatabase()
        }
        mongo.close()
        return true
    }

    @Override
    List convertFields(String host, String userName, String prettyName, UserFieldDataBundle bundle) {
        List<FieldTypePair> fields = bundle.fields, failedFields = [], tempFailed = []
        MongoClient mongo = new MongoClient(host, 27017)
        DBCollection collection = getDatabase(mongo, [userName: userName, prettyName: prettyName])?.getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        if(collection == null) {
            return null
        }
        DBCursor cursor = collection.find()
        while(cursor.hasNext()) {
            BasicDBObject record = cursor.next() as BasicDBObject
            fields.each { field ->
                String s = record.get(field.name) as String
                if(s == "" || (field.type =~ /(?i)integer|long|double|float(?-i)/ && s =~ /(?i)none|null(?-i)/ && s.length() == 4)) {
                    record.removeField(field.name) // TODO - As it is this is pretty permanent. Given the file size restriction maybe this is okay because they can just re-upload?
                    return
                }
                Object result = ImportUtilities.convertValueToType(record.get(field.name), field.type, bundle.format)
                (result != null) ? record.put(field.name, result) : tempFailed.add(field)
            }
            failedFields.addAll(tempFailed)
            fields.removeAll(tempFailed)
            tempFailed.clear()
            collection.save(record)
        }
        cursor.close()
        mongo.close()
        return failedFields
    }

    /**
     * Gets a database form the given mongo instance, given its identifier.
     * @param mongo The mongo instance from which to get the database.
     * @param identifier The Map containing username and database name linked to the database to get.
     * @return The database on the given mongo instance with the given identifier.
     */
    private DB getDatabase(MongoClient mongo, Map identifier) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = metaCollection.findOne(identifier as BasicDBObject)
        return mongo.getDB(metaRecord.get("databaseName"))
    }

    /**
     * Adds metadata for a user-created database to the given mongo instance. Utilizes a database dedicated to storing information
     * about user-created databases.
     * @param mongo The mongo instance on which to put the metadata for the given database.
     * @param userName The name of the user creating the database.
     * @param identifier The identifier with which the database of the given name is associated.
     */
    private void addMetadata(MongoClient mongo, String userName, String prettyName) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = new BasicDBObject()
        metaRecord.append("userName", userName)
        metaRecord.append("prettyName", prettyName)
        metaRecord.append("databaseName", makeUglyName(userName, prettyName))
        metaCollection.insert(metaRecord)
    }

    /**
     * Guesses the types of the given list of fields in the given collection. Does this by attempting to convert them to various types of objects,
     * starting off strict and getting less so as they fail to convert. Returns a list with each of the given fields associated with a type string.
     * @param collection The collection whose fields should be checked.
     * @param fields The list of fields whose types should be checked.
     * @return A list of the given fields, each wrapped in a {@link FieldTypePair} object to accociate them with their guessed type.
     */
    private List guessTypes(DBCollection collection, List fields) {
        long numRecords = (collection.count() > ImportUtilities.NUM_TYPE_CHECKED_RECORDS) ? ImportUtilities.NUM_TYPE_CHECKED_RECORDS : collection.count()
        List fieldsAndTypes = []
        List records = collection.find().limit(numRecords as int).toArray() // Safe so long as NUM_TYPE_CHECKED_RECORDS is reasonable.
        fields.each { field ->
            List valuesOfField = []
            records.each { record ->
                valuesOfField.add(record.get(field))
            }
            FieldTypePair pair = null
            // Sets the pair type equals to the first type that matches.
            pair = (!pair && ImportUtilities.isListIntegers(valuesOfField)) ? new FieldTypePair(name: field, type: "Integer") : pair
            pair = (!pair && ImportUtilities.isListLongs(valuesOfField)) ? new FieldTypePair(name: field, type: "Long") : pair
            pair = (!pair && ImportUtilities.isListDoubles(valuesOfField)) ? new FieldTypePair(name: field, type: "Double") : pair
            pair = (!pair && ImportUtilities.isListFloats(valuesOfField)) ? new FieldTypePair(name: field, type: "Float") : pair
            pair = (!pair && ImportUtilities.isListDates(valuesOfField)) ? new FieldTypePair(name: field, type: "Date") : pair
            pair = (!pair) ? new FieldTypePair(name: field, type: "String") : pair
            fieldsAndTypes.add(pair)
        }
        return fieldsAndTypes
    }

    /**
     * Takes a username and "pretty" human-readable name and uses them to generate an ugly, more unique name.
     * @param userName The username to use in making the ugly name.
     * @param pretttyName The human-readable name to use in making the ugly name.
     * @return The ugly name created from the given username and pretty name.
     */
    private String makeUglyName(String userName, String prettyName) {
        return "$userName${ImportUtilities.SEPARATOR}$prettyName"
    }
}