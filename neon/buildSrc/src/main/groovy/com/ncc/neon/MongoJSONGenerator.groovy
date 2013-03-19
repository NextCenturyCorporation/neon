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
 * Generates json that can be inserted into a mongodb, specifically converting ids and dates to their mongo representations
 */
class MongoJSONGenerator {

    /**
     * Reads all json files (non-recursively) from the input directory, converts them to a form suitable for upload
     * to mongo and writes them to the output directory. The output directory will be created if needed.
     * @param inputDirPath
     * @param outputDirPath
     */
    static void generateMongoJson(inputDirPath, outputDirPath) {
        new File(inputDirPath).eachFileMatch(~/.*\.json/) { file ->
            def jsonArray = new JSONArray(file.text)
            jsonArray.length().times { index ->
                def row = jsonArray.get(index)
                rewriteIdField(row)
                rewriteDateFields(row)
            }
            writeOutputFile(jsonArray, new File(outputDirPath), file.name)
        }
    }

    private static rewriteIdField(row) {
        // replace the string row with the mongo oid
        if (row.has('_id')) {
            def id = row.get('_id')
            if (id instanceof String) {
                def mongoId = new JSONObject()
                mongoId.put('$oid', id)
                row.put('_id', mongoId)
            }
        }
    }

    private static rewriteDateFields(row) {
        row.keys().each { key ->
            if (key =~ /(?i)date/) {
                def mongoDate = new JSONObject()
                mongoDate.put('$date', row.get(key))
                row.put(key, mongoDate)
            }
        }
    }

    private static writeOutputFile(jsonArray, outDir, fileName) {
        outDir.mkdirs()
        def outfile = new File(outDir, fileName)
        outfile.withWriter { w ->
            w << jsonArray.toString(4)
        }
    }


}
