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

/**
 * Adds, removes, and modifies fields of user-given data inside a generic data store.
 */
public interface ImportHelper {
    /**
     * Loads the file pointed to by the given stream as-is into a data store on the given host, and also stores
     * user name, "pretty" (human-readable) database name, file type, and a unique "job ID" associated with it. Then
     * begins an asynchronous process that searches for fields in the file and stores a list of the ones it found - as
     * well as guesses as to what types of data they are - in the data store to be retrieved later.
     * Returns a map containing the job ID created for the file, to be used as a way to refer to that
     * specific file in later methods.
     * @param host The host on which the data store to use is running.
     * @param userName The username given by the user to be associated with this file.
     * @param prettyName the "pretty" database name given by the user to be associated with this file.
     * @param fileType The type of file being stored, e.g. "csv". Used to determine how to parse the file later.
     * @param stream The stream holding the file's data to be read.
     * @return A map, of the form [jobID: (String)], where the string value is the uuid generated for this file.
     */
    Map uploadFile(String host, String userName, String prettyName, String fileType, InputStream stream)

    /**
     * Checks in on the status of the asynchronous task finding and guessing the type of fields in the file associated with
     * the given job ID. Returns a map containing whether the finding and guessing is complete, a list of guesses if it is,
     * and the same job ID that was given as input.
     * @param host The host on which the data store to use is running.
     * @param uuid The job ID associated with the file whose fields are being found.
     * @return A map, of the form [complete: (boolean), guesses (List<FieldTypePair>), jobID: (String)], where the boolean value
     * is true if the finding and guessing of field types is complete, the list of FieldTypePairs contains the fields found and
     * the types they are guessed to be, and the string is the job ID associated with the file whose field type guesses are being
     * checked on.
     */
    Map checkTypeGuessStatus(String host, String uuid)

    /**
     * Launches an asynchronous process that uses the information in the given UserFieldDataBundle as a guide as it takes the file
     * associated with the given job ID and parses out individual records, converting their fields into the appropriate types as it
     * does, and stores those records in a database, using the file's associated username and pretty name to name the database. If the
     * file has already been loaded into a database - that is, if a database with a name identical to the one which would be created
     * from the file's username and pretty name metadata already exists - the asynchronous process simply converts the fields of records
     * in that database to the appropriate types, rather than trying to load the records in a second time. This is relevant in cases
     * where the user gave incorrect information about what type one or more fields was, and so conversion needs to be attempted
     * more than once.
     * Returns a map containing the job ID associated with the file being processed, which can be used to check on the state of the
     * processing.
     * @param host The host on which the data store to use is running.
     * @param uuid The job ID associated with the file being processed.
     * @param bundle A UserFieldDataBundle containing a date string for the asynchronous task to use when attempting to parse dates
     * (this string is allowed to be null) as well as a list of FieldTypePairs, defining the names of fields the records to be
     * inserted should have and the types they should be converted to.
     * @return A map, of the form [jobID: (String)], where the string value is the job ID associated with this file.
     */
    Map loadAndConvertFields(String host, String uuid, UserFieldDataBundle bundle)

    /**
     * Checks in on the status of the asynchronous task handling the loading and converson of records found in the file associated
     * with the given job ID. Returns a map containing whether the processing is complete, how many records have been processed, a
     * list of fields that failed to convert, and the same job ID that was given as input.
     * @param host The host on which the data store to use is running.
     * @param uuid The job ID associated with the file being processed.
     * @return A map, of the form [complete: (boolean), numFinished: (int), failedFields: (List<FieldTypePair>), jobID: (String)],
     * where the boolean value is whether or not the file's records are done being added into a database and converted, the int
     * is how many records have been added to the database and converted (-1 if there is no file associated with the given job ID),
     * the list of FieldtypePairs contains the fields that failed to convert for at least one record as they were being added to the
     * database, and the string value is the job ID associated with the file whose progress is being checked on.
     */
    Map checkImportStatus(String host, String uuid)

    /**
     * Drops the database associated with the given username and "pretty" name, removing it from the data store. Returns a map
     * containing a success value -true if dropping the database was a success, or false if it didn't exist or failed for some
     * other reason.
     * @param host The host on which the data store containing the database to be dropped is running.
     * @param userName The username associated with the database to be dropped.
     * @param prettyName The "Pretty" human-readable name associated with the database to be dropped.
     * @return A map, of the form [success: (boolean)], where the boolean value is whether or not dropping the database was
     * successful. If true, the database was dropped. If false, the database either did not exist, or the drop failed for some
     * other reason.
     */
    Map dropDataset(String host, String userName, String prettyName)
}