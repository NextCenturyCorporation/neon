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

import com.ncc.neon.query.filter.Filter

/**
 * Executes a query against a datastore
 */
public interface QueryExecutor {

    /**
     * Executes the query and returns the results
     * @param query
     * @return The result of the query
     */
    QueryResult execute(Query query)

    /**
     * Executes a group of queries and returns the results
     * @param query
     * @return The result of the queries appended together
     */
    QueryResult execute(QueryGroup query)


    /**
     * Executes the query and returns the results
     * @param query
     * @return The result of the query
     */
    QueryResult executeDisregardingFilters(Query query)

    /**
     * Executes a group of queries and returns the results
     * @param query
     * @return The result of the queries appended together
     */
    QueryResult executeDisregardingFilters(QueryGroup query)

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

    /**
     * Sets the selection to be the items that match the filter
     * @param filter
     */
    void setSelectionWhere(Filter filter)

    // the id is left as a generic Object since it may vary depending on the implementation. some datastores
    // keep complex ids so we may not be able to create a single class/interface to accurately describe it

    /**
     * Sets the selection to those items with the specified ids
     * @param ids
     */
    void setSelectedIds(Collection<Object> ids)

    /**
     * Adds the items with the specified ids to the current selection
     * @param ids
     */
    void addSelectedIds(Collection<Object> ids)

    /**
     * Removes the items with the specified ids from the current selection
     * @param ids
     */
    void removeSelectedIds(Collection<Object> ids)

    /**
     * Clears the current selection
     */
    void clearSelection()

    /**
     * Gets any selected items that are matched by this filter
     * @param filter
     * @return
     */
    QueryResult getSelectionWhere(Filter filter)


}
