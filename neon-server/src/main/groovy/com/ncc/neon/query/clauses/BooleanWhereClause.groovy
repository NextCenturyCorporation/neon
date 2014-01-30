package com.ncc.neon.query.clauses

import groovy.transform.ToString



/**
 * A list of where clauses with a boolean operator (either 'and' or 'or')
 */

@ToString(includeNames = true)
class BooleanWhereClause implements WhereClause, Serializable {

    private static final long serialVersionUID = -1686544619324087210L

    List<WhereClause> whereClauses

}
