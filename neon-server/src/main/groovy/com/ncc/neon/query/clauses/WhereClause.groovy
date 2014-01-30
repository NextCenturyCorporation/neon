package com.ncc.neon.query.clauses

import org.codehaus.jackson.annotate.JsonSubTypes
import org.codehaus.jackson.annotate.JsonTypeInfo



/**
 * Marker interface just to give context that implementors are WhereClauses.
 * Also provides JSON metadata to determine which implementation to use
 */


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes([
    @JsonSubTypes.Type(value = SingularWhereClause, name = 'where'),
    @JsonSubTypes.Type(value = WithinDistanceClause, name =  'withinDistance'),
    @JsonSubTypes.Type(value =  AndWhereClause, name =  'and'),
    @JsonSubTypes.Type(value =  OrWhereClause, name =  'or')
])
public interface WhereClause {
}