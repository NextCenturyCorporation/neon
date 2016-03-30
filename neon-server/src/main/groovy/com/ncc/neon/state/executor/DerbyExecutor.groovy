/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.state.executor

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.state.exceptions.StateServiceException
import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.query.jackson.NeonModule
import com.ncc.neon.query.jackson.ObjectMapperProvider

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.type.TypeReference

import java.sql.*

/**
 * Executes queries against a derby data store
 */
@Component
class DerbyExecutor {

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${savedStatesDatabaseName}')
    String savedStatesDatabaseName

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClient}')
    String derbyClient

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClientHost}')
    String derbyClientHost

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClientPort}')
    String derbyClientPort

    @Autowired
    private ConnectionManager connectionManager

    private final String dashboardTableName = "dashboardStates"
    private final String filterTableName = "filterStates"

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
     * Saves the dashboard state to the database.
     * @param dashboardState Map of all the dashboard information needed to save the state
     * @param dashboardState.dashboard
     * @param dashboardState.dataset
     * @param stateName Optional name to give to the state
     * @return The ID assigned to the state.
     * @method saveDashboardState
     */
    String saveDashboardState(Map dashboardState, String stateName){
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, dashboardTableName)

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
     */
    String editDashboardState(Map dashboardState, String dashboardStateId){
        Connection conn = getClient()
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

        return dashboardStateId
    }

    /**
     * Retrieves the dashboard and dataset information saved for the dashboard state with the given ID.
     * @param id ID of the dashboard state to retrieve information from.
     * @return Map containing the dashboard and dataset information saved in the database.
     * @method getDashboardState
     */
    Map getDashboardState(String id){
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, dashboardTableName)
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

        return toReturn
    }

    /**
     * Finds the ID of the dashboard state with the given name.
     * @param stateName Name of the state to find the ID for.
     * @return State ID for the given name.
     * @method getDashboardStateId
     */
    String getDashboardStateId(String stateName) {
        return getStateId(stateName, dashboardTableName)
    }

    /**
     * Finds the name of the dashboard state with the given ID.
     * @param stateId ID of the state to find the name for.
     * @return State name for the given ID.
     * @method getDashboardStateName
     */
    String getDashboardStateName(String stateId) {
        return getStateName(stateId, dashboardTableName)
    }

    /**
     * Finds all names in the dashboard state table.
     * @return List of all saved dashboard state names.
     * @method getAllDashboardStateNames
     */
    List<String> getAllDashboardStateNames() {
        return getAllStateNames(dashboardTableName)
    }

    /**
     * Saves the filter state to the database.
     * @param filters List of all filters to save
     * @param stateName Optional name to give to the state.
     * @return The ID assigned to the state.
     * @method saveFilterState
     */
    String saveFilterState(List filters, String stateName){
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, filterTableName)
        if(!exists) {
            st.execute(filterCreateScript)
        }
        PreparedStatement psInsert = conn.prepareStatement("insert into " + filterTableName + "(id, filterstate, name) values (?, ?, ?)")
        String uuid = UUID.randomUUID().toString()

        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)
        String allFiltersAsString = mapper.writeValueAsString(filters)

        psInsert.setString(1, uuid)
        psInsert.setString(2, allFiltersAsString)
        psInsert.setString(3, (stateName?: Calendar.getInstance().getTime().toString()))
        psInsert.executeUpdate()

        psInsert.close()
        st.close()

        return uuid
    }

    /**
     * Updates the state with the given ID with the current filters.
     * @param filters
     * @param filterStateId
     * @return The ID assigned to the state.
     * @method editFilterState
     */
    String editFilterState(List filters, String filterStateId){
        Connection conn = getClient()
        Statement st = conn.createStatement()
        ObjectMapperProvider provider = new ObjectMapperProvider()
        ObjectMapper mapper = provider.getContext(NeonModule)

        PreparedStatement ps = conn.prepareStatement("update " + filterTableName + " set filterstate=? where id=?")
        ps.setString(1, mapper.writeValueAsString(filters))
        ps.setString(2, filterStateId)
        ps.executeUpdate()

        ps.close()
        st.close()

        return filterStateId
    }

    /**
     * Retrieves the filters saved for the filter state with the given ID.
     * @param id ID of the filter state to retrieve information from.
     * @return List of FilterKey objects that were in the saved filter.
     * @method getFilterState
     */
    List<FilterKey> getFilterState(String id){
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, filterTableName)
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

        return filterState
    }

    /**
     * Finds the ID of the filter state with the given name.
     * @param stateName Name of the state to find the ID for.
     * @return State ID for the given name.
     * @method getFilterStateId
     */
    String getFilterStateId(String stateName) {
        return getStateId(stateName, filterTableName)
    }

    /**
     * Finds the name of the filter state with the given ID.
     * @param stateId ID of the state to find the name for.
     * @return State name for the given ID.
     * @method getFilterStateName
     */
    String getFilterStateName(String stateId) {
        return getStateName(stateId, filterTableName)
    }

    /**
     * Finds all names in the filter state table.
     * @return List of all saved filter state names.
     * @method getAllFilterStateNames
     */
    List<String> getAllFilterStateNames() {
        return getAllStateNames(filterTableName)
    }

    /**
     * Deletes the states associated with the given name.
     * @param stateName
     * @return Map of the state IDs deleted.
     * @method deleteState
     */
    Map deleteState(String stateName) {
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, dashboardTableName)
        Map toReturn = [:]
        if(exists) {
            ResultSet result = st.executeQuery("select id from " + dashboardTableName + " where name = '" + stateName + "'")

            if(result.next()) {
                toReturn.put("dashboardStateId", result.getString(1))
                st.execute("delete from " + dashboardTableName + " where name = '" + stateName + "'")
            }
        }

        exists = doesDBExist(st, filterTableName)
        if(exists) {
            ResultSet result = st.executeQuery("select id from " + filterTableName + " where name = '" + stateName + "'")

            if(result.next()) {
                toReturn.put("filterStateId", result.getString(1))
                st.execute("delete from " + filterTableName + " where name = '" + stateName + "'")
            }
        }

        st.close()

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
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, tableName)
        List<String> toReturn = []

        if(exists) {
            ResultSet allResults = st.executeQuery("select name from " + tableName)

            while (allResults.next()) {
                toReturn.add(allResults.getString(1))
            }

            allResults.close()
        }

        st.close()

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
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, tableName)
        String id = ""

        if(exists) {
            ResultSet allResults = st.executeQuery("select id from " + tableName + " where name = '" + stateName + "'")

            if(allResults.next()) {
                id = allResults.getString(1)
            }

            allResults.close()
        }

        st.close()

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
        Statement st = getClient().createStatement()
        boolean exists = doesDBExist(st, tableName)
        String name = ""

        if(exists) {
            ResultSet allResults = st.executeQuery("select name from " + tableName + " where id = '" + stateId + "'")

            if(allResults.next()) {
                name = allResults.getString(1)
            }

            allResults.close()
        }

        st.close()

        return name
    }

    /**
     * Determines if the given table name exists in the database.
     * @param statement
     * @param tableName
     * @return True if the table exists in the database, false otherwise.
     * @method doesDBExist
     * @private
     */
    private boolean doesDBExist(Statement statement, String tableName) {
        try {
            statement.execute("select count(*) from " + tableName)
        } catch (SQLException sqle) {
            String theError = sqle.getSQLState()
            if (theError == "42X05") {
                // Table does not exist
                return false
            }
            throw new StateServiceException(sqle.getMessage())
        }
        return true
    }

    private Connection getClient() {
        def connInfo

        if(Boolean.parseBoolean(derbyClient)) {
            def host = derbyClientHost ?: "localhost"
            def port = derbyClientPort ?: 1527
            connInfo = new ConnectionInfo(dataSource: DataSources.derby, databaseName: savedStatesDatabaseName, host: (host + ":" + port))
        } else {
            connInfo = new ConnectionInfo(dataSource: DataSources.derby, databaseName: savedStatesDatabaseName)
        }

        connectionManager.currentRequest = connInfo
        return connectionManager.connection.connection
    }
}