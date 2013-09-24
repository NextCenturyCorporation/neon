package com.ncc.neon.session

import com.ncc.neon.connect.Connection
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.MongoConnection
import com.ncc.neon.query.mongo.MongoQueryExecutor
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.connect.HiveConnection
import com.ncc.neon.query.hive.HiveQueryExecutor

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

/**
 * Creates the appropriate query executor implementation from the current connection.
 */

class QueryExecutorFactory {

    static QueryExecutor create(Connection connection, ConnectionInfo info) {
        if (connection instanceof MongoConnection) {
            return new MongoQueryExecutor(connection.connect(info))
        }
        if (connection instanceof HiveConnection) {
            return new HiveQueryExecutor(connection, info)
        }

        throw new IllegalArgumentException("Unable to create database connection.")
    }
}
