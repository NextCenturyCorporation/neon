package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * The select clause of a query. The default is * meaning all fields,
 * but fields can be specified in a list.
 */

@ToString(includeNames = true)
class SelectClause {

    /** indicator to select all fields */
    static final ALL_FIELDS = ["*"]

    String databaseName
    String tableName

    def fields = ALL_FIELDS

    boolean isSelectAllFields() {
        return fields == ALL_FIELDS
    }

}
