package com.ncc.neon

import org.json.JSONArray
import org.json.JSONObject

/**
 * Generates json that can be used for hive database tests, specifically removing nested data
 */
class HiveJSONGenerator extends AbstractJSONGenerator {


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
