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
 */

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
