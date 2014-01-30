package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * Group by a specified field
 */

@ToString(includeNames = true)
class GroupByFieldClause implements GroupByClause {

    def field

}
