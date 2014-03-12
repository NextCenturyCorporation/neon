/*
 * Copyright 2014 Next Century Corporation
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

/**
 * A pool of JDBC connections for a specific host
 */
class JdbcClientPool implements ConnectionClient {

    private final ComboPooledDataSource connectionPool

    JdbcClientPool(ConnectionInfo info, String driver, String databaseType) {
        connectionPool = createConnectionPool(info, driver, databaseType)
    }

    private ComboPooledDataSource createConnectionPool(ConnectionInfo info, String driver, String databaseType) {
        ComboPooledDataSource dataSource = new ComboPooledDataSource()
        dataSource.setDriverClass(driver)
        dataSource.setJdbcUrl("jdbc:${databaseType}://${info.host}")
        addShutdownHook {
            close()
        }
        return dataSource
    }

    JdbcClient getJdbcClient() {
        return new JdbcClient(connectionPool.getConnection("",""))
    }

    @Override
    void close() throws IOException {
        connectionPool.close()
    }
}
