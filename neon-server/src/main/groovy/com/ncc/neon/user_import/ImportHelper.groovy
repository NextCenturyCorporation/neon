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

import org.apache.commons.io.LineIterator

/**
 * Adds, removes, and modifies fields of user-given data inside a generic data store.
 */
public interface ImportHelper {
    // Returns a map containing the job ID for this data - this ID can be used to reference this data in later methods.
    // [jobID: string]
    Map uploadFile(String host, String userName, String prettyName, String fileType, InputStream stream)

    // Returns a map containing information about the finding of type information for this data, as well as its job ID.
    // [complete: boolean, guesses: list of FieldTypePairs, jobID: string (the job ID that was given as input)]
    Map checkTypeGuessStatus(String host, String jobUUID)

    // Returns a map containing the job ID for this data.
    Map loadAndConvertFields(String host, String uuid, UserFieldDataBundle bundle)

    // Returns a map containing information about the status of uploading this data into a database, as well as its job ID.
    // [complete: boolean, numFinished: integer, failedFields: list, jobID: string]
    Map checkImportStatus(String host, String uuid)

    // Drops a dataset that has been loaded into a database. Returns a map containing whether or not the operations succeeded.
    // [success: boolean]
    Map dropDataset(String host, String userName, String prettyName)
}