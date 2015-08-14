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

package com.ncc.neon.userimport.readers

import com.ncc.neon.userimport.ImportUtilities
import com.ncc.neon.userimport.exceptions.BadSheetException

import org.apache.commons.io.IOUtils
import org.apache.commons.io.LineIterator

/**
 * Implementation of Sheetreader designed to read records from CSV files.
 */
class CSVSheetReader extends SheetReader {

    private LineIterator iterator
    private List fieldNames

    @Override
    SheetReader initialize(InputStream stream) {
        iterator = IOUtils.lineIterator(stream, "UTF-8")
        if(!hasNext()) {
            close()
            return null
        }
        fieldNames = next()
        return hasNext() ? this : null
    }

    @Override
    void close() {
        if(!iterator) {
            return
        }
        iterator.close()
        iterator = null
    }

    @Override
    boolean hasNext() {
        return iterator.hasNext()
    }

    @Override
    List next() {
        if(!hasNext()) {
            throw new NoSuchElementException("No more rows left to read.")
        }
        return cellsFromRow(nextRow())
    }

    /**
     * Heer method for next. Gets the next complete row of a spreadsheet. Throws an exception if there is no next complete, non-malformed
     * row, or if the next complete row exceeds a number of characters defined by ImportUtilities.MAX_ROW_LENGTH. This length check is
     * a protection against attempting to load an entire sheet into memory while searching for the end of a malformed cell, but note that
     * it will not work if the length limit is exceeded before ever encountering a newline character - in that case, the LineIterator used
     * to scan the file will simply continue to read until it hits the end of the file, hits a new line, or runs out of memory if the
     * string it is writing to gets too large.
     * @return The next complete row of the spreadsheet pointed to by the given LineIterator.
     */
    private String nextRow() {
        if(!hasNext()) {
            return null
        }
        String row = iterator.next()
        while(countSpecificChar(row, '\"' as char) % 2 != 0) {
            if(hasNext() && row.length < ImportUtilities.MAX_ROW_LENGTH) {
                row = row + "\n" + iterator.next()
            }
            else {
                throw new BadSheetException("End of file or maximum legal row length reached with a cell left unterminated (no ending quotation mark).")
            }
        }
        return row
    }

    /**
     * Helper method for next. Splits a string containing the contents of one row of a spreadsheet into multiple cells. Cell boundaries are determined
     * by a given delineator string, and a char is given as the start and end point of individual cells. E.g. in the row ["Hello", "goodbye"\n] comma
     * would be the delineator, and quotation marks would be the cell start and end point character. Comma and quotation marks are the default values
     * for these, respectively.
     * @param lne The spreadsheet row to split into cells.
     * @param cellDelineator The string that signifies transition from one cell to the next. Defaults to a comma.
     * @param beginEndCellChar The character used to signify the beginning and ending of a single cell. Defaults to a quotation mark.
     * @return A list of the individual cells in the given row.
     */
    private List cellsFromRow(String row, String cellDivider = ",", char textDelimiter = "\"" as char) {
        if(!row) {
            return null
        }
        List cells = row.split(cellDivider)
        for(int step = cells.size() - 1; step >= 0; step--) {
            if(countSpecificChar(cells.get(step), textDelimiter) % 2 != 0) {
                // If the split occured in the middle of a cell, stick the parts together and add the divider back in.
                cells.set(step - 1, cells.get(step - 1) + cellDivider + cells.remove(step))
            }
        }
        return cells
    }

    /**
     * Counts the instances of a given character in a string.
     * @param letters The string in which to search for the given character.
     * @param delineator The character to search for in the given array.
     * @return The number of times the specified character was found in the given string.
     */
    private int countSpecificChar(String string, char charToCount) {
        char[] chars = string as char[]
        int count = 0
        for(int character = 0; character < chars.length; character++) {
            if(chars[character] == charToCount) {
                count++
            }
        }
        return count
    }

    /**
     * Returns the list of field names for records in this spreadsheet that was found during initialization.
     * @return The list of field names for records in this spreadsheet that was found during initialization.
     */
    List getSheetFieldNames() {
        return fieldNames
    }
}