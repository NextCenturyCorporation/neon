package com.ncc.neon.connect

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 */

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
