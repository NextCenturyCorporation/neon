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

import org.junit.Before
import org.junit.Test


class ExcelSheetReaderTest {

    private ExcelSheetReader excelSheetReader

    @Before
    void before() {
        excelSheetReader = new ExcelSheetReader()
    }

    @Test
    void "initialize"() {
        SheetReader reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file.xlsx")))
        assert reader
        assert reader.fieldNames == ["name", "mother"]
        assert reader.sheet

        reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-just-headers.xlsx")))
        assert !reader
        assert excelSheetReader.fieldNames == ["name", "mother"]

        reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-empty.xlsx")))
        assert !reader
    }
    
    @Test
    void "close"() {
        SheetReader reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file.xlsx")))
        assert reader.iterator
        reader.close()
        assert !reader.iterator
        assert !reader.sheet
        // Shouldn't throw error
        reader.close()
    }

    @Test
    void "has next"() {
        SheetReader reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file.xlsx")))
        assert reader.hasNext()
        reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-just-headers.xlsx")))
        assert !reader
        // Get rid of header
        excelSheetReader.next()
        assert !excelSheetReader.hasNext()
        excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-empty.xlsx")))
        assert !excelSheetReader.hasNext()
        excelSheetReader.close()
        assert !excelSheetReader.hasNext()
    }

    @Test
    void "next"() {
        excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-quotes.xlsx")))
        assert excelSheetReader.next() == ["\"Joe\"", "Lisa"]
        excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file.xlsx")))
        assert excelSheetReader.next() == ["Joe", "Lisa"]
        assert excelSheetReader.next() == ["Janice", "Bob"]
        assert !excelSheetReader.hasNext()
    }

    @Test(expected=NoSuchElementException)
    void "next throws no such element exception"() {
        excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file-just-headers.xlsx")))
        // Get rid of header
        excelSheetReader.next()
        excelSheetReader.next()
    }

    @Test
    void "get sheet field names"() {
        SheetReader reader = excelSheetReader.initialize(new FileInputStream(new File("src/test-data/excel-files/test-file.xlsx")))
        assert reader.getSheetFieldNames() == reader.fieldNames
    }

}
