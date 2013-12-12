package com.ncc.neon.query.filter

import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.WhereClause

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

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
 *
 * 
 * @author tbrooks
 */

class FilterCache implements Serializable{
    private static final long serialVersionUID = - 4017268491878023244L
    private final ConcurrentMap<FilterKey, Filter> filters = [:] as ConcurrentHashMap

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
        def oldFilter = filters.putIfAbsent(filterKey, filter)
        if(oldFilter){
            filter.whereClause = determineFilterWhereClause(oldFilter, filter)
            filters.replace(filterKey, filter)
        }
    }

    private WhereClause determineFilterWhereClause(Filter oldFilter, Filter filter) {
        if (oldFilter.whereClause) {
            AndWhereClause andWhereClause = new AndWhereClause(whereClauses: [oldFilter.whereClause, filter.whereClause])
            return andWhereClause
        }
        return filter.whereClause
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
