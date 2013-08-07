package com.ncc.neon.query.hive
import com.ncc.neon.query.*
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.jdbc.JdbcClient
import com.ncc.neon.query.jdbc.JdbcQueryResult
import com.ncc.neon.selection.SelectionManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

class HiveQueryExecutor implements QueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveQueryExecutor)

    private final SelectionManager selectionManager = new SelectionManager()
    private final FilterState filterState = new FilterState()
    private final JdbcClient jdbcClient

    HiveQueryExecutor(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient
    }

    @Override
    QueryResult execute(Query query, boolean includedFiltered) {
        String hiveQuery = createHiveQuery(query, includedFiltered)
        LOGGER.debug("Hive Query: {}", hiveQuery)

        List<Map> resultList = jdbcClient.executeQuery(hiveQuery)
        return new JdbcQueryResult(resultList: resultList)
    }

    @Override
    QueryResult execute(QueryGroup query, boolean includeFiltered) {
        QueryGroupResult queryGroupResult = new QueryGroupResult()
        query.namedQueries.each {
            def result = execute(it.query, includeFiltered)
            queryGroupResult.namedResults[it.name] = result
        }
        return queryGroupResult
    }

    @Override
    Collection<String> getFieldNames(String databaseName, String tableName) {
        return jdbcClient.getColumnNames(databaseName, tableName)
    }

    @Override
    UUID addFilter(Filter filter) {
        return filterState.addFilter(filter)
    }

    @Override
    void removeFilter(UUID id) {
        filterState.removeFilter(id)
    }

    @Override
    void clearFilters() {
        filterState.clearFilters()
    }

    @Override
    void setSelectionWhere(Filter filter) {
        throw new UnsupportedOperationException("We can't set a selection through hive, since there is no ID field.")
    }

    @Override
    void setSelectedIds(Collection<Object> ids) {
        selectionManager.replaceSelectionWith(ids)
    }

    @Override
    void addSelectedIds(Collection<Object> ids) {
        selectionManager.addIds(ids)
    }

    @Override
    void removeSelectedIds(Collection<Object> ids) {
        selectionManager.removeIds(ids)
    }

    @Override
    void clearSelection() {
        selectionManager.clear()
    }

    @Override
    QueryResult getSelectionWhere(Filter filter) {
        Query query = QueryUtils.queryFromFilter(filter)
        execute(query, true)
    }

    @Override
    List<String> showDatabases() {
        jdbcClient.executeQuery("SHOW DATABASES").collect{ Map<String,String> map ->
            map.get("database_name")
        }
    }

    @Override
    List<String> showTables(String dbName) {
        try{
            jdbcClient.execute("USE " + dbName)
        }
        catch (SQLException ex){
            return []
        }

        jdbcClient.executeQuery("SHOW TABLES").collect{ Map<String,String> map ->
            map.get("tab_name")
        }
    }

    private String createHiveQuery(Query query, boolean includeFiltered){
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState)
        if(includeFiltered){
            return conversionStrategy.convertQuery(query)
        }
        return conversionStrategy.convertQueryWithFilters(query)
    }
}
