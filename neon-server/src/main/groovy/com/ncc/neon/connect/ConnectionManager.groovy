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
 * Provides a way for a client to get a connection to a database
 */
@Component
class ConnectionManager {


    private final ConcurrentMap<ConnectionInfo, ConnectionClient> clients = new ConcurrentHashMap<ConnectionInfo, MongoConnectionClient>()

    ConnectionClientFactory mongoConnectionFactory = new MongoConnectionClientFactory()
    ConnectionClientFactory sparkSQLConnectionFactory = new JdbcConnectionClientFactory("org.apache.hive.jdbc.HiveDriver", "hive2")
    ConnectionClientFactory elasticSearchConnectionFactory = new ElasticSearchConnectionClientFactory()
    ConnectionClientFactory elasticSearchRestConnectionFactory = new ElasticSearchRestConnectionClientFactory()
    DerbyConnectionClientFactory derbyConnectionFactory = new DerbyConnectionClientFactory()


    // note this current request connection is a unique object per request (the bean has a request scope annotation on it)
    @Autowired
    private CurrentRequestConnection currentRequestConnection

    /**
     * Gets a connection to use for the current query
     * @return
     */
    ConnectionClient getConnection() {
        validateCurrentRequestConnection()
        ConnectionInfo connectionInfo = currentRequestConnection.connectionInfo
        clients.putIfAbsent(connectionInfo, new ConnectionClientHolder(createClientFactory(connectionInfo)))
        ConnectionClientHolder clientHolder = clients.get(connectionInfo)
        def connection = clientHolder.connection
        if (!connection) {
            // initClient is threadsafe so even if two threads get here at the same time because they both request
            // the initial connection at the same time, the connection will only be initialized once
            connection = clientHolder.initClient(connectionInfo)
        }
        return connection
    }

    /**
     * Sets the connection that is being used for the current request. This will only affect the current request.
     * @param connection
     */
    void setCurrentRequest(ConnectionInfo connectionInfo) {
        currentRequestConnection.connectionInfo = connectionInfo
    }

    private void validateCurrentRequestConnection() {
        if ( !currentRequestConnection.connectionInfo) {
            throw new NeonConnectionException("No connection set for the current request")
        }
    }

    private ConnectionClientFactory createClientFactory(ConnectionInfo connectionInfo) {
        if (connectionInfo.dataSource == DataSources.mongo) {
            return mongoConnectionFactory
        }
        if (connectionInfo.dataSource == DataSources.sparksql) {
            return sparkSQLConnectionFactory
        }
        if(connectionInfo.dataSource == DataSources.elasticsearch) {
            return elasticSearchConnectionFactory
        }
        if(connectionInfo.dataSource == DataSources.elasticsearchrest) {
            return elasticSearchRestConnectionFactory
        }
        if(connectionInfo.dataSource == DataSources.derby) {
            return derbyConnectionFactory
        }
        throw new NeonConnectionException("Could not connect to data source of type ${connectionInfo.dataSource}")
    }

}
