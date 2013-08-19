package com.ncc.neon.query.filter

import com.ncc.neon.query.clauses.AndWhereClause
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext

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

/**
 * Stores any filters applied to the datasets
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class FilterState implements Serializable {

    private static final long serialVersionUID = 5897358582328819569L

    private final Map<FilterKey, Filter> filters = [:]

    /**
     * Clears all existing filters
     */
    void clearAllFilters() {
        filters.clear()
    }

    /**
     * Adds a filter to the filter state
     * @param filterKey
     * @param filter
     * @return
     */
    void addFilter(FilterKey filterKey, Filter filter) {
        if(filters.containsKey(filterKey)){
            Filter oldFilter = filters.get(filterKey)
            if(oldFilter.whereClause){
                AndWhereClause andWhereClause = new AndWhereClause(whereClauses: [oldFilter.whereClause, filter.whereClause])
                filter.whereClause = andWhereClause
            }
        }

        filters.put(filterKey, filter)
    }

    /**
     * Removes the filter
     * @param id The id of the filter generated when adding it
     * @return
     */
    void removeFilter(FilterKey filterKey) {
        filters.remove(filterKey)
    }

    /**
     * Gets any filters that are applied to the specified dataset
     * @param dataset The current dataset
     * @return A list of filters applied to that dataset
     */
    List<Filter> getFiltersForDataset(DataSet dataSet) {
        def datasetFilters = []
        filters.each {k,v ->
            if(k.dataSet == dataSet){
                datasetFilters << v
            }
        }
        return datasetFilters
    }
}
