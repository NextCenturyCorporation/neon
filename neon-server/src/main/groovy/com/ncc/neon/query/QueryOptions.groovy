package com.ncc.neon.query

import groovy.transform.Immutable



@Immutable
class QueryOptions {
    static final QueryOptions ALL_DATA = new QueryOptions(true, true)
    static final QueryOptions FILTERED_DATA = new QueryOptions(false, true)
    static final QueryOptions FILTERED_AND_SELECTED_DATA = new QueryOptions(false, false)

    final boolean disregardFilters
    final boolean disregardSelection

}
