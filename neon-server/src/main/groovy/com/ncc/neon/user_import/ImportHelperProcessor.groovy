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

import org.springframework.scheduling.annotation.Async

/**
 * Provides asynchronous methods for use by import helpers to work on a data store.
 */
public interface ImportHelperProcessor {

	// Gets the first few records (either the number designated as NUM_TYPE_CHECKED_RECORDS in ImportUtilities or  the number of records in the database,
	// whichever is smaller) from a GridFSFile and parses through them to guess the types of their fields. Stores these guesses as metadata in the
	// GridFSFile once it's done.
	@Async
	void processTypeGuesses(String host, String uuid)

	// Grabs a data set from a GridFSFile and parses through it, putting it into its own database and converting the fields of records to types chosen
	// by the user as it goes. When it's done, it marks the GridFSFile as completed uploading and stores the names and given types of any fields that
	// failed to be converted as metadata in the GridFSFile (e.g. if the user said to convert field X to an Integer, but one entry had X = "hello",
	// [X: "Integer"] would be stored in the GridFSFile).
	@Async
	void processLoadAndConvert(String host, String uuid, String dateFormat, List<FieldTypePair> fieldTypePairs)
}