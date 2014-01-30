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
