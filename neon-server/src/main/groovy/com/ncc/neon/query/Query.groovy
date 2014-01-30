package com.ncc.neon.query

import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import groovy.transform.ToString
import org.codehaus.jackson.annotate.JsonIgnoreProperties



/**
 * A query stores a filter for selecting data and optional aggregation methods for grouping the data.
 * The query is translated to a data source specific operation which returns the appropriate data.
 */
@ToString(includeNames = true)
@JsonIgnoreProperties(value = ['disregardFilters_', 'selectionOnly_'])
class Query {

    Filter filter
    boolean isDistinct = false
    List<String> fields = SelectClause.ALL_FIELDS
    List<AggregateClause> aggregates = []
    List<GroupByClause> groupByClauses = []
    List<SortClause> sortClauses = []
    LimitClause limitClause
    OffsetClause offsetClause
    Transform transform

    def getDatabaseName() {
        filter.databaseName
    }

    def getTableName() {
        filter.tableName
    }

}
