package com.ncc.neon.query.jdbc

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement

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
@SuppressWarnings('ClassForName')
class JdbcClient {

    final String driverName
    final String databaseType
    final String databaseName
    final String dbHostString

    JdbcClient(String driverName, String databaseType, String databaseName, String dbHostString) {
        this.driverName = driverName
        this.databaseType = databaseType
        this.databaseName = databaseName
        this.dbHostString = dbHostString

        Class.forName(driverName)
    }

    public List<Map> executeQuery(String query) {
        Connection connection = DriverManager.getConnection("jdbc:" + databaseType + "://" + dbHostString + "/" + databaseName, "", "")
        Statement statement = connection.createStatement()
        ResultSet resultSet = statement.executeQuery(query)
        List<Map> resultList = []
        ResultSetMetaData metadata = resultSet.metaData
        int columnCount = metadata.columnCount
        while (resultSet.next()) {
            Map<String, Object> result = new HashMap<String, Object>() {}
            for (ii in 1..columnCount) {
                result[metadata.getColumnName(ii)] = resultSet.getObject(ii)
            }
            resultList.add(result)
        }
        resultSet.close()
        statement.close()
        connection.close()
        return resultList
    }

    public void execute(String query) {
        Connection connection = DriverManager.getConnection("jdbc:" + databaseType + "://" + dbHostString + "/" + databaseName, "", "")
        Statement statement = connection.createStatement()
        statement.execute(query)
        statement.close()
        connection.close()
    }

    public List<String> getColumnNames(String dataSourceName, String datasetId) {
        Connection connection = DriverManager.getConnection("jdbc:" + databaseType + "://" + dbHostString + "/" + databaseName, "", "")
        DatabaseMetaData metadata = connection.getMetaData()
        ResultSet resultSet = metadata.getColumns(dataSourceName, null, datasetId, null)
        List<String> columnNames = []
        while (resultSet.next()) {
            columnNames << resultSet.getString("COLUMN_NAME")
        }

        return columnNames
    }
}
