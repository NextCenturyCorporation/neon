package com.ncc.neon.connect

import org.junit.Before
import org.junit.Test
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
