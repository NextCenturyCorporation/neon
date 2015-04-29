/*
 * Copyright 2013 Next Century Corporation
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

package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject

class JSONToCSVConverter {

    private static final DATE_REGEX = /.*date.*/

    /**
     * Converts the input json text to csv and writes it to the output file. Writes the field names to a separate file
     *
     * @param jsonText
     * @param outputCsvFile
     * @param outputFieldsFile
     * @param excludes Any fields to exclude from the resulting csv
     */
    static convertToCSV(jsonText, outputCsvFile, outputFieldsFile, excludes = [] as Set) {
        def (fields, rows) = parseJson(jsonText, excludes)
        writeCSV(outputCsvFile, fields, rows)
        writeFieldsFile(outputFieldsFile, fields)
    }

    private static def parseJson(jsonText, excludes) {
        def fields = [] as LinkedHashSet
        def rows = []

        def jsonArray = new JSONArray(jsonText)
        jsonArray.length().times { index ->
            def row = [:]
            def jsonObject = jsonArray.get(index)
            jsonObject.keys().each { key ->
                if (!excludes.contains(key)) {
                    row[key] = jsonObject.get(key)
                    fields << key
                }
            }
            rows << row
        }
        return [fields, rows]

    }

    private static void writeCSV(outputCsvFile, fields, rows) {
        outputCsvFile.parentFile.mkdirs()
        outputCsvFile.withWriter { w ->
            // since this data comes from json, each row may have different row keys so normalize here by adding
            // empty values for keys that this row did not have
            rows.each { row ->
                def rowWithEmptyValues = []
                fields.each { field ->
                    def val = row[field] ?: '\\N'
                    if (val) {
                        if (field =~ DATE_REGEX) {
                            val = val.replaceAll("T", " ").replaceAll("Z", "")
                        }
                        if(field == 'tags') {
                            val = val.join(":")
                            val = val.replaceAll('"', '')
                        }
                    }
                    rowWithEmptyValues << val
                }
                w.println(rowWithEmptyValues.join(","))
            }
        }
    }


    private static void writeFieldsFile(outputFieldsFile, fields) {
        outputFieldsFile.withWriter { w ->
            // use print not println since we're just going to read/parse the text and we don't want any new lines
            w.print fields.join(",")
        }
    }
}