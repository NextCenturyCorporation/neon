package com.ncc.neon.connect

import com.mchange.v2.c3p0.ComboPooledDataSource

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
                this.dataSource = new ComboPooledDataSource()
                this.dataSource.setDriverClass(driverName)
                this.dataSource.setJdbcUrl("jdbc:${databaseType}://${info.connectionUrl}")
            }
        }
        return new JdbcClient(dataSource.getConnection("", ""))
    }
}
