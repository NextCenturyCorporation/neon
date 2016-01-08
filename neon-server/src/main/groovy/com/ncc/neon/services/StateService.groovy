/*
 * Copyright 2015 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.query.derby.DerbyExecutor
import com.ncc.neon.query.derby.StateServiceException

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Service for loading and saving states that are stored in a database.
 */

@Component
@Path("/stateservice")
class StateService {

    @Autowired
    FilterService filterService

    @Autowired
    DerbyExecutor derbyExecutor

    /**
     * Saves the current filter state and given dashboard state to the database using the given name. If a name isn't given,
     * the current time will be used.
     * @param stateName Optional name to give to the filter and dashboard states.
     * @param dashboardState All the dashboard state information needed to save to the database.
     * @param dashboardState.dashboard Layout of the dashboard.
     * @param dashboardState.dataset Dataset being used for the dashboard.
     * @return Map of the new dashboard and filter state IDs.
     * @method saveState
     */
    @POST
    @Path("savestate")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> saveState(@QueryParam("stateName") String stateName,
                                  Map dashboardState) {
        Map<String, String> idMapping = [:]
        String dashboardStateId
        String filterStateId

        if(stateName) {
            dashboardStateId = derbyExecutor.getDashboardStateId(stateName)
            filterStateId = derbyExecutor.getFilterStateId(stateName)
        }

        if(!dashboardState.get("dashboard")) {
            dashboardState.put("dashboard", [])
        }
        if(!dashboardState.get("dataset")) {
            dashboardState.put("dataset", [:])
        }

        dashboardStateId = (dashboardStateId ? derbyExecutor.editDashboardState(dashboardState, dashboardStateId) : derbyExecutor.saveDashboardState(dashboardState, stateName))

        List filters = filterService.getFilters("*", "*")
        filterStateId = (filterStateId ? derbyExecutor.editFilterState(filters, filterStateId) : derbyExecutor.saveFilterState(filters, stateName))

        idMapping.put("dashboardStateId", dashboardStateId)
        idMapping.put("filterStateId", filterStateId)

        return idMapping
    }

    /**
     * Finds the states for the given IDs or name. Clears and adds the filter states found and returns
     * the dashboard state.
     * @param dashboardStateId ID of the dashboard state to load.
     * @param filterStateId ID of the filter state to load.
     * @param stateName Name of the state to load. If no state name is given, the IDs will be used.
     * @return Map giving the dashboard and dataset for the state as well as the IDs.
     * @method loadState
     */
    @GET
    @Path("loadstate")
    @Produces(MediaType.APPLICATION_JSON)
    Map loadState(@QueryParam("dashboardStateId") String dashboardStateId,
                   @QueryParam("filterStateId") String filterStateId,
                   @QueryParam("stateName") String stateName) {
        Map states = [:]
        String dashboardId = dashboardStateId
        String filterId = filterStateId
        if(stateName) {
            dashboardId = derbyExecutor.getDashboardStateId(stateName)
            filterId = derbyExecutor.getFilterStateId(stateName)
            if(!(dashboardId || filterId)) {
                throw new StateServiceException("No saved state with name " + stateName + " was found")
            }
        }
        if(dashboardId) {
            Map dashboardState = derbyExecutor.getDashboardState(dashboardId)
            if(dashboardState) {
                states.put("dashboardStateId", dashboardId)
                if(dashboardState.size()) {
                    states.put("dashboard", dashboardState.get("dashboard"))
                    states.put("dataset", dashboardState.get("dataset"))
                }
            }
        }
        if(filterId) {
            List<FilterKey> filterState = derbyExecutor.getFilterState(filterId)
            if(filterState != null) {
                states.put("filterStateId", filterId)
                if(filterState.size()) {
                    filterService.clearFilters()
                    editFilters(filterState).each { filter ->
                        filterService.replaceFilter(filter)
                    }
                }
            }
        }
        return states
    }

    /**
     * Finds the state name for the given state IDs. Only returns the name if it matches for
     * both IDs.
     * @param dashboardStateId
     * @param filterStateId
     * @return Map giving the state name.
     * @method stateName
     */
    @GET
    @Path("statename")
    @Produces(MediaType.APPLICATION_JSON)
    Map stateName(@QueryParam("dashboardStateId") String dashboardStateId,
                        @QueryParam("filterStateId") String filterStateId){
        if(dashboardStateId && filterStateId) {
            String dashboardStateName = derbyExecutor.getDashboardStateName(dashboardStateId)
            String filterStateName = derbyExecutor.getFilterStateName(filterStateId)

            if(dashboardStateName == filterStateName) {
                return [
                    "stateName": dashboardStateName
                ]
            }
        }

        return [:]
    }

    /**
     * Finds the names for all the saved states.
     * @return List of all the unique state names.
     * @method getAllStatesNames
     */
    @GET
    @Path("allstatesnames")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getAllStatesNames(){
        List<String> states = derbyExecutor.getAllDashboardStateNames()

        derbyExecutor.getAllFilterStateNames().each { stateName ->
            if(!states.contains(stateName)) {
                states.add(stateName)
            }
        }

        return states
    }

    /**
     * Deletes the states associated with the given name.
     * @param stateName
     * @return Map of the state IDs deleted.
     * @method deleteState
     */
    @DELETE
    @Path("deletestate/{stateName}")
    @Produces(MediaType.APPLICATION_JSON)
    Map deleteState(@PathParam("stateName") String stateName){
        return derbyExecutor.deleteState(stateName)
    }

    /**
     * Edits each filter's ID and filterName so it shows the filters belong to the dashboard now instead of
     * a particular visualization.
     * @param filters List of FilterKey objects
     * @return The edited filters
     * @method editFilters
     * @private
     */
    private List<FilterKey> editFilters(List<FilterKey> filters) {
        filters.each { filter ->
            def index = filter.id.indexOf("-")
            filter.id = "dashboard-" + (index >= 0 ? filter.id.substring(index + 1) : filter.id)

            index = filter.filter.filterName.indexOf(" - ")
            def colonIndex = filter.filter.filterName.indexOf(":")
            if(index >= 0 && colonIndex >= 0) {
                filter.filter.filterName = filter.filter.filterName.substring(index + 3)
            }
        }
    }
}