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
