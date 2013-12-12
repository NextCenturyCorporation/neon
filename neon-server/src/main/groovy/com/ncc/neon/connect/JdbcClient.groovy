package com.ncc.neon.connect

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import java.sql.*

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

/**
 * Wrapper for JDBC API
 */
//We have to suppress this warning in order to load the the JDBC driver via DriverManager. See NEON-459
@SuppressWarnings("SynchronizedMethod")
class JdbcClient implements ConnectionClient{

    private Connection connection

    JdbcClient(Connection connection) {
        this.connection = connection
    }

    /**
     * Each jdbcClient instance is created per session,
     * This method is synchronized because a user cannot perform multiple simultaneous queries
     * without hive blowing up.
     */
    synchronized List executeQuery(String query) {
        Statement statement
        ResultSet resultSet
        try {
            statement = connection.createStatement()
            resultSet = statement.executeQuery(query)
            return createMappedValuesFromResultSet(resultSet)
        }
        finally {
            resultSet?.close()
            statement?.close()
        }

        return []
    }

    private List createMappedValuesFromResultSet(ResultSet resultSet) {
        List resultList = []
        ResultSetMetaData metadata = resultSet.metaData
        int columnCount = metadata.columnCount
        while (resultSet.next()) {
            def result = [:]
            for (ii in 1..columnCount) {
                result[metadata.getColumnName(ii)] = getValue(metadata, resultSet, ii)
            }
            resultList.add(result)
        }
        return resultList
    }

    private def getValue(ResultSetMetaData metadata, ResultSet resultSet, int index) {
        def val = resultSet.getObject(index)
        // timestamps are time-zone less, but we assume UTC
        if (metadata.getColumnType(index) == Types.TIMESTAMP) {
            // use joda time because not all jdbc drivers (e.g. hive) support timezones - they return in local time
            val = new DateTime(val.time).withZoneRetainFields(DateTimeZone.UTC).toDate()
        }
        return val
    }

    /**
     * Each jdbcClient instance is created per session,
     * This method is synchronized because a user cannot perform multiple simultaneous queries
     * without hive blowing up.
     */
    synchronized void execute(String query) {
        Statement statement
        try {
            statement = connection.createStatement()
            statement.execute(query)
        }
        finally {
            statement?.close()
        }
    }

    List<String> getColumnNames(String dataStoreName, String databaseName) {
        String query = "select * from ${dataStoreName}.${databaseName} limit 1"

        List list = executeQuery(query)
        if (!list) {
            return []
        }
        list[0].keySet().asList()
    }

    @Override
    void close() {
        connection?.close()
        connection = null
    }

}
