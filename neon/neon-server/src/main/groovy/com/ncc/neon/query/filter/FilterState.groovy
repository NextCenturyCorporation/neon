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
    private final def dataSourcesToFilters = [:]

    /**
     * Clears any existing filters
     */
    def clearFilters() {
        idsToFilters.clear()
        dataSourcesToFilters.clear()
    }

    /**
     * Adds a new filter
     * @param filter
     * @return The id of the newly added filter
     */
    def addFilter(filter) {
        def id = UUID.randomUUID()
        def dataSource = dataSourceFromFilter(filter)
        idsToFilters.put(id, filter)
        if (!dataSourcesToFilters.containsKey(dataSource)) {
            dataSourcesToFilters.put(dataSource, [] as Set)
        }
        dataSourcesToFilters[dataSource] << id
        return id
    }

    /**
     * Removes the filter
     * @param id The id of the filter generated when adding it
     * @return
     */
    def removeFilter(id) {
        def filter = idsToFilters.remove(id)
        def dataSource = dataSourceFromFilter(filter)
        def datasetFilters = dataSourcesToFilters[dataSource]
        datasetFilters.remove(id)
        if (datasetFilters.isEmpty()) {
            dataSourcesToFilters.remove(dataSource)
        }

    }

    /**
     * Gets any filters that are applied to the specified dataset
     * @param dataSourceName The name of the data source containing the data set
     * @param datasetId The id of the dataset whose filters are being returned
     * @return
     */
    def getFiltersForDataset(dataSourceName, datasetId) {
        def filters = []
        DataSource dataSource = new DataSource(dataSourceName: dataSourceName, datasetId: datasetId)
        if (dataSourcesToFilters.containsKey(dataSource)) {
            def ids = dataSourcesToFilters.get(dataSource)
            ids.each {
                filters << idsToFilters[it]
            }
        }
        return filters
    }

    private static def dataSourceFromFilter(filter) {
        return new DataSource(dataSourceName: filter.dataSourceName, datasetId: filter.datasetId)
    }

}
