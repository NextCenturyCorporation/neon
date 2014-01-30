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
