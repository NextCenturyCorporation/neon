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

/**
 * Abstract class, designed to read through a spreadsheet and return records from it.
 */
abstract class SheetReader implements Iterator<List> {

    /**
     * Sets up a SheetReader, given an InputString that points ot a spreadsheet of the
     * appropriate type for that reader. Sets up any structures behind-the-scenes needed
     * to read parse the spreadsheet. Then gets and storesthe field names of records in the spreadsheet
     * (often the first row of cells) and returns itself. If there were no cells in the spreadsheet,
     * or if there were only the field names and no actual records, closes resources and returns null.
     * @param stream The InputStream pointing to the spreadsheet to be read.
     * @return Itself, or null if there were no records in the file to read.
     */
    abstract SheetReader initialize(InputStream stream)

    /**
     * Closes the resources used by this reader. Should be idempotent, not causing any issues when called
     * multiple times.
     */
    abstract void close()

    /**
     * Determines whether or not there are any records left in the spreadsheet to be read.
     * @return Whether there are any records left in the spreadsheet ot be read. True if there are, false
     * otherwise.
     */
    abstract boolean hasNext()

    /**
     * Reads the next record contained in the spreadsheet, and returns a list of all field values, in the order
     * they were found. If any field values were not defined, null should be returned - those values should not
     * be skipped.
     * @return A list of the next record's values for every field found at the beginning of the spreadsheet, in order.
     */
    abstract List next()

    /**
     * Not supported.
     */
    void remove() {
        throw new UnsupportedOperationException()
    }
}