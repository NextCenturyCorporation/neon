package com.ncc.neon.data

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.sql.Connection
import java.sql.Statement
import com.mchange.v2.c3p0.ComboPooledDataSource

/*
 * ************************************************************************
 * Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
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

class HiveDataDeleter extends DefaultTask {
    static final String DATABASE_NAME = "concurrencytest"
    static final String TABLE_NAME = "records"

    String host = "shark:10000"

    @TaskAction
    void run(){
        def dataSource = new ComboPooledDataSource()
        Connection connection = createConnection(dataSource)
        execute(connection, "drop table if exists ${DATABASE_NAME}.${TABLE_NAME}")
        execute(connection, "drop database if exists ${DATABASE_NAME}")
        connection.close()
        dataSource.close()
    }

    Connection createConnection(dataSource) {
        def driverName = "org.apache.hive.jdbc.HiveDriver"
        def databaseType = "hive2"
        dataSource.setDriverClass(driverName)
        dataSource.setJdbcUrl("jdbc:${databaseType}://${host}/")
        return dataSource.getConnection("","")
    }

    private synchronized void execute(connection, query){
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }
}