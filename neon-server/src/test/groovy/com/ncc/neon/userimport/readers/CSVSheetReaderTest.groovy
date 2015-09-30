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

import com.ncc.neon.userimport.types.ImportUtilities

import com.ncc.neon.userimport.exceptions.BadSheetException

import org.junit.Before
import org.junit.Test


class CSVSheetReaderTest {

    private CSVSheetReader csvSheetReader

    @Before
    void before() {
        csvSheetReader = new CSVSheetReader()
    }

    @Test
    void "initialize"() {
        SheetReader reader = csvSheetReader.initialize(new StringBufferInputStream("name,mother\nJoe,Lisa\nJanice,Bob"))
        assert reader
        assert reader.fieldNames == ["name", "mother"]

        reader = csvSheetReader.initialize(new StringBufferInputStream("name,mother"))
        assert !reader
        assert csvSheetReader.fieldNames == ["name", "mother"]

        reader = csvSheetReader.initialize(new StringBufferInputStream(""))
        assert !reader
    }

    @Test
    void "close"() {
        SheetReader reader = csvSheetReader.initialize(new StringBufferInputStream("name,mother\nJoe,Lisa\nJanice,Bob"))
        assert reader.iterator
        reader.close()
        assert !reader.iterator
        // Shouldn't throw error
        reader.close()
    }

    @Test
    void "has next"() {
        SheetReader reader = csvSheetReader.initialize(new StringBufferInputStream("name,mother\nJoe,Lisa\nJanice,Bob"))
        assert reader.hasNext()
        csvSheetReader.initialize(new StringBufferInputStream("name,mother"))
        assert !csvSheetReader.hasNext()
        csvSheetReader.initialize(new StringBufferInputStream(""))
        assert !csvSheetReader.hasNext()
        csvSheetReader.close()
        assert !csvSheetReader.hasNext()
    }

    @Test
    void "next"() {
        csvSheetReader.initialize(new StringBufferInputStream("name,mother\n\"Joe\",Lisa\nJanice,Bob"))
        assert csvSheetReader.next() == ["\"Joe\"", "Lisa"]
        csvSheetReader.initialize(new StringBufferInputStream("name,mother\nJoe,Lisa\nJanice,Bob"))
        assert csvSheetReader.next() == ["Joe", "Lisa"]
        assert csvSheetReader.next() == ["Janice", "Bob"]
        assert !csvSheetReader.hasNext()
    }

    @Test(expected=BadSheetException)
    void "next throws bad sheet exception for malformed data"() {\
        csvSheetReader.initialize(new StringBufferInputStream("name,mother\n\"Joe,Lisa\nJanice,Bob"))
        // Should throw a BadSheetException due to malformed data (specifically "\"Joe")
        csvSheetReader.next()
    }

    @Test(expected=BadSheetException)
    void "next throws bad sheet exception for malformed data row length"() {
        csvSheetReader.initialize(new StringBufferInputStream("name,mother\n\"" + ('l' * (ImportUtilities.MAX_ROW_LENGTH))))
        // Should throw a BadSheetException due to row length being too long on malformed data
        csvSheetReader.next()
    }

    @Test(expected=NoSuchElementException)
    void "next throws no such element exception"() {
        csvSheetReader.initialize(new StringBufferInputStream("name,mother"))
        csvSheetReader.next()
    }

    @Test
    void "get sheet field names"() {
        SheetReader reader = csvSheetReader.initialize(new StringBufferInputStream("name,mother\nJoe,Lisa\nJanice,Bob"))
        assert reader.getSheetFieldNames() == reader.fieldNames
    }

}
