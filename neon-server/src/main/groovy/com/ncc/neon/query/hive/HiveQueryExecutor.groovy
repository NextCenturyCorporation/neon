package com.ncc.neon.query.hive

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.*
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.connect.JdbcClient
import com.ncc.neon.query.Transform
import com.ncc.neon.transform.Transformer
import com.ncc.neon.transform.TransformerNotFoundException
import com.ncc.neon.transform.TransformerRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.sql.SQLException



@Component
class HiveQueryExecutor implements QueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveQueryExecutor)

    @Autowired
    TransformerRegistry registry

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult execute(Query query, QueryOptions options) {
        return runAndRelease { client -> executeQuery(client, query, options) }
    }

    private QueryResult executeQuery(JdbcClient client, Query query, QueryOptions options) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState: filterState, selectionState: selectionState)
        String hiveQuery = conversionStrategy.convertQuery(query, options)
        LOGGER.debug("Hive Query: {}", hiveQuery)
        int offset = query.offsetClause ? query.offsetClause.offset : 0
        List<Map> resultList = client.executeQuery(hiveQuery, offset)
        QueryResult result = new TableQueryResult(data: resultList)
        return transform(query.transform, result)
    }

    QueryResult transform(Transform transform, QueryResult queryResult) {
        if(!transform){
            return queryResult
        }

        String transformName = transform.transformName
        Transformer transformer = registry.getTransformer(transformName)
        if(!transformer){
            throw new TransformerNotFoundException("Transform ${transformName} does not exist.")
        }

        return transformer.convert(queryResult, transform.params)
    }

    @Override
    QueryResult execute(QueryGroup queryGroup, QueryOptions options) {
        return runAndRelease { client ->
            TableQueryResult queryResult = new TableQueryResult()
            queryGroup.queries.each {
                def result = executeQuery(client, it, options)
                queryResult.data.addAll(result.data)
            }
            return queryResult
        }
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing Hive SHOW DATABASES")
        return runAndRelease { client ->
            client.executeQuery("SHOW DATABASES").collect { Map<String, String> map ->
                map.get("database_name")
            }
        }
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing Hive SHOW TABLES on database {}", dbName)
        return runAndRelease { client ->
            client.executeQuery("SHOW TABLES IN " + dbName).collect { Map<String, String> map ->
                map.get("tab_name")
            }
        }
    }

    @Override
    QueryResult getFieldNames(String databaseName, String tableName) {
        try {
            def columns = runAndRelease { client -> client.getColumnNames(databaseName, tableName) }
            return new ListQueryResult(columns)
        }
        catch (SQLException ex) {
            LOGGER.error("Columns cannot be found ", ex)
            return new ListQueryResult()
        }
    }

    private JdbcClient getJdbcClient() {
        return connectionManager.connectionClient
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
