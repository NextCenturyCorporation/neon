package com.ncc.neon.query

import org.codehaus.jackson.annotate.JsonIgnoreProperties



/**
 * A group of queries that can be executed and results aggregated to be returned
 * to a client as if it was a single query
 */
@JsonIgnoreProperties(value = ['disregardFilters_', 'selectionOnly_'])
class QueryGroup {

    List<Query> queries = []

}
