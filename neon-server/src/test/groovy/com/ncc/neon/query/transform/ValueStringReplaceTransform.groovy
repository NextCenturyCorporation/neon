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

package com.ncc.neon.query.transform

import org.json.JSONArray



/**
 * A transform used for testing that can replaces the string values in json results
 */
class ValueStringReplaceTransform {

    private final String replaceThis
    private final int replaceWith

    ValueStringReplaceTransform(String replaceThis, Integer replaceWith) {
        this.replaceThis = replaceThis
        this.replaceWith = replaceWith
    }

    /**
     * A no arg constructor for testing transforms with no arguments. It replaces occurrences of "abc"
     * with 10
     */
    ValueStringReplaceTransform() {
        this("abc", 10)
    }

    @Override
    String apply(inputJsonArray) {
        def jsonArray = new JSONArray(inputJsonArray)
        jsonArray.length().times { index ->
            def object = jsonArray.getJSONObject(index)
            object.keys().each { key ->
                def val = object.getString(key)
                if ( val == replaceThis ) {
                    val = replaceWith
                }
                object.put(key, val)
            }
        }
        return jsonArray.toString()
    }

}
