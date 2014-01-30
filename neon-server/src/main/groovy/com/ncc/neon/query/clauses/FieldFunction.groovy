package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * A generic function that can be applied to a database field that results in a
 * new field being created (such as creating a new field based on the sum of other fields)
 */
@ToString(includeNames = true)
class FieldFunction {

    def name
    def operation
    def field

}
