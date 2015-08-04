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

import com.ncc.neon.userimport.exceptions.BadSheetException
import com.ncc.neon.userimport.exceptions.UnsupportedFiletypeException

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import org.apache.commons.io.IOUtils
import org.apache.commons.io.LineIterator

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile

import com.monitorjbl.xlsx.StreamingReader
import com.monitorjbl.xlsx.impl.StreamingRow

/**
 * Houses the asynchronous methods that do the actual labor of importing data, as well as a number of helper methods and methods to handle different types of input.
 */
@Component
class MongoImportHelperProcessor implements ImportHelperProcessor {

    @Async
    @Override
    void processTypeGuesses(String host, String uuid) {
        MongoClient mongo = new MongoClient(host, 27017)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile gridFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        try {
            switch(fileType) {
                case "csv":
                    processTypeGuessesCSV(gridFile)
                    break
                case "xlsx":
                    processTypeGuessesExcel(gridFile)
                    break
                default:
                    mongo.close()
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        }
        finally {
            mongo.close()
        }
    }

    void processTypeGuessesCSV(GridFSDBFile file) {
        LineIterator iter = IOUtils.lineIterator(file.getInputStream(), "UTF-8")
        String row = ImportUtilities.getNextWholeRow(iter)
        if(!row || !iter.hasNext()) {
            throw new BadSheetException("No data in this file to process!")
        }
        List fields = ImportUtilities.getCellsFromRow(row)
        Map fieldsAndValues = [:]
        fields.each { field ->
            fieldsAndValues.put(field, [])
        }
        row = ImportUtilities.getNextWholeRow(iter)
        for(int counter = 0; counter < ImportUtilities.NUM_TYPE_CHECKED_RECORDS && row; counter++) {
            List rowValues = ImportUtilities.getCellsFromRow(row)
            for(int x = 0; x < fields.size() && x < rowValues.size(); x++) {
                if(rowValues.get(x)) { // Only add the value to the map if it's non-null and not an empty string.
                    fieldsAndValues.get(fields.get(x)).add(rowValues.get(x))
                }
            }
            row = ImportUtilities.getNextWholeRow(iter)
        }
        Map fieldsAndTypes = fieldTypePairListToMap(ImportUtilities.getTypeGuesses(fieldsAndValues))
        file.put("programGuesses", fieldsAndTypes)
        file.save()
    }

    void processTypeGuessesExcel(GridFSDBFile file) {
        StreamingReader sheet
        try {
            sheet = StreamingReader.builder().sheetIndex(0).read(file.getInputStream())
            Iterator iterator = sheet.iterator()
            if(!iterator.hasNext()) { // An initial call to hasNext is needed to "prime" the sheet iterator, so we may as well make use of the result.
                throw new BadSheetException("No data in this file to process!")
            }
            List fields = []
            Map fieldsAndValues = [:]
            iterator.next().each { cell ->
                fields << cell.getContents().toString()
                fieldsAndValues.put(cell.getContents().toString(), [])
            }
            StreamingRow row = iterator.next()
            for(int count = 0; count < ImportUtilities.NUM_TYPE_CHECKED_RECORDS && row; count++) {
                for(int col = 0; col < fields.size(); col++) {
                    if(row.getCell(col)) { // Only add the value to the map if it's non-null.
                        fieldsAndValues.get(fields.get(col)).add(row.getCell(col).getContents().toString())
                    }
                }
                row = iterator.hasNext() ? iterator.next() : null
            }
            file.put("programGuesses", fieldTypePairListToMap(ImportUtilities.getTypeGuesses(fieldsAndValues)))
            file.save()
        }
        finally{
            sheet.close()
        }
    }

    @Async
    @Override
    void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        MongoClient mongo = new MongoClient(host, 27017)
        GridFSDBFile gridFile = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME)).findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        try {
            if(alreadyLoaded(mongo, gridFile)) {
                processConvert(mongo, gridFile, dateFormat, fieldTypePairs)
                return
            }
            switch(fileType) {
                case "csv":
                    processLoadAndConvertCSV(mongo, gridFile, dateFormat, fieldTypePairs)
                    break
                case "xlsx":
                    processLoadAndConvertExcel(mongo, gridFile, dateFormat, fieldTypePairs)
                    break
                default:
                    mongo.close()
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        }
        finally {
            mongo.close()
        }
    }

    void processLoadAndConvertCSV(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        file.put("numCompleted", 0)
        file.save()
        LineIterator iter = IOUtils.lineIterator(file.getInputStream(), "UTF-8")
        Map fieldsToTypes = fieldTypePairListToMap(fieldTypePairs)
        Set<FieldTypePair> failedFields = [] as Set
        DBCollection collection = mongo.getDB(ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)

        String row = ImportUtilities.getNextWholeRow(iter)
        if(!row || !iter.hasNext()) {
            throw new BadSheetException("No data in this file to process!")
        }
        List fieldNamesInOrder = ImportUtilities.getCellsFromRow(row)
        row = ImportUtilities.getNextWholeRow(iter)
        int numCompleted = 0
        while(row) {
            List values = ImportUtilities.getCellsFromRow(row)
            List<FieldTypePair> failed = addRecord(collection, fieldNamesInOrder, values, fieldsToTypes, dateFormat)
            failed.each { ftPair ->
                failedFields << ftPair
                fieldsToTypes.put(ftPair.name, "String") // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
            }
            row = ImportUtilities.getNextWholeRow(iter)
            if(numCompleted++ % 1037 == 0 || !row) {
                file.put("numCompleted", numCompleted)
                file.save()
            }
        }
        file.put("failedFields", fieldTypePairListToMap(failedFields as List))
        file.save()
        addMetadata(mongo, file)
    }

    // Need to make this method shorter... somehow.
    void processLoadAndConvertExcel(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        int numCompleted = 0
        StreamingReader sheet
        Map fieldsToTypes = fieldTypePairListToMap(fieldTypePairs)
        Set<FieldTypePair> failedFields = [] as Set
        DBCollection collection = mongo.getDB(ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        try {
            sheet = StreamingReader.builder().sheetIndex(0).read(file.getInputStream())
            Iterator iterator = sheet.iterator()
            if(!iterator.hasNext()) { // An initial call to hasNext is needed to "prime" the sheet iterator, so we may as well make use of the result.
                throw new BadSheetException("No data in this file to process!")
            }
            List fieldNamesInOrder = []
            iterator.next().each { cell -> // Assuming here that the first row will not have any empty cells in the middle e.g. "A, B, , D".
                fieldNamesInOrder << cell.getContents().toString()
            }
            while(iterator.hasNext()) {
                StreamingRow row = iterator.next()
                List values = []
                for(int col = 0; col < fieldNamesInOrder.size(); col++) {
                    values << row.getCell(col)?.getContents()?.toString()
                }
                List<FieldTypePair> failed = addRecord(collection, fieldNamesInOrder, values, fieldsToTypes, dateFormat)
                failed.each { ftPair ->
                    failedFields << ftPair
                    fieldsToTypes.put(ftPair.name, "String") // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
                }
                if(numCompleted++ % 137 == 0 || !iterator.hasNext()) {
                    file.put("numCompleted", numCompleted)
                    file.save()
                }
            }
            file.put("failedFields", fieldTypePairListToMap(failedFields as List))
        }
        finally {
            file.save()
            sheet.close()
        }
        addMetadata(mongo, file)
    }

    // Simply converts the fields of records in a database - no adding new records, just conversion. To be called if the user messes up when giving field
    // types the first time, so we don't wind up adding duplicate records the the database.
    void processConvert(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        int numCompleted = 0
        file.put("numCompleted", numCompleted)
        file.put("complete", false)
        file.save()
        List<FieldTypePair> pairs = fieldTypePairs
        DBCollection collection = mongo.getDB(ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        DBCursor cursor = collection.find()
        Set<FieldTypePair> failedFields = [] as Set
        cursor.each { record ->
            List<FieldTypePair> failed = []
            pairs.each { ftPair ->
                String stringValue = record.get(ftPair.name) as String
                if(!stringValue || (ftPair.type =~ /(?i)integer|long|double|float(?-i)/ && stringValue =~ /(?i)none|null(?-i)/ && stringValue.length() == 4)) {
                    return // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
                }
                Object objectValue = ImportUtilities.convertValueToType(stringValue, ftPair.type, dateFormat)
                if(objectValue instanceof ConversionFailureResult) {
                    record.put(ftPair.name, objectValue)
                }
                else {
                    failed.add(ftPair)
                }
            }
            failed.each { ftPair ->
                failedFields << ftPair
                pairs.remove(ftPair) // If a field failed to convert, stop trying to convert it on future records.
            }
            if(numCompleted++ % 137 == 0) {
                file.put("numCompleted", numCompleted)
                file.save()
            }
        }
        file.putAll(["complete": true, "numCompleted": numCompleted, "failedFields": fieldTypePairListToMap(failedFields as List)] as BasicDBObject)
        file.save()
    }

    // Helper method. Adds a single record to the given collection, and returns which fields if any failed to convert during the process.
    List<FieldTypePair> addRecord(DBCollection collection, List namesInOrder, List valuesInOrder, Map namesToTypes, String dateFormat) {
        List<FieldTypePair> failedFields = []
        DBObject record = new BasicDBObject()
        for(int x = 0; x < namesInOrder.size() && x < valuesInOrder.size(); x++) {
            String stringVal = valuesInOrder[x]
            String type = namesToTypes.get(namesInOrder[x])
            if(!stringVal || (type =~ /(?i)integer|long|double|float(?-i)/ && stringVal =~ /(?i)none|null(?-i)/ && stringVal.length() == 4)) {
                continue // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            }
            Object objectVal = ImportUtilities.convertValueToType(stringVal, type, dateFormat)
            if(objectVal instanceof ConversionFailureResult) {
                record.put(namesInOrder[x], stringVal)
                failedFields.add([name: namesInOrder[x], type: type] as FieldTypePair)
            }
            else {
                record.put(namesInOrder[x], objectVal)
            }
        }
        collection.insert(record)
        return failedFields
    }

    // Helper method. Adds an entry for a set of user-given data in the meta database. Also marks the GridFSFile containing that data as completed,
    // so it can be deleted the next time its status is asked for.
    void addMetadata(MongoClient mongo, GridFSDBFile file) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = new BasicDBObject()
        metaRecord.append("userName", file.get("userName"))
        metaRecord.append("prettyName", file.get("prettyName"))
        metaRecord.append("databaseName", ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName")))
        metaCollection.insert(metaRecord)

        file.put("complete", true)
        file.save()
    }

    Map fieldTypePairListToMap(List ftPairs) {
        Map m = [:]
        ftPairs.each { ftPair ->
            m.put(ftPair.name, ftPair.type)
        }
        return m
    }

    // Determines if a file has already been loaded into a database by checking ifthere is already a database with the same name as this one would have.
    boolean alreadyLoaded(MongoClient mongo, GridFSDBFile file) {
        DBCollection collection = mongo.getDB(ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        return (collection && collection.count > 0)
    }
}