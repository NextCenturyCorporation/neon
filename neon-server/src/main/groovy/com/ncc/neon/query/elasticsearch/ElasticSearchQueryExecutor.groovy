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

import java.util.Arrays
import java.util.HashMap

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.Query

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest
import org.elasticsearch.cluster.metadata.MappingMetaData
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.client.Client
import org.elasticsearch.cluster.metadata.IndexMetaData

@Component
class ElasticSearchQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchQueryExecutor)

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    private Client getClient() {
        return connectionManager.connection.client
    }

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        return null
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing showDatabases to retrieve indices")
        Client client = getClient()

        def stateResponse = client.admin.cluster.state {}
        def indices = stateResponse.actionGet().state.metaData.indices().key().toArray(String)

        return Arrays.asList(indeces);
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing showTables for index " + dbName + " to get type mappings")

        Client client = getClient()

        def mappingsResponse = client.admin.indices.getMappings {}

        def mappings = mappingsResponse.actionGet().mappings().get(dbName).keys().toArray(String)
        return Arrays.asList(mappings)
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        LOGGER.debug("Executing getFieldNames for index " + databaseName + " type " + tableName)

        Client client = getClient()

        def fieldsResponse = client.admin().indices().getMappings {}
        def fields = fieldsResponse.actionGet().mappings().get(databaseName).get(tableName).getSourceAsMap().get('properties')
        return new ArrayList<String>(fields.keySet());;
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit = 40) {
        return null
    }
}
