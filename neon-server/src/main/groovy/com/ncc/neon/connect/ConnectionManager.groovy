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

package com.ncc.neon.connect

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap



/**
 * This holds the connection information for the application.
 */
@Component
class ConnectionManager {

    private final ConcurrentMap<String, ConnectionInfo> connections = new ConcurrentHashMap()
    private final ConcurrentMap<String, ConnectionClientFactory> factoryCache = new ConcurrentHashMap()

    ConnectionClientFactory mongoConnectionFactory = new MongoConnectionClientFactory()
    ConnectionClientFactory hiveConnectionFactory = new JdbcConnectionClientFactory("org.apache.hive.jdbc.HiveDriver", "hive2")

    @Autowired
    CurrentRequestConnection currentRequestConnection

    /**
     * Registers a connection resource with the application.
     * @param info Fully identifies the connection information
     * @return A key that identifies this connection resource.
     */

    String connect(ConnectionInfo info) {
        String connectionId = createConnectionId(info)
        connections.putIfAbsent(connectionId, info)
        factoryCache.putIfAbsent(connectionId, createClientFactory(info))
        return connectionId
    }

    /**
     * Removes a connection
     * @param connectionId the id of the connection
     */

    void removeConnection(String connectionId) {
        connections.remove(connectionId)
        factoryCache.remove(connectionId)
    }

    /**
     * Gets the connection for the current request
     * @return The client
     */

    ConnectionClient getCurrentConnectionClient(){
        return getConnectionClient(currentRequestConnection.connectionId)
    }

    /**
     * Get the connection info based on the id
     * @param connectionId The id
     * @return The info
     */

    ConnectionInfo getConnectionById(String connectionId) {
        return connections.get(connectionId)
    }

    /**
     * Gets all the registered connections ids.
     * @return the ids
     */
    Set<String> getAllConnectionIds() {
        return connections.keySet()
    }


    private ConnectionClient getConnectionClient(String connectionId){
        ConnectionClientFactory factory = factoryCache.get(connectionId)
        if(!factory){
            throw new NeonConnectionException("Connection to ${connectionId} was never established.")
        }

        return factory.createConnectionClient(connections.get(connectionId))
    }

    private String createConnectionId(ConnectionInfo info) {
        return "${info.dataSource?.name()}@${info.connectionUrl}"
    }

    private ConnectionClientFactory createClientFactory(ConnectionInfo connectionInfo) {
        if (connectionInfo.dataSource == DataSources.mongo) {
            return mongoConnectionFactory
        }
        if (connectionInfo.dataSource == DataSources.hive) {
            return hiveConnectionFactory
        }
        throw new NeonConnectionException("There must be a data source to which to connect.")
    }

}
