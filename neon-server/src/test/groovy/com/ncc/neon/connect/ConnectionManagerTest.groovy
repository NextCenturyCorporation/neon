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
    private final def fakeMongoConnectionClient = {"mongo"} as ConnectionClient
    private final def fakeHiveConnectionClient = {"hive"} as ConnectionClient


    @Before
    void setup(){
        connectionManager = new ConnectionManager()

        connectionManager.mongoConnectionFactory = { fakeMongoConnectionClient } as ConnectionClientFactory
        connectionManager.hiveConnectionFactory = { fakeHiveConnectionClient } as ConnectionClientFactory
    }

    @Test(expected = NeonConnectionException)
    void "no connections set throws an exception"() {
        connectionManager.getConnectionClient("")
    }

    @Test(expected = NeonConnectionException)
    void "invalid connection info throws exception"() {
        ConnectionInfo info = new ConnectionInfo()
        connectionManager.connect(info)
        connectionManager.getConnectionClient(info)
    }

    @Test
    void "connecting to data sources"() {
        ConnectionInfo mongo = new ConnectionInfo(dataSource: DataSources.mongo)
        String id = connectionManager.connect(mongo)
        assert connectionManager.getConnectionClient(id) == fakeMongoConnectionClient

        ConnectionInfo hive = new ConnectionInfo(dataSource: DataSources.hive)
        id = connectionManager.connect(hive)
        assert connectionManager.getConnectionClient(id) == fakeHiveConnectionClient
    }

}
