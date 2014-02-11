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

    @Before
    void setup(){
        connectionManager = new ConnectionManager()
        connectionManager.sessionConnection = new SessionConnection()
    }

    @Test(expected = NeonConnectionException)
    void "no connections set throws an exception"() {
        connectionManager.getCurrentConnectionInfo()
    }

    @Test(expected = NeonConnectionException)
    void "invalid connection info throws exception"() {
        connectionManager.connect(new ConnectionInfo())
        connectionManager.getCurrentConnectionInfo()
    }

    @Test
    void "connecting to data sources returns the last one established"() {
        ConnectionInfo mongo = new ConnectionInfo(dataSource: DataSources.mongo)
        connectionManager.connect(mongo)
        assert connectionManager.getCurrentConnectionInfo() == mongo

        ConnectionInfo hive = new ConnectionInfo(dataSource: DataSources.hive)
        connectionManager.connect(hive)
        assert connectionManager.getCurrentConnectionInfo() == hive

        connectionManager.connect(mongo)
        assert connectionManager.getCurrentConnectionInfo() == mongo
    }

}
