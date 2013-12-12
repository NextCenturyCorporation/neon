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

class JdbcConnectionClientFactory implements ConnectionClientFactory{

    private final ComboPooledDataSource dataSource
    private final String databaseType
    private final String databaseName

    public JdbcConnectionClientFactory(String driverName, String databaseType, String databaseName){
        this.databaseType = databaseType
        this.databaseName = databaseName

        this.dataSource = new ComboPooledDataSource()
        this.dataSource.setDriverClass(driverName)
    }

    @Override
    ConnectionClient createConnectionClient(ConnectionInfo info) {
        if(info.dataSource != DataSources.hive){
            throw new NeonConnectionException("JDBC clients should only be created for jdbc connections")
        }
        dataSource.setJdbcUrl("jdbc:${databaseType}://${info.connectionUrl}/${databaseName}")

        return new JdbcClient(dataSource.getConnection("",""))
    }
}
