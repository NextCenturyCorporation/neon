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
    /**
     * Takes a LineIterator that points to a spreadsheet, and adds the data from the spreadsheet
     * into a data store running at the given host, with the given associated user and pretty name
     * for the added data. Also guesses at the data type of each field found for entries in the
     * spreadsheet, and returns those guesses on completion.
     * @param host The host on which the data store is running.
     * @param userName The user name with which to associate the added data.
     * @param prettyName The "pretty" database name with which to associate the added data.
     * @param fileType The type of spreadsheet containing the data to be added.
     * @param stream An InputStream containing the data that should be added.
     * @return A list of {@link FieldTypePair} objects, containing record fields and guesses as to their types.
     * If an error occurs, returns a one-element list of strings describing the error.
     */
    List uploadData(String host, String userName, String prettyName, String fileType, InputStream stream)

    /**
     * Drops a set of user-given data from a data store, given its associated username and pretty name.
     * @paramhost The host on which the data store is running.
     * @param userName The user name associated with the data to be dropped.
     * @param prettyName the "pretty" database name associated with the data to be dropped.
     * @return True if the data was successfully dropped, false (or an exception) otherwise.
     */
    boolean dropData(String host, String userName, String prettyName)

    /**
     * Given a {@link UserFieldDataBundle} (a date pattern string and a list of fields and corresponding types),
     * attempts to convert the given fields in the given database to their given type. Any fields that cannot be
     * converted to that type are added to a list and ignored, and the list of fields that failed to convert is
     * returned.
     * @param host The host on which the data store is running.
     * @param userName The username associated with the database within the data store.
     * @param prettyName The "pretty" database name associated with the database within the data store.
     * @param bundle A bundle of information containing a list of fields and corresponding conversion types, as
     * well as a date format string that describes the format in which to attempt to parse dates. If this date
     * format string is null, uses the default list of date strings instead.
     * @return The list of fields that failed to convert and what type they were attempting to convert to. If there
     * was an issue accessing the database or table, returns null.
     */
    List convertFields(String host, String userName, String prettyName, UserFieldDataBundle bundle)
}