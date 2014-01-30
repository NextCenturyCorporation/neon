package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * Limit a query to a specified number of results. This can be combined with an offset to paginate queries.
 */
@ToString(includeNames = true)
class LimitClause {

    int limit

}
