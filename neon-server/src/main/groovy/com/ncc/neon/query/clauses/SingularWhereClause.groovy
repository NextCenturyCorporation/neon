package com.ncc.neon.query.clauses

import com.ncc.neon.query.jackson.QueryValueDeserializer
import groovy.transform.ToString
import org.codehaus.jackson.map.annotate.JsonDeserialize



/**
 * A where clause which has a column name, operator, and value
 */

@ToString(includeNames = true)
class SingularWhereClause implements WhereClause, Serializable {

    private static final long serialVersionUID = 5063293720269360039L

    def lhs
    def operator

    @JsonDeserialize(using = QueryValueDeserializer)
    def rhs

}
