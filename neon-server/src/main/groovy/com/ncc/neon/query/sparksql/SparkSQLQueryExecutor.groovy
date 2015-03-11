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

package com.ncc.neon.query.sparksql
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
            SparkSQLConversionStrategy conversionStrategy = new SparkSQLConversionStrategy(filterState: filterState, selectionState: selectionState)
            LOGGER.error("Got a query, checking for aggregations... {}", query.aggregates)
            query.aggregates.each { agg ->
                LOGGER.error("Aggregation {}", agg)
            }
            String sparkSQLQuery = conversionStrategy.convertQuery(query, options)
            LOGGER.debug("Query: {}", sparkSQLQuery)
            int offset = query.offsetClause ? query.offsetClause.offset : 0
            List<Map> resultList = client.executeQuery(sparkSQLQuery, offset)
            // Make sure the aggregate fields have the proper capitalization, since Hive will
            // convert them to lower case (NEON-1009).
            query.aggregates.each { agg ->
                def requestedName = agg.name
                def mangledName = agg.name.toLowerCase()
                // Nothing needs to be done if the requested name was already lower case
                if (requestedName != mangledName) {
                    resultList.each { record ->
                        // Only do the conversion if the requested name is not there but the mangled
                        // name is.
                        if (!record.containsKey(requestedName) && record.containsKey(mangledName)) {
                            def value = record[mangledName]
                            record.remove(mangledName)
                            record[requestedName] = value
                        }
                    }
                }
            }
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
