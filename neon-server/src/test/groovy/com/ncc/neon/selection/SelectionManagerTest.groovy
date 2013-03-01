package com.ncc.neon.selection

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
class SelectionManagerTest {


    def selectionManager

    @Before
    void before() {
       selectionManager = new SelectionManager()
    }


    @Test
    void "add ids"() {
        selectionManager.addIds(["a","b","c"])
        selectionManager.addIds(["d","e","f"])
        verifySelectedIds(["a","b","c","d","e","f"])
    }

    @Test
    void "duplicate ids rejected"() {
        selectionManager.addIds(["a","b","a"])
        verifySelectedIds(["a","b"])
    }

    @Test
    void "remove ids"() {
        selectionManager.addIds(["a","b","c"])
        selectionManager.removeIds(["b"])
        verifySelectedIds(["a","c"]);
    }

    @Test
    void "replace ids"() {
        selectionManager.addIds(["a","b","c"])
        selectionManager.replaceSelectionWith(["d","e","f"])
        verifySelectedIds(["d","e","f"])
    }

    @Test
    void "clear ids"() {
        selectionManager.addIds(["a","b","c"])
        selectionManager.clear()
        verifySelectedIds([])
    }

    private def verifySelectedIds(expected) {
        AssertUtils.assertEqualCollections(expected, selectionManager.selectedIds);
    }
}
