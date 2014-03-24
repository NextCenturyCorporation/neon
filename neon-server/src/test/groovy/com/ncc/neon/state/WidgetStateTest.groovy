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




class WidgetStateTest {

    private static final String ID_1 = "1"
    private static final String ID_2 = "2"
    private static final String CONTENT_1 = "content1"
    private static final String CONTENT_2 = "content2"

    @Test
    void "ids must be equal for WidgetStates to be equal"() {
        WidgetState state1 = new WidgetState(instanceId: ID_1, state: CONTENT_1)
        WidgetState state2 = new WidgetState(instanceId: ID_1, state: CONTENT_1)

        assert state1 == state2

        state2 = new WidgetState(instanceId: ID_1, state: CONTENT_2)

        assert state1 == state2
    }

    @Test
    void "if the ids are not equal the widget states are not equal"() {

        WidgetState state1 = new WidgetState(instanceId: ID_1, state: CONTENT_1)
        WidgetState state2 = new WidgetState(instanceId: ID_2, state: CONTENT_1)

        assert state1 != state2

        state2 = new WidgetState(instanceId: ID_2, state: CONTENT_2)

        assert state1 != state2

    }

}
