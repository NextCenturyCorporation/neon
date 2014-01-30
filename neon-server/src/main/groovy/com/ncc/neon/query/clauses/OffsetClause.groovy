package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * Indicates the number of results that should be offset/skipped in a query. This can be combined with limit to paginate
 * queries.
 */
@ToString(includeNames = true)
class OffsetClause {

    int offset

}
