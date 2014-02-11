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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap



/**
 * This holds the connection information for the application.
 */

class ConnectionManager {

    private final ConcurrentMap<ConnectionInfo, ConnectionClientFactory> connectionCache = [:] as ConcurrentHashMap

    @Autowired
    SessionConnection sessionConnection

    /** the default connection used if the session is not explicitly connected to one */
    private ConnectionInfo defaultConnection


    void connect(ConnectionInfo info) {
        sessionConnection.connectionInfo = info
        connectionCache.putIfAbsent(info, createClientFactory(info))
    }

    void initConnectionManager(ConnectionInfo info){
        connectionCache.clear()
        if(info){
            connectionCache.put(info, createClientFactory(info))
        }
        defaultConnection = info

    }

    ConnectionClient getConnectionClient(){
        return createConnectionClient(getCurrentConnectionInfo())
    }

    ConnectionClient getDefaultConnectionClient() {
        return createConnectionClient(defaultConnection)
    }

    ConnectionInfo getCurrentConnectionInfo(){
        ConnectionInfo connection = sessionConnection.connectionInfo ?: defaultConnection
        if ( !connection ) {
            throw new NeonConnectionException("No default or session connections exist")
        }
        return connection
    }

    private ConnectionClient createConnectionClient(ConnectionInfo info) {
        ConnectionClientFactory factory = connectionCache.get(info)
        return factory.createConnectionClient(info)
    }

    private ConnectionClientFactory createClientFactory(ConnectionInfo connectionInfo) {
        if (connectionInfo.dataSource == DataSources.mongo) {
            return new MongoConnectionClientFactory()
        }
        if (connectionInfo.dataSource == DataSources.hive) {
            return new JdbcConnectionClientFactory("org.apache.hive.jdbc.HiveDriver", "hive2")
        }
        throw new NeonConnectionException("There must be a data source to which to connect.")
    }
}
