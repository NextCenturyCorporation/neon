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
import com.ncc.neon.query.jackson.*

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired

import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.type.TypeReference

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.DELETE
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

import java.sql.*

import groovy.json.JsonSlurper

@Component
@Path("/stateservice")
class StateService {

    @Autowired
    FilterService filterService

    private final String dbName="savedStates"
    private final String dashboardTableName="dashboardStates"
    private final String filterTableName="filterStates"
    private final String connectionURL = "jdbc:derby:" + dbName + ";create=true"

    private final String dashboardCreateScript = "create table " + dashboardTableName +
                                                 "(id varchar(36) not null constraint id_pk_dt primary key, " +
                                                 "name varchar(128) unique, " +
                                                 "dashboard long varchar, " +
                                                 "dataset long varchar)"

    private final String filterCreateScript = "create table " + filterTableName +
                                              "(id varchar(36) not null constraint id_pk_ft primary key, " +
                                              "name varchar(128) unique, " +
                                              "filterstate long varchar)"

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
            dashboardStateId = getStateId(stateName, dashboardTableName)
            filterStateId = getStateId(stateName, filterTableName)
        }

        if(!dashboardState.get("dashboard")) {
            dashboardState.put("dashboard", [])
        }
        if(!dashboardState.get("dataset")) {
            dashboardState.put("dataset", [:])
        }

        dashboardStateId = (dashboardStateId ? editDashboardState(dashboardState, dashboardStateId) : saveDashboardState(dashboardState, stateName))
        filterStateId = (filterStateId ? editFilterState(filterStateId) : saveFilterState(stateName))

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
            dashboardId = getStateId(stateName, dashboardTableName)
            filterId = getStateId(stateName, filterTableName)
        }
        if(dashboardId) {
            Map dashboardState = getDashboardState(dashboardId)
            if(dashboardState) {
                states.put("dashboardStateId", dashboardId)

                if(dashboardState.size()) {
                    states.put("dashboard", dashboardState.get("dashboard"))
                    states.put("dataset", dashboardState.get("dataset"))
                }
            }
        }
        if(filterId) {
            List<FilterKey> filterState = getFilterState(filterId)
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
     * Finds the state name for the given state IDs. Only returns the name if itmatches for
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
            String dashboardStateName = getStateName(dashboardStateId, dashboardTableName)
            String filterStateName = getStateName(filterStateId, filterTableName)

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
        List<String> states = getAllStateNames(dashboardTableName)

        getAllStateNames(filterTableName).each { stateName ->
            if(!states.contains(stateName)) {
                states.add(stateName)
            }
        }

        return states
    }

    /**
     * Retrieves all the data for all saved dashboard states.
     * @return List of all dashboard states and their data.
     * @method getAllDashboardStates
     */
    @GET
    @Path("alldashboardstates")
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, String>> getAllDashboardStates(){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, dashboardTableName)
        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)
        List<Map<String, Object>> toReturn = []

        if(exists) {
            ResultSet allResults = st.executeQuery("select * from " + dashboardTableName)

            while (allResults.next()) {
                def map = [:]
                map.put("id", allResults.getString("id"))
                map.put("name", allResults.getString("name"))
                map.put("dashboard", mapper.readValue(allResults.getString("dashboard"),  new TypeReference<List<Object>>(){}))
                map.put("dataset", mapper.readValue(allResults.getString("dataset"),  new TypeReference<Map>(){}))
                toReturn.add(map)
            }

            allResults.close()
        }

        st.close()
        conn.close()

        return toReturn
    }

    /**
     * Retrieves all the data for all saved filter states.
     * @return List of all filter states and their data.
     * @method getAllFilterStates
     */
    @GET
    @Path("allfilterstates")
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, String>> getAllFilterStates(){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, filterTableName)
        List<Map<String, Object>> toReturn = []

        if(exists) {
            ResultSet allResults = st.executeQuery("select * from " + filterTableName)

            while (allResults.next()) {
                def map = [:]
                map.put("id", allResults.getString("id"))
                map.put("name", allResults.getString("name"))
                map.put("filters",  new JsonSlurper().parseText(allResults.getString("filterstate")))
                toReturn.add(map)
            }

            allResults.close()
        }

        st.close()
        conn.close()

        return toReturn
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
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, dashboardTableName)
        Map toReturn = [:]
        if(exists) {
            ResultSet result = st.executeQuery("select id from " + dashboardTableName + " where name = '" + stateName + "'")

            if(result.next()) {
                toReturn.put("dashboardStateId", result.getString(1))
                st.execute("delete from " + dashboardTableName + " where name = '" + stateName + "'")
            }
        }

        exists = doesDBExist(conn, filterTableName)
        if(exists) {
            ResultSet result = st.executeQuery("select id from " + filterTableName + " where name = '" + stateName + "'")

            if(result.next()) {
                toReturn.put("filterStateId", result.getString(1))
                st.execute("delete from " + filterTableName + " where name = '" + stateName + "'")
            }
        }

        st.close()
        conn.close()

        return toReturn
    }

    /**
     * Saves the dashboard state to the database.
     * @param dashboardState Map of all the dashboard information needed to save the state
     * @param dashboardState.dashboard
     * @param dashboardState.dataset
     * @param stateName Optional name to give to the state
     * @return The ID assigned to the state.
     * @method saveDashboardState
     * @private
     */
    private String saveDashboardState(Map dashboardState, String stateName){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, dashboardTableName)

        if(!exists) {
            st.execute(dashboardCreateScript)
        }
        PreparedStatement psInsert = conn.prepareStatement("insert into " + dashboardTableName + "(id, name, dashboard, dataset) values (?, ?, ?, ?)")
        String uuid = UUID.randomUUID().toString()
        psInsert.setString(1, uuid)
        psInsert.setString(2, (stateName ?: Calendar.getInstance().getTime().toString()))

        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)

        psInsert.setString(3, mapper.writeValueAsString(dashboardState.get("dashboard")))
        psInsert.setString(4, mapper.writeValueAsString(dashboardState.get("dataset")))
        psInsert.executeUpdate()

        psInsert.close()
        st.close()
        conn.close()

        return uuid
    }

    /**
     * Updates the state with the given ID with the new dashboard information.
     * @param dashboardState Map of all the dashboard information needed to overwrite the state
     * @param dashboardState.dashboard
     * @param dashboardState.dataset
     * @param dashboardStateId
     * @return The ID assigned to the state.
     * @method editDashboardState
     * @private
     */
    private String editDashboardState(Map dashboardState, String dashboardStateId){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)

        PreparedStatement ps = conn.prepareStatement("update " + dashboardTableName + " set dashboard=?, dataset=? where id=?")
        ps.setString(1, mapper.writeValueAsString(dashboardState.get("dashboard")))
        ps.setString(2, mapper.writeValueAsString(dashboardState.get("dataset")))
        ps.setString(3, dashboardStateId)
        ps.executeUpdate()

        ps.close()
        st.close()
        conn.close()

        return dashboardStateId
    }

    /**
     * Retrieves the dashboard and dataset information saved for the dashboard state with the given ID.
     * @param id ID of the dashboard state to retrieve information from.
     * @return Map containing the dashboard and dataset information saved in the database.
     * @method getDashboardState
     * @private
     */
    private Map getDashboardState(String id){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, dashboardTableName)
        Map toReturn

        if(exists) {
            ResultSet allResults = st.executeQuery("select dashboard, dataset from " + dashboardTableName + " where id = '" + id + "'")

            if(allResults.next()) {
                ObjectMapperProvider provider = new ObjectMapperProvider()
                ObjectMapper mapper = provider.getContext(NeonModule)

                toReturn = [:]
                toReturn.put("dashboard", mapper.readValue(allResults.getString("dashboard"),  new TypeReference<List<Object>>(){}))
                toReturn.put("dataset", mapper.readValue(allResults.getString("dataset"),  new TypeReference<Map>(){}))
            }

            allResults.close()
        }

        st.close()
        conn.close()

        return toReturn
    }

    /**
     * Finds all names in the given table.
     * @param tableName
     * @return List of all saved state names.
     * @method getAllStateNames
     * @private
     */
    private List<String> getAllStateNames(String tableName){
        Connection conn = DriverManager.getConnection(connectionURL)
        boolean exists = doesDBExist(conn, tableName)
        List<String> toReturn = []

        if(exists) {
            Statement st = conn.createStatement()
            ResultSet allResults = st.executeQuery("select name from " + tableName)

            while (allResults.next()) {
                toReturn.add(allResults.getString(1))
            }

            allResults.close()
            st.close()
        }

        conn.close()

        return toReturn
    }

    /**
     * Finds the ID of the state with the given name.
     * @param stateName Name of the state to find the ID for.
     * @param Table name from which to look for the state.
     * @return State ID for the given name from the given table.
     * @method getStateId
     * @private
     */
    private String getStateId(String stateName, String tableName){
        Connection conn = DriverManager.getConnection(connectionURL)
        boolean exists = doesDBExist(conn, tableName)
        String id = ""

        if(exists) {
            Statement st = conn.createStatement()
            ResultSet allResults = st.executeQuery("select id from " + tableName + " where name = '" + stateName + "'")

            if(allResults.next()) {
                id = allResults.getString(1)
            }

            st.close()
            allResults.close()
        }

        conn.close()

        return id
    }

    /**
     * Finds the name of the state with the given ID.
     * @param stateId ID of the state to find the name for.
     * @param Table name from which to look for the state.
     * @return State name for the given ID from the given table.
     * @method getStateName
     * @private
     */
    private String getStateName(String stateId, String tableName){
        Connection conn = DriverManager.getConnection(connectionURL)
        boolean exists = doesDBExist(conn, tableName)
        String name = ""

        if(exists) {
            Statement st = conn.createStatement()
            ResultSet allResults = st.executeQuery("select name from " + tableName + " where id = '" + stateId + "'")

            if(allResults.next()) {
                name = allResults.getString(1)
            }

            st.close()
            allResults.close()
        }

        conn.close()

        return name
    }

    /**
     * Saves the filter state to the database.
     * @param stateName Optional name to give to the state.
     * @return The ID assigned to the state.
     * @method saveFilterState
     * @private
     */
    private String saveFilterState(String stateName){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, filterTableName)
        if(!exists) {
            st.execute(filterCreateScript)
        }
        PreparedStatement psInsert = conn.prepareStatement("insert into " + filterTableName + "(id, filterstate, name) values (?, ?, ?)")
        String uuid = UUID.randomUUID().toString()

        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)
        String allFiltersAsString = mapper.writeValueAsString(filterService.getFilters("*", "*"))

        psInsert.setString(1, uuid)
        psInsert.setString(2, allFiltersAsString)
        psInsert.setString(3, (stateName?: Calendar.getInstance().getTime().toString()))
        psInsert.executeUpdate()

        psInsert.close()
        st.close()
        conn.close()

        return uuid
    }

    /**
     * Updates the state with the given ID with the current filters.
     * @param filterStateId
     * @return The ID assigned to the state.
     * @method editFilterState
     * @private
     */
    private String editFilterState(String filterStateId){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)

        PreparedStatement ps = conn.prepareStatement("update " + filterTableName + " set filterstate=? where id=?")
        ps.setString(1, mapper.writeValueAsString(filterService.getFilters("*", "*")))
        ps.setString(2, filterStateId)
        ps.executeUpdate()

        ps.close()
        st.close()
        conn.close()

        return filterStateId
    }

    /**
     * Retrieves the filters saved for the filter state with the given ID.
     * @param id ID of the filter state to retrieve information from.
     * @return List of FilterKey objects that were in the saved filter.
     * @method getFilterState
     * @private
     */
    private List<FilterKey> getFilterState(@PathParam("id") String id){
        Connection conn = DriverManager.getConnection(connectionURL)
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(conn, filterTableName)
        List<FilterKey> filterState

        if(exists) {
            ResultSet allResults = st.executeQuery("select filterstate from " + filterTableName + " where id = '" + id + "'")

            if(allResults.next()) {
                ObjectMapperProvider provider = new ObjectMapperProvider()
                ObjectMapper mapper = provider.getContext(NeonModule)
                filterState = mapper.readValue(allResults.getString(1),  new TypeReference<List<FilterKey>>(){})
            }

            allResults.close()
        }

        st.close()
        conn.close()

        return filterState
    }

    /**
     * Determines if the given table name exists in the database.
     * @param conn
     * @param tableName
     * @return True if the table exists in the database, false otherwise.
     * @method doesDBExist
     * @private
     */
    private boolean doesDBExist(Connection conn, String tableName) {
        try {
            Statement st = conn.createStatement()
            st.execute("select count(*) from " + tableName)
        } catch (SQLException sqle) {
            String theError = (sqle).getSQLState()
            if (theError == "42X05") {
                // Table does not exist
                return false
            } else if (theError == "42X14" || theError == "42821") {
                // Incorrect table definition
                throw sqle
            } else {
                // Unhandled SQLException
                throw sqle
            }
        }
        return true
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