package com.ncc.neon.query.filter

import com.ncc.neon.util.AssertUtils
import org.junit.Before
import org.junit.Test

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
class FilterStateTest {

    def filterState

    @Before
    void before() {
        filterState = new FilterState()
    }

    @Test
    void "add filter"() {

        def dataSourceName1 = "dataSource1"
        def dataSourceName2 = "dataSource2"
        def datasetId1 = "dataset1"
        def datasetId2 = "dataset2"


        def filter1 = new Filter(dataSourceName:  dataSourceName1, datasetId: datasetId1)
        def filter2 = new Filter(dataSourceName:  dataSourceName1, datasetId: datasetId1)
        def filter3 = new Filter(dataSourceName:  dataSourceName2, datasetId: datasetId2)

        filterState.addFilter(filter1)
        filterState.addFilter(filter2)
        filterState.addFilter(filter3)

        verifyFilters(dataSourceName1, datasetId1, [filter1,filter2])
        verifyFilters(dataSourceName2, datasetId2, [filter3])
    }

    @Test
    void "remove filter"() {
        def dataSourceName1 = "dataSource1"
        def datasetId1 = "dataset1"
        def filter1 = new Filter(dataSourceName:  dataSourceName1, datasetId: datasetId1)
        def filter2 = new Filter(dataSourceName:  dataSourceName1, datasetId: datasetId1)
        def id1 = filterState.addFilter(filter1)
        def id2 = filterState.addFilter(filter2)
        filterState.removeFilter(id1)
        verifyFilters(dataSourceName1, datasetId1, [filter2])
        filterState.removeFilter(id2)
        verifyFilters(dataSourceName1, datasetId1, [])
    }

    @Test
    void "clear filters"() {
        def dataSourceName = "dataSource1"
        def datasetId = "dataset1"
        def filter1 = new Filter(dataSourceName:  dataSourceName, datasetId: datasetId)
        def filter2 = new Filter(dataSourceName:  dataSourceName, datasetId: datasetId)
        filterState.addFilter(filter1)
        filterState.addFilter(filter2)
        filterState.clearFilters()
        verifyFilters(dataSourceName, datasetId, [])
    }

    private def verifyFilters(dataSourceName,datasetId,expected) {
        def actual =  filterState.getFiltersForDataset(dataSourceName,datasetId)
        AssertUtils.assertEqualCollections(expected,actual)
    }

}
