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

        def databaseName1 = "databaseName1"
        def databaseName2 = "databaseName2"
        def tableName1 = "tableName1"
        def tableName2 = "tableName2"


        def filter1 = new Filter(databaseName: databaseName1, tableName: tableName1)
        def filter2 = new Filter(databaseName: databaseName1, tableName: tableName1)
        def filter3 = new Filter(databaseName: databaseName2, tableName: tableName2)

        filterState.addFilter(filter1)
        filterState.addFilter(filter2)
        filterState.addFilter(filter3)

        verifyFilters(databaseName1, tableName1, [filter1, filter2])
        verifyFilters(databaseName2, tableName2, [filter3])
    }

    @Test
    void "remove filter"() {
        def databaseName1 = "databaseName1"
        def tableName = "tableName"
        def filter1 = new Filter(databaseName: databaseName1, tableName: tableName)
        def filter2 = new Filter(databaseName: databaseName1, tableName: tableName)
        def id1 = filterState.addFilter(filter1)
        def id2 = filterState.addFilter(filter2)
        filterState.removeFilter(id1)
        verifyFilters(databaseName1, tableName, [filter2])
        filterState.removeFilter(id2)
        verifyFilters(databaseName1, tableName, [])
    }

    @Test
    void "clear filters"() {
        def databaseName1 = "databaseName1"
        def tableName = "tableName"
        def filter1 = new Filter(databaseName: databaseName1, tableName: tableName)
        def filter2 = new Filter(databaseName: databaseName1, tableName: tableName)
        filterState.addFilter(filter1)
        filterState.addFilter(filter2)
        filterState.clearFilters()
        verifyFilters(databaseName1, tableName, [])
    }

    private def verifyFilters(dataStoreName, databaseName, expected) {
        def actual = filterState.getFiltersForDataset(dataStoreName, databaseName)
        AssertUtils.assertEqualCollections(expected, actual)
    }

}
