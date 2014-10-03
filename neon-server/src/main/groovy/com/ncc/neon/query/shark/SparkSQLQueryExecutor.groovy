/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.query.shark
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.JdbcClient
import com.ncc.neon.query.*
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.sql.SQLException

@Component
class SparkSQLQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkSQLQueryExecutor)

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager


    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        return runAndRelease { client ->
            SharkConversionStrategy conversionStrategy = new SharkConversionStrategy(filterState: filterState, selectionState: selectionState)
            String sharkQuery = conversionStrategy.convertQuery(query, options)
            LOGGER.debug("Query: {}", sharkQuery)
            int offset = query.offsetClause ? query.offsetClause.offset : 0
            List<Map> resultList = client.executeQuery(sharkQuery, offset)
            return  new TabularQueryResult(resultList)
        }
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing SHOW DATABASES")
        return runAndRelease { client ->
            client.executeQuery("SHOW DATABASES").collect { Map<String, String> map ->
                map.get("result")
            }
        }
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing SHOW TABLES IN {}", dbName)
        return runAndRelease { client ->
            client.executeQuery("SHOW TABLES IN " + dbName).collect { Map<String, String> map ->
                map.get("result")
            }
        }
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        try {
            def columns = runAndRelease { client -> client.getColumnNames(databaseName, tableName) }
            return columns
        }
        catch (SQLException ex) {
            LOGGER.error("Columns cannot be found ", ex)
            return []
        }
    }

    private JdbcClient getJdbcClient() {
        return connectionManager.connection.jdbcClient
    }

    /**
     * Runs the closure containing a query to run and releases the connection back into the pool
     * @param query
     */
    private def runAndRelease(Closure query) {
        // use the explicit getter here to make it clear since the getter actually will grab a connection
        // from the pool
        def client = getJdbcClient()
        try {

            return query.call(client)
        }
        finally {
            // neon uses connection pooling, so all this does is release it back into the pool
            client?.close()
        }
    }
}
