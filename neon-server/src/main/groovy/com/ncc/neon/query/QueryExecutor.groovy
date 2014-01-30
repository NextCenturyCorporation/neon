
package com.ncc.neon.query
/**
 * Executes a query against a generic data source
 */
public interface QueryExecutor {

    /**
     * Executes a query against a generic data source given the current filter and selection state.
     * @param query An object that represents the query we wish to execute
     * @param options Determines if we should include filters or selection in the query execution
     * @return An object containing the results of the query
     */
    QueryResult execute(Query query, QueryOptions options)

    /**
     * Executes a group of queries against a generic data source given the current filter and selection state.
     * @param query A group of query objects
     * @param options Determines if we should include filters or selection in the query execution
     * @return An object containing the results of the query
     */
    QueryResult execute(QueryGroup queryGroup, QueryOptions options)

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
    QueryResult getFieldNames(String databaseName, String tableName)

}
