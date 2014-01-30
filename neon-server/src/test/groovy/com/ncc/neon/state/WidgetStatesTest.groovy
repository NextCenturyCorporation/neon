package com.ncc.neon.state

import org.junit.Test




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

}
