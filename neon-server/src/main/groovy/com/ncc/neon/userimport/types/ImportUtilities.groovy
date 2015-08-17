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

package com.ncc.neon.userimport.types

import java.text.ParseException

import org.apache.commons.lang.time.DateUtils

/**
 * Provides a number of non-database-specific variables and functions useful for import functionality. In addition, can hold variables that
 * define what amounts to config info for various database types.
 */
class ImportUtilities {

/*
 * ===============================================================================================================================
 * Variables to store settings for import - declared here so modifying them if necessary can be easily done.
 * ===============================================================================================================================
 */

    // Maximum allowed length of a single row. Determines when the row getter should stop atempting to grab lines from the stream to complete a row.
    // Note that the row getter only compares length to this value on encountering a newline character.
    static final int MAX_ROW_LENGTH = 500000

    // Number of records to pull from a database for the purposes of determining types.
    static final int NUM_TYPE_CHECKED_RECORDS = 100

    // Currently, database "ugly" (backend - what they're called on the machine, out of the users' view) names are just user name, then
    // a separator, then pretty name. This defines the separator.
    static final String SEPARATOR = "~"

    // Various mongo-specific values. Defines the name of the database and collection in which to store information about user-defined data, the
    // name of the GridFS database in which to store raw files, and the collection name in which to put user-defined data (since every user-defined
    // data set is given its own database).
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
     * Takes a map of field names to lists of values for those field names (e.g. [grade: ['A', 'B', 'C', 'D'], gpaValue: ['4', '3', '2', '1']])
     * and attempts to determine what type of data each list of field values contains. Returns a list of {@link FieldTypePair}s, which simply
     * relate field name to the guessed type of data in that field.
     * This method assumes that the values given in the lists are strings - it may work correctly if they are not, but is not guaranteed to.
     * Possible types to check for are: Integer, Long, Double, Float, Date, and String (the default, if none of the others are valid)
     * @param fieldsAndValues A map of field names to lists of values for the field of that name.
     * @param return Type The type of object to return the results as. The default is a list, but it can also handle maps from name to type.
     * @return A list of FieldTypePairs, which relate field names to guessed type of data for those field names.
     */
    static List getTypeGuesses(Map fieldsAndValues) {
        List fields = fieldsAndValues.keySet() as List
        List fieldsAndTypes = []
        fields.each { field ->
            FieldTypePair pair = null
            List valuesOfField = fieldsAndValues.get(field)
            pair = (!pair && ImportUtilities.isListIntegers(valuesOfField)) ? new FieldTypePair(name: field, type: FieldType.INTEGER) : pair
            pair = (!pair && ImportUtilities.isListLongs(valuesOfField)) ? new FieldTypePair(name: field, type: FieldType.LONG) : pair
            pair = (!pair && ImportUtilities.isListDoubles(valuesOfField)) ? new FieldTypePair(name: field, type: FieldType.DOUBLE) : pair
            pair = (!pair && ImportUtilities.isListFloats(valuesOfField)) ? new FieldTypePair(name: field, type: FieldType.FLOAT) : pair
            pair = (!pair && ImportUtilities.isListDates(valuesOfField)) ? new FieldTypePair(name: field, type: FieldType.DATE) : pair
            pair = (!pair) ? new FieldTypePair(name: field, type: FieldType.STRING) : pair
            fieldsAndTypes.add(pair)
        }
        return fieldsAndTypes
    }

    /**
     * Checks whether or not a list of strings can be converted to integers. Returns true if every object can be, or false otherwise.
     * @param list The list of strings to check.
     * @return Whether or not all strings in the list can be converted to integers.
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
     * Checks whether or not a list of strings can be converted to longs. Returns true if every object can be, or false otherwise.
     * @param list The list of strings to check.
     * @return Whether or not all strings in the list can be converted to longs.
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
     * Checks whether or not a list of strings can be converted to doubles. Returns true if every object can be, or false otherwise.
     * @param list The list of strings to check.
     * @return Whether or not all strings in the list can be converted to doubles.
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
     * Checks whether or not a list of strings can be converted to floats. Returns true if every object can be, or false otherwise.
     * @param list The list of strings to check.
     * @return Whether or not all strings in the list can be converted to floats.
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
        } catch (IllegalArgumentException | ParseException e) {
            return false
        }
        return true
    }

    /**
     * Attempts to convert the given object to the given type. Has support for integer, long, double, float, date, and string conversion.
     * If an unsupported type is given, defaults to string conversion. Also takes an optional string array containing date string matchers,
     * for conversion to date.
     * @param value The object to attempt to convert.
     * @param type The type to which to attempt to convert the given object.
     * @param datePatterns An optional parameter giving a list of date strings to use when attempting to convert to a date. Defaults to null.
     * If no list of strings is given, defaults to using a list of date strings defined by DATE_PATTERNS.
     * @return The given input value, converted to the given type or to a string if the given type is not valid. If an invalid type is
     * given - e.g. value = "Hello" and type = "Integer" - returns a ConversionFailureResult containing the given value and type.
     */
    static Object convertValueToType(Object value, FieldType type, String[] datePatterns = null) {
        try {
            switch(type) {
                case FieldType.INTEGER:
                    return Integer.parseInt(value)
                case FieldType.LONG:
                    return Long.parseLong(value)
                case FieldType.DOUBLE:
                    return Double.parseDouble(value)
                case FieldType.FLOAT:
                    return Float.parseFloat(value)
                case FieldType.DATE:
                    return DateUtils.parseDate(value, datePatterns ?: DATE_PATTERNS)
                case FieldType.STRING:
                default:
                    return value.toString()
            }
        }
        catch(NumberFormatException | IllegalArgumentException | ParseException e) {
            return new ConversionFailureResult([value: value, type: type])
        }
    }

    /**
     * Takes a username and "pretty" human-readable name and uses them to generate an "ugly", more unique name.
     * @param userName The username to use in making the ugly name.
     * @param pretttyName The human-readable name to use in making the ugly name.
     * @return The ugly name created from the given username and pretty name.
     */
    static String makeUglyName(String userName, String prettyName) {
        return "$userName$SEPARATOR$prettyName"
    }
}
