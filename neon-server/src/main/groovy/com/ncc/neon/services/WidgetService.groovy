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

package com.ncc.neon.services
import com.ncc.neon.state.StateIdGenerator
import com.ncc.neon.state.WidgetState
import com.ncc.neon.state.WidgetStates
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
/**
 * Service for saving and restoring widget states.
 */

@Component
@Path("/widgetservice")
class WidgetService {

    @Autowired
    WidgetStates widgetStates

    @Autowired
    StateIdGenerator stateIdGenerator

    /**
     * Saves the state of the widget to the user's session
     * @param state The widget state to save
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("savestate")
    void saveState(WidgetState state) {
        widgetStates.addWidgetState(state)
    }

    /**
     * Gets a widget's state from the session
     * @param instanceId An identifier of a widget instance
     * @return json containing information about the widget's state, or null if nothing is found.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("restorestate/{instanceId}")
    String restoreState(@PathParam("instanceId") String instanceId) {
        def widgetState = widgetStates.getWidgetState(instanceId)
        if (widgetState) {
            return widgetState.state
        }
        return "{}"
    }

    /**
     * Gets an id unique to the session
     * @param qualifier If the qualifier is used, te id will be specific to that qualifier. If
     * no qualifier is used, it will just be a globally unique id for the session.
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("instanceid")
    String getInstanceId(@QueryParam("qualifier") String qualifier) {
        // if there is qualifier, get the id associated with that. otherwise, use the global id
        UUID id = qualifier ? stateIdGenerator.getId(qualifier) : stateIdGenerator.id
        return id.toString()
    }


}
