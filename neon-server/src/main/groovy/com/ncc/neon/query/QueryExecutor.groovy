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
     * Executes the query and returns the results in json
     * @param query
     * @param includedFiltered includes all data, even those that have been removed by the currently applied filters
     * @return The result of the query
     */
    QueryResult execute(Query query, boolean includedFiltered);

    /**
     * Gets the names of the fields in the specified dataset
     * @param dataSourceName
     * @param datasetId
     * @return
     */
    String getFieldNames(String dataSourceName, String datasetId);

    /**
     * Applies a filter so only data within this filter is returned by a query
     * @param filter
     * @return The id of the filter
     */
    UUID addFilter(Filter filter);

    /**
     * Removes a filter with the specified id
     * @param id
     */
    void removeFilter(UUID id);

    /**
     * Clears all filters
     */
    void clearFilters();

    /**
     * Sets the selection to be the items that match the filter
     * @param filter
     */
    void setSelectionWhere(Filter filter);

    // the id is left as a generic Object since it may vary depending on the implementation. some datastores
    // keep complex ids so we may not be able to create a single class/interface to accurately describe it

    /**
     * Sets the selection to those items with the specified ids
     * @param ids
     */
    void setSelectedIds(Collection<Object> ids);

    /**
     * Adds the items with the specified ids to the current selection
     * @param ids
     */
    void addSelectedIds(Collection<Object> ids);

    /**
     * Removes the items with the specified ids from the current selection
     * @param ids
     */
    void removeSelectedIds(Collection<Object> ids);

    /**
     * Clears the current selection
     */
    void clearSelection();

    /**
     * Gets any selected items that are matched by this filter
     * @param filter
     * @return
     */
    QueryResult getSelectionWhere(Filter filter);

}
