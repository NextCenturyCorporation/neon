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

import org.junit.Before
import org.junit.Test

class ConnectionManagerTest {

    private ConnectionManager connectionManager
    private final def fakeMongoConnectionClient = { "mongo" } as ConnectionClient
    private final def fakeHiveConnectionClient = { "hive" } as ConnectionClient


    @Before
    void setup() {
        connectionManager = new ConnectionManager()
        connectionManager.currentRequestConnection = new CurrentRequestConnection()
        connectionManager.mongoConnectionFactory = { fakeMongoConnectionClient } as ConnectionClientFactory
        connectionManager.hiveConnectionFactory = { fakeHiveConnectionClient } as ConnectionClientFactory
    }

    @Test(expected = NeonConnectionException)
    void "no connection set throws an exception"() {
        // nothing explicit to do here, just verifying that this throws an exception
        connectionManager.connection
    }

    @Test
    void "connecting to data sources"() {
        connectionManager.currentRequest = new ConnectionInfo(host: "aHost", dataSource: DataSources.mongo)
        assert connectionManager.connection == fakeMongoConnectionClient

        connectionManager.currentRequest = new ConnectionInfo(host: "aHost", dataSource: DataSources.hive)
        assert connectionManager.connection == fakeHiveConnectionClient
    }

}
