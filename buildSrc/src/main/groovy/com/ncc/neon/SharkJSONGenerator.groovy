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
 * Generates json that can be used for shark database tests, specifically removing nested data
 */
class SharkJSONGenerator extends AbstractJSONGenerator {


    @Override
    protected void modifyJson(jsonArray) {
        jsonArray.length().times { index ->
            def row = jsonArray.get(index)
            removeNestedData(row)
        }
    }

    private static removeNestedData(row) {
        def keysToRemove = []
        row.keys().each { key ->
            def val = row.get(key)
            if (val instanceof JSONObject) {
                keysToRemove << key
            }
        }
        keysToRemove.each {
            row.remove(it)
        }
    }

}
