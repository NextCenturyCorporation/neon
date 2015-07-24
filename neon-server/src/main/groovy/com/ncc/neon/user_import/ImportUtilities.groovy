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

import org.apache.commons.io.LineIterator
import org.apache.commons.lang.time.DateUtils

import groovy.transform.InheritConstructors

/**
 * Provides a number of non-database-specific variables and functions useful for import functionality.
 */
class ImportUtilities {

/*
 * ===============================================================================================================================
 * Variables to store settings for import - declared here so modifying them if necessary can be easily done.
 * ===============================================================================================================================
 */

    // Maximum allowed length of a single line. Determines when the line getter should stop atempting to grab lines from the stream to complete a row.
    static final int MAX_ROW_LENGTH = 500000

    // Number of records to pull from a database forthe purposes of checking types.
    static final int NUM_TYPE_CHECKED_RECORDS = 100

    // Currently, database "ugly" names are just user name, then a separator, then pretty name. This defines the separator.
    static final String SEPARATOR = "~"

    // Various mongo-specific values. Defines the database and collection name in which to store information about user-defined data, as well as
    // the collection name in which to put user-defined data (since every user-defined data set is given its own database).
    static final String MONGO_UPLOAD_DB_NAME = "Uploads"
    static final String MONGO_META_DB_NAME = "customDataInfo"
    static final String MONGO_META_COLL_NAME = "customDataInfo"
    static final String MONGO_USERDATA_COLL_NAME = "Data"

    // The list of default date pattern strings to use when attempting to convert date strings to date objects.
    // For information on the format of date pattern strings, check here: http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
    static final String[] DATE_PATTERNS = [
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss.SSS"
    ]

/*
 * ===============================================================================================================================
 * Helper and convenience methods that can be used by ImportHelpers for various databsase types.
 * ===============================================================================================================================
 */

    /**
     * Gets the next complete row of a spreadsheet pointed to by the given LineIterator. Throws an exception if there is no next
     * complete (non-malformed) row, or if the next complete row exceeds a number of characters defined by MAX_ROW_LENGTH. 
     * This length check is a protection against attempting to load an entire sheet into memory while searching for the end of a malformed
     * cell, but note that it will not work if the length limit is exceeded before ever encountering a newline character - in that case, the
     * LineIterator given as a parameter will simply continue to read until it hits the end of the file, hits a new line, or runs out of
     * memory if the string it is writing to gets too large.
     * @param iter The LineIterator pointing to the spreadsheet from which to get the next row.
     * @return The next complete row of the spreadsheet pointed to by the given LineIterator.
     */
    static String getNextWholeRow(LineIterator iter) {
        if(!iter.hasNext()) {
            return null
        }
        String line = iter.next()
        while(countSpecificChar(line as char[], '"' as char) % 2 != 0) {
            if(iter.hasNext() && line.length() < MAX_ROW_LENGTH) {
                // next() strips out the newline character - if it was in the middle of a row, we want to add it back in.
                line = line + "\n" + iter.next()
            }
            else {
                throw new BadSheetException("Invalid data. End of file or maximum legal row length reached with a cell left unterminated (no ending quotation mark).")
            }
        }
        return line
    }

    /**
     * Splits a string containing the contents of one row of a spreadsheet into multiple cells. Cell boundaries are determined by a given delineator string,
     * and a char is given as the start and end point of individual cells. E.g. in the row ["Hello", "goodbye"\n] comma would be the delineator, and
     * quotation marks would be the cell start and end point character. Comma and quotation marks are the default values for these, respectively.
     * @param lne The spreadsheet row to split into cells.
     * @param cellDelineator The string that signifies transition from one cell to the next. Defaults to a comma.
     * @param beginEndCellChar The character used to singify the beginning and ending of a single cell. Defaults to a quotation mark.
     * @return A list of the individual cells in the given row.
     */
    static List getCellsFromRow(String line, String cellDelineator = ',', char beginEndCellChar = '"' as char) {
        List cells = line.split(cellDelineator)
        for(int x = cells.size() - 1; x >= 0; x--) {
            if(countSpecificChar(cells.get(x) as char[], beginEndCellChar) % 2 != 0) {
                // If the split occured in the middle of a cell, stick the parts together and add the delineator back in.
                cells.set(x - 1, cells.get(x - 1) + cellDelineator + cells.remove(x))
            }
        }
        return cells
    }


    /**
     * Counts the instances of a given char in a char array.
     * @param letters The char array in which to search for the given character.
     * @param delineator The character to search for in the given array.
     * @return The number of times the specified character was found in the given char array.
     */
    static int countSpecificChar(char[] letters, char delineator) {
        int count = 0
        for(int x = 0; x < letters.length; x++) {
            if(letters[x] == delineator) {
                count++
            }
        }
        return count
    }

    /**
     * Takes a map of field names to lists of values for those field names (e.g. [grade: ['A', 'B', 'C', 'D'], gpaValue: ['4', '3', '2', '1']])
     * and attempts to determine what type of data each list of field values contains. Returns a list of {@link FieldTypePair}s, which simply
     * relate field name to the guessed type of data in that field.
     *
     * This method assumes that the values given in the lists are strings - it may work if they are other types of data, but is not guaranteed to.
     * Possible types to check for are:
     * Integer
     * Long
     * Double
     * Float
     * Date
     * String (the default, if none of the others are valid)
     * @param fieldsAndValues A map of field names to lists of values for the field of that name.
     * @param return Type The type of object to return the results as. The default is a list, but it can also handle maps from name to type.
     * @return A list of FieldTypePairs, which relate field names to guessed type of data for those field names.
     */
    static Object getTypeGuesses(Map fieldsAndValues, String returnType = "list") {
        List fields = fieldsAndValues.keySet() as List
        List fieldsAndTypes = []
        fields.each { field ->
            FieldTypePair pair = null
            List valuesOfField = fieldsAndValues.get(field)
            pair = (!pair && ImportUtilities.isListIntegers(valuesOfField)) ? new FieldTypePair(name: field, type: "Integer") : pair
            pair = (!pair && ImportUtilities.isListLongs(valuesOfField)) ? new FieldTypePair(name: field, type: "Long") : pair
            pair = (!pair && ImportUtilities.isListDoubles(valuesOfField)) ? new FieldTypePair(name: field, type: "Double") : pair
            pair = (!pair && ImportUtilities.isListFloats(valuesOfField)) ? new FieldTypePair(name: field, type: "Float") : pair
            pair = (!pair && ImportUtilities.isListDates(valuesOfField)) ? new FieldTypePair(name: field, type: "Date") : pair
            pair = (!pair) ? new FieldTypePair(name: field, type: "String") : pair
            fieldsAndTypes.add(pair)
        }
        if(returnType == "map") {
            Map m = [:]
            fieldsAndTypes.each { ftPair ->
                m.put(ftPair.name, ftPair.type)
            }
            return m
        }
        return fieldsAndTypes
    }

    /**
     * Checks whether or not a list of objects can be converted to integers. Returns true if every object can be, or false otherwise.
     * @param list The list of objects to check.
     * @return Whether or not all objects in the list can be converted to integers.
     */
    static boolean isListIntegers(List list) {
        try {
            list.each { value ->
                if(value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) {
                    return
                }
                Integer.parseInt(value)
            }
        }
        catch (NumberFormatException e) {
            return false
        }
        return true
    }


    /**
     * Checks whether or not a list of objects can be converted to longs. Returns true if every object can be, or false otherwise.
     * @param list The list of objects to check.
     * @return Whether or not all objects in the list can be converted to longs.
     */
    static boolean isListLongs(List list) {
        try {
            list.each { value ->
                if(value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) {
                    return
                }
                Long.parseLong(value)
            }
        }
        catch (NumberFormatException e) {
            return false
        }
        return true
    }


    /**
     * Checks whether or not a list of objects can be converted to doubles. Returns true if every object can be, or false otherwise.
     * @param list The list of objects to check.
     * @return Whether or not all objects in the list can be converted to doubles.
     */
    static boolean isListDoubles(List list) {
        try {
            list.each { value ->
                if(value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) {
                    return
                }
                Double.parseDouble(value)
            }
        }
        catch (NumberFormatException e) {
            return false
        }
        return true
    }

    /**
     * Checks whether or not a list of objects can be converted to floats. Returns true if every object can be, or false otherwise.
     * @param list The list of objects to check.
     * @return Whether or not all objects in the list can be converted to floats.
     */
    static boolean isListFloats(List list) {
        try {
            list.each { value ->
                if(value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) {
                    return
                }
                Float.parseFloat(value)
            }
        }
        catch (NumberFormatException e) {
            return false
        }
        return true
    }

    /**
     * Checks whether or not a list of strings can be converted to dates. Returns true if every string can be, or false otherwise.
     * @param list The list of strings to test.
     * @return Whether or not all strings in the list can be converted to dates.
     */
    static boolean isListDates(List list) {
        try {
            list.each { value ->
                DateUtils.parseDate(value, DATE_PATTERNS)
            }
        } catch (Exception e) {
            return false
        }
        return true
    }

    // TODO add support for arrays. Also, do we want to throw an exception on unsupported type rather than default to string?
    /**
     * Attempts to convert the given object to the given type. Has support for integer, long, double, float, date, and string conversion.
     * If an unsupported type is given, defaults to string conversion. Also takes an optional string array containing date string matchers,
     * for conversion to date.
     * @param value The object to attempt to convert.
     * @param type The type to which to attempt to convert the given object.
     * @param datePatterns An optional parameter giving a list of date strings to use when attempting to convert to a date. Defaults to null.
     * @return The given input value, converted to the given type or to a string if the given type is not valid.
     */
    static Object convertValueToType(Object value, String type, String[] datePatterns = null) {
        try {
            switch(type) {
                case "Integer":
                    return Integer.parseInt(value)
                    break
                case "Long":
                    return Long.parseLong(value)
                    break
                case "Double":
                    return Double.parseDouble(value)
                    break
                case "Float":
                    return Float.parseFloat(value)
                    break
                case "Date":
                    return DateUtils.parseDate(value, datePatterns ?: DATE_PATTERNS)
                    break
                default:
                    return value.toString()
            }
        }
        catch(Exception e) {
            return null
        }
    }

    /**
     * Takes a username and "pretty" human-readable name and uses them to generate an ugly, more unique name.
     * @param userName The username to use in making the ugly name.
     * @param pretttyName The human-readable name to use in making the ugly name.
     * @return The ugly name created from the given username and pretty name.
     */
    static String makeUglyName(String userName, String prettyName) {
        return "$userName$SEPARATOR$prettyName"
    }
}
