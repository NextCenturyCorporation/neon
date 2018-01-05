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

import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.util.ResourceNotFoundException
import groovy.json.JsonSlurper
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

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

    @SuppressWarnings('MethodSize')
    @Override
    QueryResult doExecute(Query query, QueryOptions options) {

        def aggregates = query.aggregates
        def groupByClauses = query.groupByClauses

        checkDatabaseAndTableExists(query.databaseName, query.tableName)
        long d1 = new Date().getTime()

        ElasticSearchRestConversionStrategy conversionStrategy = new ElasticSearchRestConversionStrategy(
                filterState: filterState, selectionState: selectionState)
        String request = conversionStrategy.convertQuery(query, options)
        Response response = getClient().performRequest("GET", request)

        // SearchResponse results = getClient().search(request).actionGet()
        // def aggResults = results.aggregations
        def returnVal = new TabularQueryResult()

        if (aggregates && !groupByClauses) {
            LOGGER.debug("aggs and no group by ")
//            returnVal = new TabularQueryResult([
//                    extractMetrics(aggregates, aggResults ? aggResults.asMap() : null, results.hits.totalHits)
//            ])
        } else if (groupByClauses) {
            LOGGER.debug("group by ")
//            def buckets = extractBuckets(groupByClauses, aggResults.asList()[0])
//            buckets = combineDuplicateBuckets(buckets)
//            buckets = extractMetricsFromBuckets(aggregates, buckets)
//            buckets = sortBuckets(query.sortClauses, buckets)
//            buckets = limitBuckets(buckets, query)
//            returnVal = new TabularQueryResult(buckets)
        } else if (query.isDistinct) {
            LOGGER.debug("distinct")
//            returnVal = new TabularQueryResult(extractDistinct(query, aggResults.asList()[0]))
//        } else if (results.getScrollId()) {
//            returnVal = collectScrolledResults(query, results)
        } else {
            LOGGER.debug("none of the above")
            returnVal = new TabularQueryResult(extractResults(response))
        }

        long diffTime = new Date().getTime() - d1
        LOGGER.debug(" Query took: " + diffTime + " ms ")

        return returnVal
    }

    private List<Map<String, Object>> extractResults(response) {
        return response.collect {
            def record = it.getSource()
            // Add the ElasticSearch id, since it isn't included in the "source" document
            record["_id"] =  it.getId()
            return record
        }
    }

    /**
     *  Note: This method is not an appropriate check for queries against index mappings as they
     *  allow both the databaseName and tableName to be wildcarded.  This method allows only
     *  the databaseName to be wildcarded to match the behavior of index searches.
     */
    protected void checkDatabaseAndTableExists(String databaseName, String tableName) {
        def dbMatch = databaseName.replaceAll(/\*/, '.*')
        def tableMatch = tableName
        def mappingData = getMappings()
        boolean found = false

        mappingData.each { dbkey, v ->
            if (dbkey.matches(dbMatch)) {
                def tablenames = v['mappings'].keySet() as List
                tablenames.each { String it ->
                    if (it.matches(tableMatch)) {
                        found = true
                    }
                }
            }
        }
        if (!found) {
            throw new ResourceNotFoundException("Table ${tableName} does not exist")
        }
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
     *{ "neonintegrationtest":
     *{"mappings":
     *{"records":
     *{"properties":
     *{"city": {"type":"keyword"},
     *                      "firstname":{"type":"keyword"}*  ....
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
