/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */
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
