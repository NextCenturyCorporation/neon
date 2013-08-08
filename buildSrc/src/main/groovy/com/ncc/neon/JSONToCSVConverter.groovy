package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject


/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

class JSONToCSVConverter {

    private static final DATE_REGEX = /.*date.*/

    /**
     * Converts the input json text to csv and writes it to the output file. Writes the field names to a separate file
     *
     * @param jsonText
     * @param outputCsvFile
     * @param outputFieldsField
     * @param excludes A set of fields to exclude
     */
    static convertToCSV(jsonText, outputCsvFile, outputFieldsFile, excludes = [] as Set) {
        def map = ([:] as LinkedHashMap).withDefault { [] }
        def jsonArray = new JSONArray(jsonText)
        jsonArray.length().times { index ->
            def jsonObject = jsonArray.get(index)
            jsonObject.keys().each { key ->
                if (!excludes.contains(key)) {
                    map[key] << jsonObject.get(key)
                }
            }
        }
        outputCsvFile.parentFile.mkdirs()
        outputCsvFile.withWriter { w ->
            jsonArray.length().times { rowIndex ->
                def row = []
                map.keySet().each { field ->
                    def val = map[field][rowIndex]
                    if (field =~ DATE_REGEX) {
                        val = val.replaceAll("T", " ").replaceAll("Z", "")
                    }
                    row << val
                }
                w.println(row.join(","))
            }
        }
        outputFieldsFile.withWriter { w ->
            // use print not println since we're just going to read/parse the text and we don't want any new lines
            w.print map.keySet().join(",")
        }
    }

}