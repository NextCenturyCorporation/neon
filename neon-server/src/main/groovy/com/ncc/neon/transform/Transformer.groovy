package com.ncc.neon.transform
import com.ncc.neon.query.QueryResult


/**
 * Transforms a QueryResult into another QueryResult.
 */

interface Transformer {

    QueryResult convert(QueryResult queryResult, def params)

    String getName()

}