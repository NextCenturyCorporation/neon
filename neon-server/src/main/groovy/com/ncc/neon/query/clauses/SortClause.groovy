package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * A clause which has a field name and order (ascending or descending)
 */

@ToString(includeNames = true)
class SortClause {

    def fieldName

    SortOrder sortOrder

    def getSortDirection() {
        return sortOrder.direction
    }
}
