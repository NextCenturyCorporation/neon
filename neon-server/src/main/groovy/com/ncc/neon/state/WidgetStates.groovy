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

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext



/**
 * A cache for all the widget states for a given user.
 */

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class WidgetStates implements Serializable{

    private static final long serialVersionUID = - 5362959301029628371L

    // the states are transient because we don't actually want to save them between user sessions
    private transient Set<WidgetState> states

    WidgetStates() {
        initEmptyStates()
    }

    private void initEmptyStates() {
        states = [] as Set
    }

    /**
     * Creates a new widget state for the current user.
     * @param state The state being stored
     */

    void addWidgetState(WidgetState state) {
        if(!state.instanceId){
            return
        }
        states.remove(state)
        states.add(state)
    }

    /**
     * Gets the widget state from the session
     * @param instanceId The instance id that was used to save the widget state
     * @return the WidgetState or null if none is found.
     */
    WidgetState getWidgetState(String instanceId) {
        states.find {
            instanceId == it.instanceId
        }
    }

    @SuppressWarnings("UnusedPrivateMethod") // needed for deserialization
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject()
        initEmptyStates()
    }


}
