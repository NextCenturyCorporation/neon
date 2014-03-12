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
/**
 * A factory that maintains JDBC connection pools for each unique host neon connects to.
 *
 * Note this class is not threadsafe.
 */
class JdbcConnectionClientFactory implements ConnectionClientFactory {

    private final String databaseType
    private final String driverName

    public JdbcConnectionClientFactory(String driverName, String databaseType) {
        this.driverName = driverName
        this.databaseType = databaseType
    }

    @Override
    ConnectionClient createConnectionClient(ConnectionInfo info) {
        return new JdbcClientPool(info, driverName, databaseType)
    }

}
