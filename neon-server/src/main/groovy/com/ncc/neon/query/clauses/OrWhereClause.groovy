package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * This class does not have an additional implementation from the super class,
 * but its type is used by jackson
 */

@ToString(includeSuper = true, includeNames = true)
class OrWhereClause extends BooleanWhereClause {
    private static final long serialVersionUID = 7228009458740671210L
}
