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

import org.springframework.scheduling.annotation.Async

/**
 * Provides asynchronous methods for use by import helpers to work on a data store.
 */
public interface ImportHelperProcessor {
	/**
	 * Asynchronously ets the file on the given host associated with the given job ID, and looks at the first row to find the names of all the fields
	 * that records in that file could have. Also searches through the first several records (either the value of NUM_TYPE_CHECKED_RECORDS in
	 * ImportUtilities or every record in the file, whichever number is smaller) and makes guesses as to their types. It then stores these guesses
	 * in a map from field names to guessed types as metadata to the file itself.
	 * @param host The host on which the file to find fields and guess types for is located.
	 * @param uuid The job ID associated with the file for which to find fields and guess their types.
	 */
	@Async
	void processTypeGuesses(String host, String uuid)

	/**
	 * Asynchronously gets the file on the given host associated with the given job ID and parses through it for individual records, converting the
	 * fields of those records to the types given by the provided list of FieldTypePairs and then adding those records to a database. If any fields
	 * fail to convert properly, notes that in a list of FieldTypePairs which is stored in the file after all records have been processed. If a field
	 * fails to convert for one record, it will not attempt to convert for any record afterward. As this method progresses, it updates metadata in the
	 * file it's pulling from to tell how many records it has added and converted.
	 * If the file associated with the uuid passed to his method has already been added to a database - that is, if there is a database with the same
	 * name as this file's database would have - thismethod simply converts the records already in that database rather than adding them all over again.
	 * @param host The host on whic the file to be processed is stored.
	 * @param uuid The job ID associated with the file to be processed.
	 * @param dateFormat The date format string to be used when attempting to convert fields to Dates. This value can be null.
	 * @param fieldTypePairs a list of FieldTypePairs, telling the fields records in this file will have and what types to attempt to convert them to.
	 */
	@Async
	void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs)
}