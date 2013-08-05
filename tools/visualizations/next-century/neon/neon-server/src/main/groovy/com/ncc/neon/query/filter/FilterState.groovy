package com.ncc.neon.query.filter

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

    private final def idsToFilters = [:]
    private final def dataSetToFilters = [:]

    /**
     * Clears any existing filters
     */
    def clearFilters() {
        idsToFilters.clear()
        dataSetToFilters.clear()
    }

    /**
     * Adds a new filter
     * @param filter
     * @return The id of the newly added filter
     */
    def addFilter(filter) {
        def id = UUID.randomUUID()
        def dataSet = dataSetFromFilter(filter)
        idsToFilters.put(id, filter)
        if (!dataSetToFilters.containsKey(dataSet)) {
            dataSetToFilters.put(dataSet, [] as Set)
        }
        dataSetToFilters[dataSet] << id
        return id
    }

    /**
     * Removes the filter
     * @param id The id of the filter generated when adding it
     * @return
     */
    def removeFilter(id) {
        def filter = idsToFilters.remove(id)
        def dataSet = dataSetFromFilter(filter)
        def dataSetFilters = dataSetToFilters[dataSet]
        dataSetFilters.remove(id)
        if (dataSetFilters.isEmpty()) {
            dataSetToFilters.remove(dataSet)
        }

    }

    /**
     * Gets any filters that are applied to the specified dataset
     * @param dataStoreName The name of the data store containing the data
     * @param databaseName The name of the database from which the filters are being returned.
     * @return
     */
    def getFiltersForDataset(dataStoreName, databaseName) {
        def filters = []
        DataSet dataSet = new DataSet(dataStoreName: dataStoreName, databaseName: databaseName)
        if (dataSetToFilters.containsKey(dataSet)) {
            def ids = dataSetToFilters.get(dataSet)
            ids.each {
                filters << idsToFilters[it]
            }
        }
        return filters
    }

    private static def dataSetFromFilter(filter) {
        return new DataSet(dataStoreName: filter.dataStoreName, databaseName: filter.databaseName)
    }

}
