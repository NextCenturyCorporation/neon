package com.ncc.neon.query.hive

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.*
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.jdbc.JdbcClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Resource
import java.sql.SQLException

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

@Component
class HiveQueryExecutor implements QueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveQueryExecutor)

    @Resource
    private FilterState filterState

    @Resource
    private FilterState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult execute(Query query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState, selectionState)
        String hiveQuery = conversionStrategy.convertQueryWithFilterState(query)
        LOGGER.debug("Hive Query: {}", hiveQuery)
        List<Map> resultList = jdbcClient.executeQuery(hiveQuery)
        return new TableQueryResult(data: resultList)
    }

    @Override
    QueryResult execute(QueryGroup query) {
        TableQueryResult queryResult = new TableQueryResult()
        query.queries.each {
            def result = execute(it)
            queryResult.data.addAll(result.data)
        }
        return queryResult
    }

    @Override
    QueryResult executeDisregardingFilters(Query query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState, selectionState)
        String hiveQuery = conversionStrategy.convertQueryDisregardingFilters(query)
        LOGGER.debug("Hive Query: {}", hiveQuery)
        List<Map> resultList = jdbcClient.executeQuery(hiveQuery)
        return new TableQueryResult(data: resultList)
    }

    @Override
    QueryResult executeDisregardingFilters(QueryGroup query) {
        TableQueryResult queryResult = new TableQueryResult()
        query.queries.each {
            def result = executeDisregardingFilters(it)
            queryResult.data.addAll(result.data)
        }
        return queryResult
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing Hive SHOW DATABASES")
        def dbs = jdbcClient.executeQuery("SHOW DATABASES").collect { Map<String, String> map ->
            map.get("database_name")
        }
        return dbs
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing Hive SHOW TABLES on database {}", dbName)
        jdbcClient.execute("USE " + dbName)
        return jdbcClient.executeQuery("SHOW TABLES").collect { Map<String, String> map ->
            map.get("tab_name")
        }
    }

    @Override
    QueryResult getFieldNames(String databaseName, String tableName) {
        try {
            def columns = jdbcClient.getColumnNames(databaseName, tableName)
            return new ListQueryResult(columns)
        }
        catch (SQLException ex) {
            LOGGER.error("Columns cannot be found ", ex)
            return new ListQueryResult()
        }
    }

    private JdbcClient getJdbcClient() {
        connectionManager.client
    }
}
