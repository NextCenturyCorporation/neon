package com.ncc.neon.query.mongo

import com.mongodb.DBObject
import com.ncc.neon.query.Query
import groovy.transform.ToString



/**
 * A container for the information needed to execute a query against a mongo store
 */
@ToString(includeNames = true)
class MongoQuery {

    Query query
    DBObject whereClauseParams
    DBObject selectParams
}
