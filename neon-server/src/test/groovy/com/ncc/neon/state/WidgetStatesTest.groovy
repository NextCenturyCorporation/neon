/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.state

import org.junit.Test




class WidgetStatesTest {

    private static final String CLIENT_ID = "client id"
    private static final String STATE_DATA = "state data"
    private static final String DIFFERENT_DATA = "new data"

    @Test
    void "adding null or empty clientId does nothing"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(new WidgetState(instanceId: null, state: STATE_DATA))
        widgetStates.addWidgetState(new WidgetState(instanceId: "", state: STATE_DATA))

        WidgetState retrievedWidgetState = widgetStates.getWidgetState(null)
        assert !retrievedWidgetState

        retrievedWidgetState = widgetStates.getWidgetState("")
        assert !retrievedWidgetState
    }

    @Test
    void "add and retrieve state"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(new WidgetState(instanceId: CLIENT_ID, state: STATE_DATA))
        WidgetState retrievedWidgetState = widgetStates.getWidgetState(CLIENT_ID)

        assert retrievedWidgetState.instanceId == CLIENT_ID
        assert retrievedWidgetState.state == STATE_DATA
    }

    @Test
    void "save multiple states and the last one gets used"() {
        WidgetStates widgetStates = new WidgetStates()
        widgetStates.addWidgetState(new WidgetState(instanceId: CLIENT_ID, state: STATE_DATA))
        widgetStates.addWidgetState(new WidgetState(instanceId: CLIENT_ID, state: DIFFERENT_DATA))
        WidgetState retrievedWidgetState = widgetStates.getWidgetState(CLIENT_ID)

        assert retrievedWidgetState.instanceId == CLIENT_ID
        assert retrievedWidgetState.state == DIFFERENT_DATA
    }

}
