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

import org.junit.Before
import org.junit.Test


class ImportUtilitiesTest {

    private ImportUtilities importUtilities

    @Before
    void before() {
        importUtilities = new ImportUtilities()
    }

    @Test
    void "get type guesses"() {
        Map fieldsAndValues = [
            listInts: ["1", "2", "3"],
            listLongs: [1L as String, 2L as String, 3L as String],
            listDoubles: ["1.2324", "2.123", "3.41324"],
            listFloats: [1F as String, 2F as String],
            listDates: ["2000-01-13T12:24:02.000Z", "2000-01-13 12:24:02.020", "2000-01-13T12:24:02Z", "2000-01-13 12:24:02"],
            listObjects: ["{\"age\": 12, \"name\": \"Joe\"}"],
            listOther: ["true", "2awa"]
        ]
        importUtilities.getTypeGuesses(fieldsAndValues).each { ftPair ->
            if(ftPair.name == "listInts") {
                assert ftPair.type == FieldType.INTEGER
            } else if(ftPair.name == "listLongs") {
                assert ftPair.type == FieldType.INTEGER
            } else if(ftPair.name == "listDoubles") {
                assert ftPair.type == FieldType.DOUBLE
            } else if(ftPair.name == "listFloats") {
                assert ftPair.type == FieldType.DOUBLE
            } else if(ftPair.name == "listDates") {
                assert ftPair.type == FieldType.DATE
            } else if(ftPair.name == "listObjects") {
                assert ftPair.type == FieldType.OBJECT
                ftPair.objectFTPairs.each { pair ->
                    if(ftPair.name == "age") {
                        assert ftPair.type == FieldType.INTEGER
                    } else if(ftPair.name == "name") {
                        assert ftPair.type == FieldType.STRING
                    }
                }
            } else if(ftPair.name == "listOther") {
                assert ftPair.type == FieldType.STRING
            }
        }
    }

    @Test
    void "is list integers"() {
        assert importUtilities.isListIntegers(["1", "2", "3", 4L as String])
        assert importUtilities.isListIntegers(["1", "2", "3", "", "4"])
        assert importUtilities.isListIntegers(["1", "none", "2", "3", "4"])
        assert importUtilities.isListIntegers(["1", "2", "3", "4", "null"])
        assert !importUtilities.isListIntegers(["1.0", 1.02132F as String, "2", "3", "4"])
        assert !importUtilities.isListIntegers(["1", "2", "3", "4", "4a"])
        assert !importUtilities.isListIntegers(["true", "1", "2", "3", "4"])
        assert importUtilities.isListIntegers([])
    }

    @Test
    void "is list longs"() {
        assert importUtilities.isListLongs([123213L as String, "2", "3", "4"])
        assert importUtilities.isListLongs([1L as String, "none", 2L as String, 3L as String, 4L as String])
        assert importUtilities.isListLongs([1L as String, 2L as String, 3L as String, 4L as String, "null"])
        assert !importUtilities.isListLongs([1L as String, 2L as String, 3L as String, 4L as String, "4a"])
        assert !importUtilities.isListLongs(["true", 1L as String, 2L as String, 3L as String, 4L as String])
        assert !importUtilities.isListLongs([1F as String, "2.12", 3L as String, 4L as String])
        assert importUtilities.isListLongs([])
    }

    @Test
    void "is list doubles"() {
        assert importUtilities.isListDoubles(["1.0", "2.12", "3", "4"])
        assert importUtilities.isListDoubles(["1", 123213L as String, 12321211F as String, "", "4"])
        assert importUtilities.isListDoubles(["1", "", "2", "3", "4"])
        assert importUtilities.isListDoubles(["1", "none", "2", "3", "4"])
        assert importUtilities.isListDoubles(["1", "2", "3", "4", "null"])
        assert !importUtilities.isListDoubles(["1", "2", "3", "4", "4a"])
        assert !importUtilities.isListDoubles(["true", "1", "2", "3", "4"])
        assert importUtilities.isListDoubles([])
    }

    @Test
    void "is list floats"() {
        assert importUtilities.isListFloats(["1.0", "2.12", "3", "4"])
        assert importUtilities.isListFloats(["1", 123213L as String, 123213F as String, "", "4"])
        assert importUtilities.isListFloats(["1", "none", "2F", "3", "4"])
        assert importUtilities.isListFloats(["1", "2", "3", "4", "null"])
        assert !importUtilities.isListFloats(["1", "2", "3", "4", "4a"])
        assert !importUtilities.isListFloats(["true", "1", "2", "3", "4"])
        assert importUtilities.isListFloats([])
    }

    @Test
    void "is list dates"() {
        assert importUtilities.isListDates(["2000-01-13T12:24:02.000Z", "2000-01-13 12:24:02.020", "2000-01-13T12:24:02Z", "2000-01-13 12:24:02"])
        assert importUtilities.isListDates(["22000-01-13T12:24:02.000Z", "none", "null", ""])
        assert !importUtilities.isListDates(["2000-01-13T12:24Z"])
        assert !importUtilities.isListDates(["a"])
        assert importUtilities.isListDates([])
    }

    @Test
    void "is list objects"() {
        assert importUtilities.isListObjects(["{\"age\": 12, \"name\": \"Joe\"}", "[12, 13, 14]"])
        assert importUtilities.isListObjects(["{\"age\": 12, \"name\": \"Joe\"}", "null", "none", ""])
        assert !importUtilities.isListObjects(["1", "1.0", "2000-01-13T12:24:02.000Z"])
        assert importUtilities.isListObjects([])
    }

    @Test
    void "convert value to type"() {
        assert importUtilities.convertValueToType("12", FieldType.INTEGER) == 12
        assert importUtilities.convertValueToType("12", FieldType.LONG) == 12L
        assert importUtilities.convertValueToType("12.1", FieldType.DOUBLE) == 12.1
        assert importUtilities.convertValueToType("12.1", FieldType.FLOAT) == 12.1F
        assert importUtilities.convertValueToType("2000-01-13T12:24:02.000Z", FieldType.DATE) == new Date("Thu Jan 13 12:24:02 EST 2000")
        assert importUtilities.convertValueToType("2000-01-13 12:24", FieldType.DATE) instanceof ConversionFailureResult
        assert importUtilities.convertValueToType("2000-01-13 12:24", FieldType.DATE, "yyyy-MM-dd HH:mm") == new Date("Thu Jan 13 12:24:00 EST 2000")
        assert importUtilities.convertValueToType("{\"age\": 12, \"name\": \"Joe\"}", FieldType.OBJECT) == [age: 12, name: "Joe"]
        assert importUtilities.convertValueToType("{\"age\": 12, \"name\": \"Joe\"}", FieldType.STRING) == "{\"age\": 12, \"name\": \"Joe\"}"
        assert importUtilities.convertValueToType("true", FieldType.STRING) == "true"
    }

    @Test(expected=NoSuchMethodException)
    void "convert value to object"() {
        assert importUtilities.convertValueToObject("{\"age\": 12, \"name\": \"Joe\"}") == [age: 12, name: "Joe"]
        // Throws NoSuchMethodException exception
        importUtilities.convertValueToObject("12")
    }

    @Test
    void "retrieve object fields and values"() {
        assert importUtilities.retrieveObjectFieldsAndValues("[{\"age\": 12, \"name\": \"Joe\"}, {\"age\": 22, \"name\": \"Bob\"}]") == [age: ["12", "22"], name: ["Joe", "Bob"]]
    }

    @Test
    void "make ugly name"() {
        assert importUtilities.makeUglyName("username", "prettyName") == ("username" + importUtilities.SEPARATOR + "prettyName")
    }

    @Test
    void "remove quotations"() {
        assert importUtilities.removeQuotations("\"Hello \"There\"\"") == "Hello \"There\""
        assert importUtilities.removeQuotations("'Hello There'") == "Hello There"
        assert importUtilities.removeQuotations("'Hello There") == "'Hello There"
        assert importUtilities.removeQuotations("'Hello' There") == "'Hello' There"
        assert importUtilities.removeQuotations("'Hello' There\"") == "'Hello' There\""
    }

}
