/*
 * Copyright 2016 Next Century Corporation
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

import com.mongodb.util.JSON
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.Client
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.util.ResourceNotFoundException

import java.util.regex.Matcher
import java.util.regex.Pattern

@Primary
@Component("elasticSearchRestQueryExecutor")
class ElasticSearchRestQueryExecutor extends AbstractQueryExecutor {

    static final String STATS_AGG_PREFIX = "_statsFor_"

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRestQueryExecutor)

    /*
     * A small class to hold the important information about an aggregation bucket between when the
     * buckets are taken out of ElasticSearch's hierarchical arrangement and when everything can be
     * extracted into the format that the Neon API uses.
     */

    private static class AggregationBucket {
        def getGroupByKeys() {
            return groupByKeys
        }

        def setGroupByKeys(newGroupByKeys) {
            groupByKeys = newGroupByKeys
        }

        def getAggregatedValues() {
            return aggregatedValues
        }

        def setAggregatedValues(newValues) {
            aggregatedValues = newValues
        }

        def getDocCount() {
            return docCount
        }

        def setDocCount(newCount) {
            docCount = newCount
        }

        private Map groupByKeys
        private Map aggregatedValues = [:]
        private def docCount
    }

    @Autowired
    protected FilterState filterState

    @Autowired
    protected SelectionState selectionState

    @Autowired
    protected ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        returnVal = new TabularQueryResult()
        return returnVal
    }

    /**
     * Show the databases in elastic search
     * @return list of index names
     */
    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing showDatabases to retrieve indices")
        def mappingData = getMappings()
        return mappingData.keySet() as List
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing showTables for index " + dbName + " to get type mappings")
        def dbMatch = dbName.replaceAll(/\*/, '.*')
        def tableList = []
        def mappingData = getMappings()
        mappingData.each { k, v ->
            if (k.matches(dbMatch)) {
                def tablenames = v['mappings'].keySet()
                tableList.addAll(tablenames)
            }
        }
        return tableList
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        if (databaseName && tableName) {
            def dbMatch = databaseName.replaceAll(/\*/, '.*')
            def tableMatch = tableName.replaceAll(/\*/, '.*')

            def fields = []
            def mappingData = getMappings()
            mappingData.each { dbkey, dbvalues ->
                if (dbkey.matches(dbMatch)) {
                    def mappings = dbvalues['mappings']
                    mappings.each { tablekey, tablevalues ->
                        if (tablekey.matches(tableMatch)) {
                            def fieldValues = tablevalues['properties'].keySet()
                            fields.addAll(fieldValues)
                        }
                    }
                }
            }

            if (fields) {
                fields.add("_id")
                return fields.unique()
            }
        }
        throw new ResourceNotFoundException("Fields for Database ${databaseName} and Table ${tableName} do not exist")
    }

    @Override
    Map getFieldTypes(String databaseName, String tableName) {
        def fieldTypes = [:]
        if (databaseName && tableName) {
            def dbMatch = databaseName.replaceAll(/\*/, '.*')
            def tableMatch = tableName.replaceAll(/\*/, '.*')
            def mappingData = getMappings()

            mappingData.each { dbkey, dbvalues ->
                if (dbkey.matches(dbMatch)) {
                    def mappings = dbvalues['mappings']
                    mappings.each { tablekey, tablevalues ->
                        if (tablekey.matches(tableMatch)) {
                            def fieldValues = tablevalues['properties']
                            fieldValues.each { fieldkey, fieldvalues ->
                                def fieldType = fieldvalues['type']
                                fieldTypes.put(fieldkey, fieldType)
                            }
                        }
                    }
                }
            }
        }
        return fieldTypes
    }

    /** Get the _mappings from the DB.  This looks like:
     * <pre>
     * { "neonintegrationtest":
     *     {"mappings":
     *         {"records":
     *              {"properties":
     *                     {"city": {"type":"keyword"},
     *                      "firstname":{"type":"keyword"}
     *  ....
     * </pre>
     * which is a map of maps.
     * @return map of maps object, groovy.json.internal.lazyMap
     */
    protected Object getMappings() {
        LOGGER.debug("Executing get mappings")
        Response response = getClient().performRequest("GET", "/_mappings")
        int statusCode = response.statusLine.statusCode
        if (statusCode != 200) {
            LOGGER.warn("Unable to get mappings.  Status code " + statusCode)
            return new Object()
        }
        def json = new JsonSlurper().parse(response.entity.content)
        return json
    }

    protected RestClient getClient() {
        return connectionManager.connection.client
    }

}
