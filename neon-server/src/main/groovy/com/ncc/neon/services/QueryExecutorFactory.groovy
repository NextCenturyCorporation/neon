package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.QueryExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Resource


/**
 * Creates the appropriate query executor implementation from the current connection.
 */
@Component
class QueryExecutorFactory {

    @Resource
    private QueryExecutor mongoQueryExecutor

    @Resource
    private QueryExecutor hiveQueryExecutor

    @Autowired
    private ConnectionManager connectionManager

    /**
     * Gets the query executor based on the connection
     * @return the appropriate query executor
     */
    QueryExecutor getExecutor() {
        if(connectionManager.currentConnectionInfo.dataSource == DataSources.hive){
            return hiveQueryExecutor
        }
        return mongoQueryExecutor
    }
}
