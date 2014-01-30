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

import com.mchange.v2.c3p0.ComboPooledDataSource


class JdbcConnectionClientFactory implements ConnectionClientFactory {

    private ComboPooledDataSource dataSource
    private final String databaseType
    private final String driverName
    private final Object lock = new Object()

    public JdbcConnectionClientFactory(String driverName, String databaseType) {
        this.driverName = driverName
        this.databaseType = databaseType
    }

    @Override
    ConnectionClient createConnectionClient(ConnectionInfo info) {
        // the data source is created lazily because ConnectionManager.connect calls
        // connectionCache.put(info,createClientFactory) as part of its connect method, so a new instance of
        // this class will be created even if it already exists in the map. initializing the pool when this connection
        // is actually used prevents multiple pools to the same connection from being instantiated

        // because of the lazy initialization, we need to synchronize on the creation of the data source to avoid
        // possibly creating it twice
        synchronized (lock) {
            if (!dataSource) {
                configureConnectionPool(info)
            }
        }
        return new JdbcClient(dataSource.getConnection("", ""))
    }

    private void configureConnectionPool(ConnectionInfo info) {
        this.dataSource = new ComboPooledDataSource()
        this.dataSource.setDriverClass(driverName)
        this.dataSource.setJdbcUrl("jdbc:${databaseType}://${info.connectionUrl}")
        addShutdownHook {
            dataSource.close()
        }
    }
}
