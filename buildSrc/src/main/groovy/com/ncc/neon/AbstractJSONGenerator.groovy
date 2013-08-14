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

/**
 * Integration tests use json as their data format. This is an abstract class
 * that can be used to transform the json to a database specific format
 */
abstract class AbstractJSONGenerator {

    /** the number of spaces to use for indentation in the output json */
    private static final def INDENT_FACTOR = 4

    /**
     * Reads all json files (non-recursively) from the input directory, converts them to a form suitable for upload
     * to the specific database and writes them to the output directory. The output directory will be created if needed.
     * @param inputDirPath
     * @param outputDirPath
     * @param regex An optional regex to specify which fiels to process
     */
    void generateJson(inputDirPath, outputDirPath, regex = ~/.*\.json/) {
        new File(inputDirPath).eachFileMatch(regex) { file ->
            def text = file.text
            def json
            if (text.startsWith("[")) {
                json = new JSONArray(text)
                modifyJson(json)
            } else {
                json = new JSONObject(text)
                json.keys().each { key ->
                    def jsonArray = json.getJSONArray(key)
                    modifyJson(jsonArray)
                }
            }
            writeOutputFile(json, new File(outputDirPath), file.name)
        }
    }

    protected abstract void modifyJson(jsonArray)

    private static writeOutputFile(json, outDir, fileName) {
        outDir.mkdirs()
        def outfile = new File(outDir, fileName)
        outfile.withWriter { w ->
            w << json.toString(INDENT_FACTOR)
        }
    }


}
