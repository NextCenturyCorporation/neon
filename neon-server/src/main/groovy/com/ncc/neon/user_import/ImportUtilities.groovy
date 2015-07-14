package com.ncc.neon.user_import

import org.apache.commons.io.LineIterator
import org.apache.commons.lang.time.DateUtils

import groovy.transform.InheritConstructors

class ImportUtilities {

/*
 * ===============================================================================================================================
 * Variables to store settings for import - declared here so modifying them if necessary can be easily done.
 * ===============================================================================================================================
 */

    // Maximum allowed length of a single line. Determines when the line getter should stop atempting to grab lines from the stream to complete a row.
	static final int MAX_ROW_LENGTH = 500000

	static final int NUM_TYPE_CHECKED_RECORDS = 100

    static final String MONGO_META_DB_NAME = "customDataInfo"

    static final String MONGO_META_COLL_NAME = "customDataInfo"

	static final String MONGO_USERDATA_COLL_NAME = "Data"

	static final String[] DATE_PATTERNS = [
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss.SSS"
    ]

/*
 * ===============================================================================================================================
 * Helper and convenience methods that can be used by ImportHelpers for various databsase types.
 * ===============================================================================================================================
 */

    static String getNextWholeRow(LineIterator iter) {
    	if(!iter.hasNext()) {
            return null
        }
        String line = iter.next()
        while(countSpecificChar(line as char[], '"' as char) % 2 != 0) {
        	// Length check prevents attempting to load entire sheet into memory while trying to finish a deformed cell.
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

    static List getCellsFromRow(String line, String cellDelineator = ',') {
        List cells = line.split(cellDelineator)
        for(int x = cells.size() - 1; x >= 0; x--) {
            if(countSpecificChar(cells.get(x) as char[], '"' as char) % 2 != 0) {
                // If the split occured in the middle of a cell, stick the parts together and add the delineator back in.
                cells.set(x - 1, cells.get(x - 1) + cellDelineator + cells.remove(x))
            }
        }
        return cells
    }

    static int countSpecificChar(char[] letters, char delineator) {
	    int count = 0
	    for(int x = 0; x < letters.length; x++) {
	        if(letters[x] == delineator) {
	            count++
	        }
	    }
	    return count
	}

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

	// TODO add support for arrays
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
                	return DateUtils.parseDate(value, (datePatterns) ?: DATE_PATTERNS)
                    break
                default:
                	return value.toString()
			}
		}
		catch(Exception e) {
			return null
		}
	}

/*
 * ===============================================================================================================================
 * Private classes.
 * ===============================================================================================================================
 */

	@InheritConstructors
    private class BadSheetException extends Exception {
    }
}















// Now go through and javadoc everything.