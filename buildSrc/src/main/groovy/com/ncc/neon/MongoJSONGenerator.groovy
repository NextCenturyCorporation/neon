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

/**
 * Generates json that can be inserted into a mongodb, specifically converting ids and dates to their mongo representations
 */
class MongoJSONGenerator extends AbstractJSONGenerator {


    @Override
    protected void modifyJson(jsonArray) {
        jsonArray.length().times { index ->
            def row = jsonArray.get(index)
            rewriteIdField(row)
            rewriteDateFields(row)
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

}
