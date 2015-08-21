/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.query.elasticsearch

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
        return null
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing SHOW DATABASES")
        return null
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing SHOW TABLES IN {}", dbName)
        return null
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        return null
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit = 40) {
        return null
    }
}
