package com.ncc.neon.session

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
 *
 * 
 * @author tbrooks
 */

class WidgetStatesTest {

    @Test
    void "adding null or empty clientId does nothing"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(null, "state data")
        widgetStates.addWidgetState("", "state data")

        WidgetState retrievedWidgetState = widgetStates.getWidgetState(null)
        assert !retrievedWidgetState

        retrievedWidgetState = widgetStates.getWidgetState("")
        assert !retrievedWidgetState
    }


    @Test
    void "add and retrieve state"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState("client id", "state data")
        WidgetState retrievedWidgetState = widgetStates.getWidgetState("client id")

        assert retrievedWidgetState.clientId == "client id"
        assert retrievedWidgetState.state == "state data"
    }

    @Test
    void "save multiple states and the last one gets used"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState("client id", "state data")
        widgetStates.addWidgetState("client id", "new data")
        WidgetState retrievedWidgetState = widgetStates.getWidgetState("client id")

        assert retrievedWidgetState.clientId == "client id"
        assert retrievedWidgetState.state == "new data"
    }

    @Test
    void "clear data"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState("client id1", "state data")
        widgetStates.addWidgetState("client id2", "new data")

        assert !widgetStates.getWidgetState("client id")
        assert widgetStates.getWidgetState("client id1")
        assert widgetStates.getWidgetState("client id2")

        widgetStates.clearWidgetStates()

        assert !widgetStates.getWidgetState("client id")
        assert !widgetStates.getWidgetState("client id1")
        assert !widgetStates.getWidgetState("client id2")

    }

}
