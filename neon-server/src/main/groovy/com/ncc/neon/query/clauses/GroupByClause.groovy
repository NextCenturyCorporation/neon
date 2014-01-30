package com.ncc.neon.query.clauses

import org.codehaus.jackson.annotate.JsonSubTypes
import org.codehaus.jackson.annotate.JsonTypeInfo



/**
 * Marker interface just to give context that implementors are GroupByClauses.
 * Also provides JSON metadata to determine which implementation to use
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes([
@JsonSubTypes.Type(value = GroupByFieldClause, name = 'single'),
@JsonSubTypes.Type(value =  GroupByFunctionClause, name =  'function')
])
public interface GroupByClause {
}