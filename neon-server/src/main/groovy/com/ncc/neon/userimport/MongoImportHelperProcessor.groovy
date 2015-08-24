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
import com.ncc.neon.userimport.readers.SheetReader
import com.ncc.neon.userimport.readers.SheetReaderFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

import com.mongodb.MongoClient
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ncc.neon.userimport.types.ImportUtilities
import com.ncc.neon.userimport.types.ConversionFailureResult
import com.ncc.neon.userimport.types.FieldType
import com.ncc.neon.userimport.types.FieldTypePair


/**
 * Houses the asynchronous methods that do the actual labor of importing data into mongo data stores, as well as a number
 * of helper methods and methods to handle different types of input.
 */
@Component
class MongoImportHelperProcessor implements ImportHelperProcessor {

    // For use by the various loadAndConvert methods. Updating loading/conversion progress is relatively expensive, so only
    // do it every so many records. For now, 137 was chosen because it's a prime number whose multiples look fairly random.
    private static final int UPDATE_FREQUENCY = 137

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoImportHelperProcessor)

    @Autowired
    SheetReaderFactory sheetReaderFactory

    @Async
    @Override
    void processTypeGuesses(String host, String uuid) {
        MongoClient mongo = new MongoClient(host)
        GridFS gridFS = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME))
        GridFSDBFile gridFile = gridFS.findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        try {
            switch(fileType) {
                case "csv":
                case "xlsx":
                    processTypeGuessesFromFile(gridFile, fileType)
                    break
                default:
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        } catch(UnsupportedFiletypeException e){
            LOGGER.error(e.getMessage(), e)
        } finally {
            mongo.close()
        }
    }

    /**
     * Gets type guesses for fields of information stored in CSV or XLSX format and stores them as metadata in the GridFSDBFile that
     * stores the file.
     * @param file The GridFSDBFile storing the file for which to make field type guesses.
     * @param fileType
     */
    private void processTypeGuessesFromFile(GridFSDBFile file, String fileType) {
        SheetReader reader
        try {
            reader = sheetReaderFactory.getSheetReader(fileType).initialize(file.getInputStream())
            if(!reader) {
                throw new BadSheetException("No records in this file to read!")
            }
            List fields = reader.getSheetFieldNames()
            Map fieldsAndValues = [:]
            fields.each { field ->
                fieldsAndValues.put(field, [])
            }
            for(int counter = 0; counter < ImportUtilities.NUM_TYPE_CHECKED_RECORDS && reader.hasNext(); counter++) {
                List record = reader.next()
                for(int field = 0; field < fields.size() && field < record.size(); field++) {
                    if(record[field]) {
                        fieldsAndValues[(fields[field])] << record[field]
                    }
                }
            }
            updateFile(file, [programGuesses: fieldTypePairListToMap(ImportUtilities.getTypeGuesses(fieldsAndValues), true)])
        } finally {
            reader?.close()
        }
    }

    @Async
    @Override
    void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        MongoClient mongo = new MongoClient(host)
        GridFSDBFile gridFile = new GridFS(mongo.getDB(ImportUtilities.MONGO_UPLOAD_DB_NAME)).findOne([identifier: uuid] as BasicDBObject)
        String fileType = gridFile.get("fileType")
        updateFile(gridFile, [complete: false, numCompleted: 0])
        try {
            if(alreadyLoaded(mongo, gridFile)) {
                processConvert(mongo, gridFile, dateFormat, fieldTypePairs)
                return
            }
            switch(fileType) {
                case "csv":
                case "xlsx":
                    processLoadAndConvertFromFile(mongo, gridFile, dateFormat, fieldTypePairs, fileType)
                    break
                default:
                    mongo.close()
                    throw new UnsupportedFiletypeException("Can't parse files of type ${fileType}!")
            }
        } catch(UnsupportedFiletypeException e){
            LOGGER.error(e.getMessage(), e)
        } finally {
            mongo.close()
        }
    }

    /**
     * Reads a raw CSV or XLSX file from the given GridFSDFile line by line, grabbing records, converting their fields into the types
     * given in fieldTypePairs, and then storing them in a database on the given mongo instance. The database's name is
     * generated using the userName and prettyName metadata in the file. This method assumes that there is only one record
     * per line of the file, and that the file contains nothing but records and a list of field names on the first line.
     * If any fields fail to convert for at least one record, these fields and the type they were attempting to be converted to are
     * stored as metadata in the given file.
     * @param mongo The mongo instance on which the database in which to convert records is located.
     * @param file The GridFSDBFile containing metadata that allows for finding of the database in which to convert records.
     * @param dateFormat The date format string to be used when attempting to convert any record to a Date.
     * @param fieldTypePairs A list of field names and what types they should be converted to.
     * @param fileType The file type of the file being read
     */
    private void processLoadAndConvertFromFile(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs, String fileType) {
        SheetReader reader
        try {
            reader = sheetReaderFactory.getSheetReader(fileType).initialize(file.getInputStream())
            if(!reader) {
                throw new BadSheetException("No records in this file to read!")
            }
            int numCompleted = 0
            Map fieldsToTypes = fieldTypePairListToMap(fieldTypePairs, false)
            Set<FieldTypePair> failedFields = [] as Set
            DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
            List fieldNamesInOrder = reader.getSheetFieldNames()
            reader.each { record ->
                addRecord(collection, fieldNamesInOrder, record, fieldsToTypes, dateFormat).each { failedFTPair ->
                    failedFields << failedFTPair
                    // If a field failed to convert, set its type to string to prevent exceptions being thrown for that field on future records.
                    if(failedFTPair.type == FieldType.OBJECT) {
                        failedFTPair.objectFTPairs.each { pair ->
                            fieldsToTypes[failedFTPair.name][1].put(pair.name, FieldType.STRING)
                        }
                    } else {
                        fieldsToTypes.put(failedFTPair.name, FieldType.STRING)
                    }
                }
                if(numCompleted++ % UPDATE_FREQUENCY == 0) {
                    updateFile(file, [numCompleted: numCompleted])
                }
            }
            updateFile(file, [numCompleted: numCompleted, failedFields: fieldTypePairListToMap(failedFields as List, true)])
            addMetadata(mongo, file)
        } finally {
            reader?.close()
        }
    }

    /**
     * Converts the fields of records in a database without adding any new ones to it. Used when the user messes up with their type
     * guesses on the first try to prevent multiple-loading of records into a database. Records progress as metadata in the given
     * GridFSDBFile as it goes along.
     * If any fields fail to convert for at least one record, these fields and the type they were attempting to be converted to are
     * stored as metadata in the given file.
     * @param mongo The mongo instance on which the database in which to convert records is located.
     * @param file The GridFSDBFile containing metadata that allows for finding of the database in which to convert records.
     * @param dateFormat The date format string to be used when attempting to convert any record to a Date.
     * @param fieldTypePairs A list of field names and what types they should be converted to.
     */
     @SuppressWarnings('MethodSize')
    private void processConvert(MongoClient mongo, GridFSDBFile file, String dateFormat, List<FieldTypePair> fieldTypePairs) {
        int numCompleted = 0
        updateFile(file, [numCompleted: numCompleted, complete: false])
        List<FieldTypePair> pairs = fieldTypePairs
        DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        DBCursor cursor = collection.find()
        Set<FieldTypePair> failedFields = [] as Set
        cursor.each { record ->
            DBObject origRecord = new BasicDBObject()
            origRecord.put("_id", record.get("_id"))
            pairs.each { ftPair ->
                def recordValue = record.get(ftPair.name)
                if(recordValue instanceof String) {
                    // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
                    if(!recordValue || (recordValue.length() == 4 && recordValue =~ /(?i)none|null(?-i)/) &&
                        (ftPair.type == FieldType.INTEGER || ftPair.type == FieldType.LONG || ftPair.type == FieldType.DOUBLE || ftPair.type == FieldType.FLOAT)) {
                        return
                    }
                    Object convertedValue = ImportUtilities.convertValueToType(recordValue, ftPair.type, dateFormat)
                    if(convertedValue instanceof ConversionFailureResult) {
                        failedFields.add(ftPair)
                    } else if(ftPair.type == FieldType.OBJECT) {
                        /* Converting STRING to OBJECT and converting all fields in OBJECT to a guessed type */
                        DBObject objectRecord = new BasicDBObject()
                        Map convertedFields = [:]
                        /* If the ftPair contains objectFTPairs, use it to convert the OBJECT's pairs to the type given in objectFTPairs.
                         * Otherwise, convert the STRING to an object and guess the type of the pairs in the OBJECT.
                         */
                        if(ftPair.objectFTPairs) {
                            convertedFields = convertPairsFromFieldTypePairs(ftPair.objectFTPairs, convertedValue, objectRecord, true, dateFormat)
                        } else {
                            convertedFields = convertPairsFromMap(convertedValue, objectRecord, [:], dateFormat)
                        }
                        record.put(ftPair.name, convertedFields.record)
                        if(convertedFields.failedFields.size()) {
                            failedFields.add([name: ftPair.name, type: ftPair.type, objectFTPairs: convertedFields.failedFields] as FieldTypePair)
                        }
                    } else {
                        /* Converting STRING to another type that's not an OBJECT */
                        record.put(ftPair.name, convertedValue)
                    }
                } else {
                    if(ftPair.type != FieldType.OBJECT) {
                        /* Converting OBJECT to other type */
                        Object convertedValue = ImportUtilities.convertValueToType(recordValue as String, ftPair.type, dateFormat)
                        if(convertedValue instanceof ConversionFailureResult) {
                            failedFields.add(ftPair)
                        } else {
                            record.put(ftPair.name, convertedValue)
                        }
                    } else {
                        /* Converting fields in an OBJECT to another type */
                        Map convertedFields = convertPairsFromFieldTypePairs(ftPair.objectFTPairs, recordValue, recordValue, false, dateFormat)
                        record.put(ftPair.name, convertedFields.record)
                        if(convertedFields.failedFields.size()) {
                            failedFields.add([name: ftPair.name, type: ftPair.type, objectFTPairs: convertedFields.failedFields] as FieldTypePair)
                        }
                    }
                }
            }
            failedFields.each { ftPair ->
                // If a field failed to convert, stop trying to convert it on future records.
                pairs.remove(ftPair)
            }
            record.removeField("_id")
            collection.update(origRecord, record)
            if(numCompleted++ % UPDATE_FREQUENCY == 0) {
                updateFile(file, [numCompleted: numCompleted])
            }
        }
        updateFile(file, [complete: true, numCompleted: numCompleted, failedFields: fieldTypePairListToMap(failedFields as List, true)])
    }

    /**
     * Helper method that adds a single record to a mongo collection, given a list of field names and string representations of
     * values, a map of which fields should be converted to which types, and a date format string to use when attempting
     * to convert field values to dates. The field name and value lists must be in order with respect to each other. That is, the
     * field name at index 0 of one list must match up with the value at index 0 of the other.
     * Returns a list of any fields that failed to convert, as well as what type they were attempting to be converted to.
     * @param collection The DBCollection into which to insert the record.
     * @param namesInOrder A list containing the names of fields the record could have (could because it's possible for some of those
     * fields to be null). This list must be in order with respect to the values list for this method to work properly.
     * @param valuesInOrder A list containing the values of fields this record has (any number of these values can be null). This list
     * must be in order with respect to the field names list in order for this method to work properly.
     * @param namesToTypes A map of field names to which type those fields should be converted to.
     * @param dateFormat A date formatting string to be used when attempting to convert a field into a Date. This can be null.
     * @return A list of FieldTypePairs containing the names of any fields which failed to convert, as well as the types they were
     * attempting to be converted to.
     */
    private List<FieldTypePair> addRecord(DBCollection collection, List namesInOrder, List valuesInOrder, Map namesToTypes, String dateFormat) {
        List<FieldTypePair> failedFields = []
        DBObject record = new BasicDBObject()
        for(int field = 0; field < namesInOrder.size() && field < valuesInOrder.size(); field++) {
            String stringValue = valuesInOrder[field]
            def type = namesToTypes.get(namesInOrder[field])
            // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            if(!stringValue || (stringValue.length() == 4 && stringValue =~ /(?i)none|null(?-i)/) &&
                (ftPair.type == FieldType.INTEGER || ftPair.type == FieldType.LONG || ftPair.type == FieldType.DOUBLE || ftPair.type == FieldType.FLOAT)) {
                continue 
            }
            // Fields of type OBJECT contain a list, instead of just a type, in namesToTypes that contain the type and a map of fields to types for each
            // value in the OBJECT, respectively.
            if(type instanceof List) {
                DBObject objectRecord = new BasicDBObject()
                Object objectValue = ImportUtilities.convertValueToType(stringValue, type[0], dateFormat)
                if(objectValue instanceof ConversionFailureResult) {
                    record.put(namesInOrder[field], stringValue)
                    failedFields.add([name: namesInOrder[field], type: type[0]] as FieldTypePair)
                } else {
                    Map convertedFields = convertPairsFromMap(objectValue, objectRecord, type[1], dateFormat)
                    record.put(namesInOrder[field], convertedFields.record)
                    if(convertedFields.failedFields.size()) {
                        failedFields.add([name: namesInOrder[field], type: type[0], objectFTPairs: convertedFields.failedFields] as FieldTypePair)
                    }
                }
            } else {
                Object objectValue = ImportUtilities.convertValueToType(stringValue, type, dateFormat)
                if(objectValue instanceof ConversionFailureResult) {
                    record.put(namesInOrder[field], stringValue)
                    failedFields.add([name: namesInOrder[field], type: type] as FieldTypePair)
                } else {
                    record.put(namesInOrder[field], objectValue)
                }
            }
        }
        collection.insert(record)
        return failedFields
    }

    /**
     * Converts values in an object to the types given in nameToTypes or a guessed type and puts them in the DB record provided
     * @param values Map of field names to values to convert
     * @param record Mongo DB object containing the record to add values to
     * @param nameToTypes A map of field names to which type those fields should be converted to. This can be an empty map
     * in order to use a guessed type.
     * @param dateFormat A date formatting string to be used when attempting to convert a field into a Date. This can be null.
     * @return Map with the keys 'record' (containing the DB object record that values were added to) and 'failedFields' (fields that
     * failed to convert)
     */
    private Map convertPairsFromMap(Map values, DBObject record, Map nameToTypes, String dateFormat) {
        def toReturn = [:]
        toReturn.put('failedFields', [])

        values.keySet().each { key ->
            def stringValue = values[key] as String
            def type

            if(nameToTypes) {
                type = nameToTypes[key]
            } else {
                def fieldAndValue = [:]
                fieldAndValue.put(key, [stringValue])
                List<FieldTypePair> typeGuess = ImportUtilities.getTypeGuesses(fieldAndValue)
                type = typeGuess[0].type
            }

            // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            if(!stringValue || (stringValue.length() == 4 && stringValue =~ /(?i)none|null(?-i)/) &&
                (type == FieldType.INTEGER || type == FieldType.LONG || type == FieldType.DOUBLE || type == FieldType.FLOAT)) {
                return
            }

            Object keyValue = ImportUtilities.convertValueToType(stringValue, type, dateFormat)
            if(keyValue instanceof ConversionFailureResult) {
                record.put(key, stringValue)
                toReturn.failedFields.push([name: key, type: type] as FieldTypePair)
            } else {
                record.put(key, keyValue)
            }
        }

        toReturn.put("record", record)
        return toReturn
    }

    /**
     * Converts values in a list of FieldTypePairs to the types given in recordValues and puts them in the DB record provided
     * @param ftPairs List of FieldTypePairs to convert
     * @param recordValues Map of fields and their associated value
     * @param record Mongo DB object containing the record to add values to
     * @param addFailedToRecord Set to true if the string value of a pair should be added to the record if the field failed to convert
     * @param dateFormat A date formatting string to be used when attempting to convert a field into a Date. This can be null.
     * @return Map with the keys 'record' (containing the DB object record that values were added to) and 'failedFields' (fields that
     * failed to convert)
     */
    private Map convertPairsFromFieldTypePairs(List<FieldTypePair> ftPairs, Object recordValues, DBObject record, Boolean addFailedToRecord, String dateFormat) {
        def toReturn = [:]
        toReturn.put('failedFields', [])

        ftPairs.each { pair ->
            def stringValue = recordValues.get(pair.name) as String

            // Don't add anything if the field is null, an empty string, or a "non-existent" value for numeric types.
            if(!stringValue || (stringValue.length() == 4 && stringValue =~ /(?i)none|null(?-i)/) &&
                (pair.type == FieldType.INTEGER || pair.type == FieldType.LONG || pair.type == FieldType.DOUBLE || pair.type == FieldType.FLOAT)) {
                return
            }

            Object convertedValue = ImportUtilities.convertValueToType(stringValue, pair.type, dateFormat)

            if(convertedValue instanceof ConversionFailureResult) {
                if(addFailedToRecord) {
                    record.put(pair.name, stringValue)
                }
                toReturn.failedFields.push([name: pair.name, type: pair.type] as FieldTypePair)
            } else {
                record.put(pair.name, convertedValue)
            }
        }

        toReturn.put("record", record)
        return toReturn
    }

    /**
     * Helper method that adds information about user-given data into a meta-database. This allows the ability to track which databases
     * are user-added, as well as provide some information about them at a later time if needed.
     * @param mongo The mongo instance in which to store the meta-database.
     * @param file The GridFSDBFile whose metadata contains the information that will be more permanently stored in the meta-database.
     */
    private void addMetadata(MongoClient mongo, GridFSDBFile file) {
        DB metaDatabase = mongo.getDB(ImportUtilities.MONGO_META_DB_NAME)
        DBCollection metaCollection = metaDatabase.getCollection(ImportUtilities.MONGO_META_COLL_NAME)
        DBObject metaRecord = new BasicDBObject()
        metaRecord.append("userName", file.get("userName"))
        metaRecord.append("prettyName", file.get("prettyName"))
        metaRecord.append("databaseName", getDatabaseName(file.get("userName"), file.get("prettyName")))
        metaCollection.insert(metaRecord)
        updateFile(file, [complete: true])
    }

    /**
     * Helper method that converts a list of FieldTypePairs to a map from name to type, and converts type from a FieldType
     * to a String if typeToString is true (used because mongo doesn't know how to deserialize FieldTypePairs for storage but
     * does know how to deserialize maps).
     * @param ftPairs A list of FieldTypePairs to convert to a map.
     * @param typeToString Set to true if the FieldTypePairs types should be converted to a String
     * @return A map whose keys are the names of the input FieldTypePairs and whose values are their corresponding types.
     */
    private Map fieldTypePairListToMap(List ftPairs, Boolean typeToString) {
        Map fieldTypePairMap = [:]
        ftPairs.each { ftPair ->
            def type = (typeToString) ? ftPair.type as String : ftPair.type
            if(ftPair.type == FieldType.OBJECT) {
                def objFtPair = [type, fieldTypePairListToMap(ftPair.objectFTPairs, typeToString)]
                fieldTypePairMap.put(ftPair.name, objFtPair)
            } else {
                fieldTypePairMap.put(ftPair.name, type)
            }
        }
        return fieldTypePairMap
    }

    /**
     * Helper method that determines whether there is a already a database in mongo for the information in the given file. If there is
     * already a database whose name is the same as the name that would be generated by the given file's username and "pretty" name, and if that
     * database has a collection in it with the name given to user-imported collections, and if that collection has at least one record in it,
     * returns true. Else, returns false.
     * @param nomgo The mongo instance in which to search.
     * @param file The GridFSDBFile whose metadata contains the username and "pretty" name associated with the database to look for.
     * @return True if the conditions above are met, or false otherwise.
     */
    private boolean alreadyLoaded(MongoClient mongo, GridFSDBFile file) {
        DBCollection collection = mongo.getDB(getDatabaseName(file.get("userName"), file.get("prettyName"))).getCollection(ImportUtilities.MONGO_USERDATA_COLL_NAME)
        return (collection && collection.count() > 0)
    }

    /**
     * Helper method that adds all of the key/value pairs in the given map to the given GridFSDBFile.
     * Made because mongodb's native putAll method doesn't appear to work.
     * @param file The GridFSDBFile to which to add values.
     * @param fieldsToUpdate A map containing key/value pairs to be added to the file.
     */
    private void updateFile(GridFSDBFile file, Map fieldsToUpdate) {
        fieldsToUpdate.each { key, value ->
            file.put(key as String, value)
        }
        file.save()
    }

    /**
     * Helper method that gets the "ugly", more unique database name created from the given username and "pretty", human-readable
     * database name. As part of this, removes any issue characters that would cause problems as part of a mongo database name.
     * @param userName The username associated with the ugly database name to be created.
     * @param prettyName The "pretty", human-readable database name associated with the ugly database name to be created.
     * @return The "ugly", more unique database name generatd from the given username and "pretty" database name.
     */
    private String getDatabaseName(String userName, String prettyName) {
        String safeUserName = userName.replaceAll(/^\$|[ \t\n\.]/, "_")
        String safePrettyName = prettyName.replaceAll(/[ \t\n\.]/, "_")
        return ImportUtilities.makeUglyName(safeUserName, safePrettyName)
    }
}