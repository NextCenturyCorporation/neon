package com.ncc.neon.connect
import com.ncc.neon.cache.ImmutableValueCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
 * @author tbrooks
 */

/**
 * This holds the connection information for the application.
 */
@Component
class ConnectionManager {

    private final ImmutableValueCache<String, ConnectionInfo> connections = new ImmutableValueCache()
    private final ImmutableValueCache<String, ConnectionClientFactory> factoryCache = new ImmutableValueCache()

    ConnectionClientFactory mongoConnectionFactory = new MongoConnectionClientFactory()
    ConnectionClientFactory hiveConnectionFactory = new JdbcConnectionClientFactory("org.apache.hive.jdbc.HiveDriver", "hive2")

    @Autowired
    CurrentRequestConnection currentRequestConnection

    ConnectionInfo getConnectionById(String connectionId) {
        return connections.get(connectionId)
    }

    /**
     * Registers a connection resource with the application.
     * @param info Fully identifies the connection information
     * @return A key that identifies this connection resource.
     */

    String connect(ConnectionInfo info) {
        String connectionId = createIdFromInfo(info)
        connections.put(connectionId, info)
        factoryCache.put(connectionId, createClientFactory(info))
        return connectionId
    }

    ConnectionClient getCurrentConnectionClient(){
        return getConnectionClient(currentRequestConnection.connectionId)
    }

    private ConnectionClient getConnectionClient(String connectionId){
        ConnectionClientFactory factory = factoryCache.get(connectionId)
        if(!factory){
            throw new NeonConnectionException("Connection to ${connectionId} was never established.")
        }

        return factory.createConnectionClient(connections.get(connectionId))
    }

    private String createIdFromInfo(ConnectionInfo info) {
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
