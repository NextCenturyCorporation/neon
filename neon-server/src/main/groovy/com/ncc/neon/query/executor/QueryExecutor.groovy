/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.ncc.neon.query.executor

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair

/**
 * Executes a query against a generic data source
 */
public interface QueryExecutor {
    /**
     * Executes a query against a generic data source given the current filter and selection state.
     * @param query An object that represents the query we wish to execute
     * @param options Determines if we should include filters or selection in the query execution
     * @param filterState The filter state
     * @return An object containing the results of the query
     */
    QueryResult execute(Query query, QueryOptions options, FilterState filterState)

    /**
     * Executes a group of queries against a generic data source given the current filter and selection state.
     * @param query A group of query objects
     * @param options Determines if we should include filters or selection in the query execution
     * @param filterState The filter state
     * @return An object containing the results of the query
     */
    QueryResult execute(QueryGroup queryGroup, QueryOptions options, FilterState filterState)

    /**
     * @return Returns all the databases
     */
    List<String> showDatabases()

    /**
     * @param dbName The current database
     * @return Returns all the table names within the current database
     */
    List<String> showTables(String dbName)

    /**
     * Gets the names of the fields in the specified dataset
     * @param databaseName
     * @param tableName
     * @return
     */
    List<String> getFieldNames(String databaseName, String tableName)

    /**
     * Gets the count for distinct values in an array field
     * @param field the field of the table on which to perform the count
     * @param limit limit number of result pairs to return
     * @param filterState The filter state
     * @param whereClause The where clause to apply to the array counts query, or null to apply no where clause
     */
    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit, FilterState filterState, WhereClause whereClause)
}
