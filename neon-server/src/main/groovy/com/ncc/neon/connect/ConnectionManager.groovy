package com.ncc.neon.connect
import org.springframework.beans.factory.annotation.Autowired

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
 * @author tbrooks
 */

/**
 * This holds the connection information for the application.
 */

class ConnectionManager {

    private final ConcurrentMap<ConnectionInfo, ConnectionClientFactory> connectionCache = [:] as ConcurrentHashMap

    @Autowired
    SessionConnection sessionConnection

    void connect(ConnectionInfo info) {
        sessionConnection.connectionInfo = info
        connectionCache.putIfAbsent(info, createClientFactory(info))
    }

    void initConnectionManager(ConnectionInfo info){
        connectionCache.clear()
        if(info){
            connectionCache.put(info, createClientFactory(info))
        }
    }

    ConnectionClient getConnectionClient(){
        return createConnectionClient(getCurrentConnectionInfo())
    }

    ConnectionInfo getCurrentConnectionInfo(){
        if(connectionCache.size() == 0){
            throw new NeonConnectionException("No known connections exist")
        }
        if(connectionCache.size() == 1){
            return connectionCache.keySet().iterator().next()
        }
        return sessionConnection.connectionInfo
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
            return new JdbcConnectionClientFactory("org.apache.hadoop.hive.jdbc.HiveDriver", "hive")
        }
        throw new NeonConnectionException("There must be a data source to which to connect.")
    }
}
