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

import com.ncc.neon.user_import.exceptions.BadSheetException
import com.ncc.neon.user_import.exceptions.UnsupportedFiletypeException

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import org.apache.commons.io.IOUtils
import org.apache.commons.io.LineIterator

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSInputFile
import com.mongodb.gridfs.GridFSDBFile

import com.monitorjbl.xlsx.StreamingReader
import com.monitorjbl.xlsx.impl.StreamingRow

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
        Map fieldsAndTypes = ImportUtilities.getTypeGuesses(fieldsAndValues, "map") // Apparently mongo can't deserialize lists of FieldTypePairs for storage. :/
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
            Map fieldsAndTypes = ImportUtilities.getTypeGuesses(fieldsAndValues, "map") // Apparently mongo can't deserialize listsof FieldTypePairs for storage. :/
            file.put("programGuesses", fieldsAndTypes)
            file.save()
        }
        catch(Exception e) {
            println e.getClass()
            println e.getMessage()
            e.printStackTrace()
        }
        finally{
            sheet.close()
        }
    }

    @Async
    @Override
    void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        MongoClient mongo = new MongoClient(host, 27017)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile gridFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        try {
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
        LineIterator iter = IOUtils.lineIterator(file.getInputStream(), "UTF-8")
        Map fieldsToTypes = [:]
        fieldTypePairs.each {ftPair ->
            fieldsToTypes.put(ftPair.name, ftPair.type)
        }
        Set<FieldTypePair> failedFields = [] as Set
        DBCollection collection = mongo.getDB(ImportUtilities.makeUglyName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)

        String row = ImportUtilities.getNextWholeRow(iter)
        if(!row || !iter.hasNext()) {
            throw new BadSheetException("No data in this file to process!")
        }
        List fieldNamesInOrder = ImportUtilities.getCellsFromRow(row)
        row = ImportUtilities.getNextWholeRow(iter)
        while(row) {
            List values = ImportUtilities.getCellsFromRow(row)
            List<FieldTypePair> failed = addRecord(collection, fieldNamesInOrder, values, fieldsToTypes, dateFormat)
            failed.each { ftPair ->
                failedFields << ftPair
                fieldsToTypes.put(ftPair.name, "String") // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
            }
            row = ImportUtilities.getNextWholeRow(iter)
        }
        file.put("failedFields", failedFields)
        file.save()
        addMetadata(mongo, file)
    }

    // Need to make this method shorter... somehow.
    void processLoadAndConvertExcel(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        StreamingReader sheet
        Map fieldsToTypes = [:]
        fieldTypePairs.each {ftPair ->
            fieldsToTypes.put(ftPair.name, ftPair.type)
        }
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
            StreamingRow row = iterator.next()
            while(row) {
                List values = []
                for(int col = 0; col < fieldNamesInOrder.size(); col++) {
                    values << row.getCell(col)?.getContents()?.toString()
                }
                List<FieldTypePair> failed = addRecord(collection, fieldNamesInOrder, values, fieldsToTypes, dateFormat)
                failed.each { ftPair ->
                    failedFields << ftPair
                    fieldsToTypes.put(ftPair.name, "String") // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
                }
                row = iterator.hasNext() ? iterator.next() : null
            }
        }
        catch(Exception e) {
            println e.getClass()
            println e.getMessage()
            e.printStackTrace()
        }
        finally {
            sheet.close()
        }
        file.put("failedFields", failedFields)
        file.save()
        addMetadata(mongo, file)
    }

    // Helper method. Adds a single record to the given collection, and returns which fields if any failed to convert during the process.
    List<FieldTypePair> addRecord(DBCollection collection, List fieldNamesInOrder, List recordValuesInOrder, Map fieldNamesToConvertTypes, String dateFormat) {
        List<FieldTypePair> failedFields = []
        DBObject record = new BasicDBObject()
        for(int x = 0; x < fieldNamesInOrder.size() && x < recordValuesInOrder.size(); x++) {
            String stringValue = recordValuesInOrder[x]
            String convertType = fieldNamesToConvertTypes.get(fieldNamesInOrder[x])
            if(!stringValue || (convertType =~ /(?i)integer|long|double|float(?-i)/ && stringValue =~ /(?i)none|null(?-i)/ && stringValue.length() == 4)) {
                continue // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            }
            Object objectValue = ImportUtilities.convertValueToType(stringValue, convertType, dateFormat)
            record.put(fieldNamesInOrder[x], (objectValue == null) ? stringValue : objectValue)
            if(objectValue == null) {
                failedFields.add([name: fieldNamesInOrder[x], type: convertType] as FieldTypePair)
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
}