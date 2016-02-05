package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.executor.QueryExecutor
import com.ncc.neon.query.mongo.MongoHeatmapExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.Resource

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

/**
 * Creates the appropriate query executor implementation from the current connection.
 */
@Component
class QueryExecutorFactory {

    @Resource
    private QueryExecutor mongoQueryExecutor

    @Resource
    private QueryExecutor sparkSQLQueryExecutor

    @Resource
    private QueryExecutor elasticSearchQueryExecutor

    @Resource
    private MongoHeatmapExecutor mongoHeatmapExecutor

    @Resource
    private QueryExecutor elasticSearchHeatmapExecutor

    @Autowired
    private ConnectionManager connectionManager

    /**
     * Gets a query executor to execute a query against the specified connection. The
     * executor is valid for the current request only.
     * @param connectionInfo
     * @return the appropriate query executor
     */
    QueryExecutor getExecutor(ConnectionInfo connectionInfo, boolean heatmapQuery = false) {
        connectionManager.currentRequest = connectionInfo
        DataSources databaseType = connectionInfo.dataSource
        if(heatmapQuery) {
            switch (databaseType) {
                case DataSources.mongo:
                    return mongoHeatmapExecutor
                case DataSources.elasticsearch:
                    return elasticSearchHeatmapExecutor
            }
        }

        switch (databaseType) {
            case DataSources.mongo:
                return mongoQueryExecutor
            case DataSources.sparksql:
                return sparkSQLQueryExecutor
            case DataSources.elasticsearch:
                return elasticSearchQueryExecutor
            default:
                throw new NeonConnectionException("Unsupported database type ${databaseType}")
        }
    }

}
