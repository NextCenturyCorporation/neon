package com.ncc.neon.state

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

    private static final String CLIENT_ID = "client id"
    private static final String STATE_DATA = "state data"
    private static final String DIFFERENT_DATA = "new data"

    @Test
    void "adding null or empty clientId does nothing"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(null, STATE_DATA)
        widgetStates.addWidgetState("", STATE_DATA)

        WidgetState retrievedWidgetState = widgetStates.getWidgetState(null)
        assert !retrievedWidgetState

        retrievedWidgetState = widgetStates.getWidgetState("")
        assert !retrievedWidgetState
    }

    @Test
    void "add and retrieve state"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(CLIENT_ID, STATE_DATA)
        WidgetState retrievedWidgetState = widgetStates.getWidgetState(CLIENT_ID)

        assert retrievedWidgetState.clientId == CLIENT_ID
        assert retrievedWidgetState.state == STATE_DATA
    }

    @Test
    void "save multiple states and the last one gets used"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(CLIENT_ID, STATE_DATA)
        widgetStates.addWidgetState(CLIENT_ID, DIFFERENT_DATA)
        WidgetState retrievedWidgetState = widgetStates.getWidgetState(CLIENT_ID)

        assert retrievedWidgetState.clientId == CLIENT_ID
        assert retrievedWidgetState.state == DIFFERENT_DATA
    }

    @Test
    void "clear data"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState("$CLIENT_ID 1", STATE_DATA)
        widgetStates.addWidgetState("$CLIENT_ID 2", DIFFERENT_DATA)

        assert !widgetStates.getWidgetState(CLIENT_ID)
        assert widgetStates.getWidgetState("$CLIENT_ID 1")
        assert widgetStates.getWidgetState("$CLIENT_ID 2")

        widgetStates.clearWidgetStates()

        assert !widgetStates.getWidgetState(CLIENT_ID)
        assert !widgetStates.getWidgetState("$CLIENT_ID 1")
        assert !widgetStates.getWidgetState("$CLIENT_ID 2")
    }

}
