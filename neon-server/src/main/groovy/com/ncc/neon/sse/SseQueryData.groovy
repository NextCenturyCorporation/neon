package com.ncc.neon.sse

import com.ncc.neon.query.Query

class SseQueryData {

    // Whether or not the query is still actively being processed.
    boolean active

    // Whether or not the query was completed (for use if/when result caching is implemented).
    boolean complete

    // Number of records in the collection the query is acting on.
    long count

    // Current minimum value of the random value field in the collection the query is acting on.
    double randMin

    // Current step value of the random value field in the collection the query is acting on.
    double randStep

    // Current maximum  value of the random value field in the collection the query is acting on.
    double randMax

    // Host of the database the query was executed on, so that when comparing identical queries on different databases they can be distinguished.
    String host

    // Type of database the query was executed on, so that identically-named databases on different data stores can be distinguished.
    String databaseType

    // Extra information associated with the query.
    boolean ignoreFilters
    boolean selectionOnly
    Set<String> ignoredFilterIds

    // Query this data is associated with.
    Query query

    // Result (or result so far, if incomplete) of the query. Each value should be a list that contains:
    // current value (mean), variance, and error (number for which we draw an X% confidence interval between [value - error, value + error])
    Map results = [:]
}