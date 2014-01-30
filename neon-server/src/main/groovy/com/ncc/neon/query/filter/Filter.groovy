package com.ncc.neon.query.filter
import com.ncc.neon.query.clauses.WhereClause
import groovy.transform.ToString


/**
 * A filter is applied to a DataSet and can contain a whereClause
 */
@ToString(includeNames = true)
class Filter implements Serializable {

    private static final long serialVersionUID = 7238913369114626126L

    String databaseName
    String tableName

    WhereClause whereClause

}
