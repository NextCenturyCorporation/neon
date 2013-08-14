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

import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterKey

/**
 * Executes a query against a datastore
 */
public interface QueryExecutor {

    /**
     * Executes the query and returns the results in json
     * @param query
     * @param includedFiltered includes all data, even those that have been removed by the currently applied filters
     * @return The result of the query
     */
    QueryResult execute(Query query, boolean includedFiltered)

    /**
     * Executes a group of queries and returns the results as a single json object
     * @param query
     * @param includeFiltered
     * @return
     */
    QueryResult execute(QueryGroup query, boolean includeFiltered)

    /**
     * Gets the names of the fields in the specified dataset
     * @param databaseName
     * @param tableName
     * @return
     */
    Collection<String> getFieldNames(String databaseName, String tableName)

    /**
     * Clients must register in order to get a FilterKey
     * @oaram dataSet The DataSet to which this filter is applied
     * @return A new filter key
     */
    FilterKey registerForFilterKey(DataSet dataSet)

    /**
     * Adds a filter. There may be only one Filter with a given FilterKey
     * @param filterKey The identifier for a Filter
     * @param filter The Filter to be added or replaced
     */
    void addFilter(FilterKey filterKey, Filter filter)

    /**
     * Removes a filter with the specified FilterKey
     * @param FilterKey
     */
    void removeFilter(FilterKey filterKey)

    /**
     * Clears all filters
     */
    void clearFilters()

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

    /**
     * @return Returns all the databases
     */
    List<String> showDatabases()

    /**
     * @param dbName The current database
     * @return Returns all the table names
     */
    List<String> showTables(String dbName)
}
