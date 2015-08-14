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

import com.monitorjbl.xlsx.StreamingReader
import com.monitorjbl.xlsx.impl.StreamingRow

/**
 * Implementation of Sheetreader designed to read records from XLSX files.
 */
class ExcelSheetReader extends SheetReader {

    private StreamingReader sheet
    private Iterator iterator
    private List fieldNames

    @Override
    SheetReader initialize(InputStream stream) {
        sheet = StreamingReader.builder().sheetIndex(0).read(stream)
        iterator = sheet.iterator()
        if(!hasNext()) {
            close()
            return null
        }
        fieldNames = next()
        return hasNext() ? this : null
    }

    @Override
    void close() {
        if(!sheet) {
            return
        }
        sheet.close()
        iterator = null
        sheet = null
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
        return cellsFromRow(nextRow(), fieldNames?.size() ?: -1)
    }

    /**
     * Helper method for next. Gets an entire row of the spreadsheet.
     * @return A row of the spreadsheet.
     */
    private StreamingRow nextRow() {
        return iterator.next()
    }

    /**
     * Helper method for next. Gets individual cells from a row in a spreadsheet. If numCells is
     * zero or greater, gets that many cells from left-to-right, regardless of whether or not they
     * are defined. If numCells is less than zero, simply gets every defined cell, regardless of
     * position.
     * @param row The row from which to get cells.
     * @param numCells How many cells to get from the row. If less than zero, gets every defined cell.
     * If greater than or equal to zero, gets that many cells - defined or undefined - from left-to-right.
     * @return A list of cells found in the row, in the order they were found.
     */
    private List cellsFromRow(StreamingRow row, int numCells) {
        List cells = []
        if(numCells < 0) {
            row.each { cell ->
                cells << cell.getContents().toString()
            }
        }
        else {
            for(int cell = 0; cell < numCells; cell++) {
                cells << row.getCell(cell)?.getContents()?.toString()
            }
        }
        return cells
    }

    /**
     * Returns the list of field names for records in this spreadsheet that was found during initialization.
     * @return The list of field names for records in this spreadsheet that was found during initialization.
     */
    List getSheetFieldNames() {
        return fieldNames
    }
}