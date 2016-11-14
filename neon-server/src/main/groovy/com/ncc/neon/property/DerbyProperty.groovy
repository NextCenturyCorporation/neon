/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.property

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.state.exceptions.StateServiceException

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.annotation.Autowired

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException

import java.sql.*

@Component
class DerbyProperty implements PropertyInterface {

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${propertiesDatabaseName}')
    String propertiesDatabaseName

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClient}')
    String derbyClient

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClientHost}')
    String derbyClientHost

    @SuppressWarnings("GStringExpressionWithinString")
    @Value('\${derbyClientPort}')
    String derbyClientPort

    private final String propertiesTableName = "properties"

    private final String propertiesCreateScript = "create table " + propertiesTableName +
                                                 "(id varchar(36) not null constraint id_pk_dt primary key, " +
                                                 "value long varchar)"

    @Autowired
    private ConnectionManager connectionManager

    public Map getProperty(String key) {
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, propertiesDatabaseName)
        Map toReturn = [key: key, value: null]
        if (exists) {
            ResultSet allResults = st.executeQuery("select id, value from " + propertiesTableName + " where id = '" + key + "'")

            if (allResults.next()) {
                toReturn.put("value", allResults.getString("value"))
            }

            allResults.close()
        }

        st.close()
        return toReturn
    }

    public void setProperty(String key, String value) {
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, propertiesDatabaseName)
        if (!exists) {
            st.execute(propertiesCreateScript)
        }

        try {
            PreparedStatement psInsert = conn.prepareStatement("insert into " + propertiesTableName + "(id, value) values (?, ?)")
            psInsert.setString(1, key)
            psInsert.setString(2, value)
            psInsert.executeUpdate()

            psInsert.close()
        } catch (DerbySQLIntegrityConstraintViolationException e) {
            // If the key already exists, an INSERT will fail because of duplicate ids, so update it
            PreparedStatement psUpdate = conn.prepareStatement("update " + propertiesTableName + " set value=? where id=?")
            psUpdate.setString(1, value)
            psUpdate.setString(2, key)
            psUpdate.executeUpdate()
            psUpdate.close()
        }
        st.close()
    }

    public void remove(String key) {
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, propertiesDatabaseName)
        if (exists) {
            st.execute("delete from " + propertiesTableName + " where id = '" + key + "'")
        }

        st.close()
    }

    public Set<String> propertyNames() {
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, propertiesDatabaseName)
        Set<String> toReturn = [] as Set
        if (exists) {
            ResultSet allResults = st.executeQuery("select id from " + propertiesTableName)

            while (allResults.next()) {
                toReturn.add(allResults.getString("id"))
            }

            allResults.close()
        }

        st.close()
        return toReturn
    }

    public void removeAll() {
        Connection conn = getClient()
        Statement st = conn.createStatement()
        boolean exists = doesDBExist(st, propertiesDatabaseName)
        if (exists) {
            st.execute("delete from " + propertiesTableName)
        }

        st.close()
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
            connInfo = new ConnectionInfo(dataSource: DataSources.derby, databaseName: propertiesDatabaseName, host: (host + ":" + port))
        } else {
            connInfo = new ConnectionInfo(dataSource: DataSources.derby, databaseName: propertiesDatabaseName)
        }

        connectionManager.currentRequest = connInfo
        return connectionManager.connection.connection
    }
}
