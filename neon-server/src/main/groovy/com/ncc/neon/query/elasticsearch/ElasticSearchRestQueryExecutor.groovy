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
import groovy.json.JsonSlurper
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.DocValueFormat
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats
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

    private RestHighLevelClient highLevelClient

    @SuppressWarnings('MethodSize')
    @Override
    QueryResult doExecute(Query query, QueryOptions options) {

        def aggregates = query.aggregates
        def groupByClauses = query.groupByClauses

        checkDatabaseAndTableExists(query.databaseName, query.tableName)
        long d1 = new Date().getTime()

        ElasticSearchRestConversionStrategy conversionStrategy = new ElasticSearchRestConversionStrategy(
                filterState: filterState, selectionState: selectionState)
        SearchRequest request = conversionStrategy.convertQuery(query, options)
        highLevelClient = getClient()
        SearchResponse response = highLevelClient.search(request)

        def aggResults = response.aggregations
        def returnVal

        if (aggregates && !groupByClauses) {
            LOGGER.debug("aggs and no group by ")
            returnVal = new TabularQueryResult([
                    extractMetrics(aggregates, aggResults ? aggResults.asMap() : null, response.hits.totalHits)
            ])
        } else if (groupByClauses) {
            LOGGER.debug("group by ")
            def buckets = extractBuckets(groupByClauses, aggResults.asList()[0])
            buckets = combineDuplicateBuckets(buckets)
            buckets = extractMetricsFromBuckets(aggregates, buckets)
            buckets = sortBuckets(query.sortClauses, buckets)
            buckets = limitBuckets(buckets, query)
            returnVal = new TabularQueryResult(buckets)
        } else if (query.isDistinct) {
            LOGGER.debug("distinct")
            returnVal = new TabularQueryResult(extractDistinct(query, aggResults.asList()[0]))
        } else if (response.getScrollId()) {
            returnVal = collectScrolledResults(query, response)
        } else {
            LOGGER.debug("none of the above")
            returnVal = new TabularQueryResult(extractHitsFromResults(response))
        }

        long diffTime = new Date().getTime() - d1
        LOGGER.debug(" Query took: " + diffTime + " ms ")

        return returnVal
    }

    /**
     * Use scroll interface to get many results.  See:
     *
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/5.6/java-rest-high-search-scroll.html
     */
    private QueryResult collectScrolledResults(Query query, SearchResponse firstResponse) {
        def accumulatedHits = []
        def response = firstResponse
        accumulatedHits.addAll(extractHitsFromResults(response))
        String scrollId = response.getScrollId()

        // Keep scrolling until we either get all of the results or we reach the requested limit
        while (response.hits.hits.size() > 0 &&
                accumulatedHits.size() < response.hits.getTotalHits() &&
                accumulatedHits.size() < query.limitClause.limit) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId)
            scrollRequest.scroll()
            response = highLevelClient.searchScroll(scrollRequest)
            scrollId = response.scrollId()
            accumulatedHits.addAll(extractHitsFromResults(response))
        }
        // Limit the list to only the desired limit.  This is because we added all the results above so
        // the size of  the accumulated hits could be more than the limit desired.
        accumulatedHits = accumulatedHits.subList(0, Math.min(accumulatedHits.size(), query.limitClause.limit))
        return new TabularQueryResult(accumulatedHits)
    }


    private List<Map<String, Object>> extractHitsFromResults(SearchResponse response) {
        return response.getHits().getHits().collect {
            def record = it.getSourceAsMap()
            // Add the ElasticSearch id, since it isn't included in the "source" document
            record["_id"] = it.getId()
            return record
        }
    }

    List<Map<String, Object>> extractDistinct(Query query, aggResult) {
        String field = query.fields[0]

        def distinctValues = []
        aggResult.buckets.each {
            def accumulator = [:]
            accumulator[field] = it.key
            distinctValues.push(accumulator)
        }

        distinctValues = sortDistinct(query, distinctValues)

        int offset = ElasticSearchRestConversionStrategy.getOffset(query)
        int limit = ElasticSearchRestConversionStrategy.getLimit(query, true)

        if (limit == 0) {
            limit = distinctValues.size()
        }

        int endIndex = ((limit - 1) + offset) < (distinctValues.size() - 1) ? ((limit - 1) + offset) : (distinctValues.size() - 1)
        endIndex = (endIndex > offset ? endIndex : offset)
        distinctValues = (offset >= distinctValues.size()) ? [] : distinctValues[offset..endIndex]

        return distinctValues
    }

    private List<Map<String, Object>> sortDistinct(Query query, values) {
        if (query.sortClauses) {
            return values.sort { a, b ->
                a[query.fields[0]] <=> b[query.fields[0]]
            }
        }

        return values
    }

    private static Map<String, Object> extractMetrics(clauses, results, totalCount) {
        def (countAllClause, metricClauses) = clauses.split {
            ElasticSearchRestConversionStrategy.isCountAllAggregation(it) || ElasticSearchRestConversionStrategy.isCountFieldAggregation(it)
        }

        def metrics = metricClauses.inject([:]) { acc, clause ->
            def result = results.get("${STATS_AGG_PREFIX}${clause.field}" as String)
            acc.put(clause.name, result[clause.operation])
            acc
        }

        if (countAllClause) {
            metrics.put(countAllClause[0].name, totalCount)
        }
        metrics
    }

    /**
     * The aggregation results from ES will be a tree of aggregation -> buckets -> aggregation -> buckets -> etc
     * we want to flatten it into a list of buckets where we have one item in the list for each leaf in the tree.
     * Each item is an accumulation of all the buckets along the path to the leaf.
     *
     * We process an aggregation by getting the list of buckets and calling extract again for each of them.
     * For each bucket we copy the current accumulator, since we're branching into more paths in the tree.
     *
     * We process a bucket by getting the current groupByClause and using it to get the value we're interested in
     * from the bucket. Then if there are more groupByClauses, we recurse into extract using the rest of the clauses
     * and the nested aggregation. If there are no more clauses to process, then we've reached the bottom of the tree
     * we add any metric aggregations to the bucket and push it onto the result list.
     */
    private
    static List<AggregationBucket> extractBuckets(groupByClauses, value, Map accumulator = [:], List<AggregationBucket> results = []) {
        value.buckets.each {
            def newAccumulator = [:]
            newAccumulator.putAll(accumulator)
            extractBucket(groupByClauses, it, newAccumulator, results)
        }

        return results
    }

    private static void extractBucket(groupByClauses, value, Map accumulator, List<Map<String, Object>> results) {
        def currentClause = groupByClauses.head()
        switch (currentClause.getClass()) {
            case GroupByFieldClause:
                accumulator.put(currentClause.field, value.key)
                break
            case GroupByFunctionClause:
                // If the group by field is a function of a date (e.g., group by month), then the
                // key field will still be just a date, but getKeyAsString() will return the value
                // returned by the function (e.g., the month).
                def key = value.getKeyAsString()
                def isDateClause = (currentClause.operation in ElasticSearchRestConversionStrategy.DATE_OPERATIONS
                        && key.isNumber())

                accumulator.put(groupByClauses.head().name, isDateClause ? key.toFloat() : key)
                break
            default:
                throw new NeonConnectionException("Bad implementation - ${currentClause.getClass()} is not a valid groupByClause")
        }

        if (groupByClauses.tail()) {
            extractBuckets(groupByClauses.tail(), (MultiBucketsAggregation) value.getAggregations().asList().head(), accumulator, results)
        } else {
            def bucket = new AggregationBucket()
            bucket.setGroupByKeys(accumulator)
            bucket.setDocCount(value.getDocCount())

            def terminalAggs = value.getAggregations()
            if (terminalAggs) {
                bucket.getAggregatedValues().putAll(terminalAggs.asMap())
            }
            results.push(bucket)
        }
    }

    private static List<AggregationBucket> combineDuplicateBuckets(List<AggregationBucket> buckets) {
        Map mappedBuckets = [:]
        // Iterate over all of the buckets, looking for any that have the same groupByKeys
        buckets.each {
            // Only process a bucket if there are documents in it, since we're using a histogram to
            // replicate group-by functionality.
            if (it.getDocCount() > 0) {
                def existingBucket = mappedBuckets.get(it.getGroupByKeys())
                if (existingBucket) {
                    // If we've already found a bucket with these groupByKeys, then combine them
                    it.getAggregatedValues().each { key, value ->
                        def existingAgg = existingBucket.getAggregatedValues().get(key)
                        if (existingAgg) {
                            def newAgg = createInternalStats(
                                    existingAgg.getName(),
                                    existingAgg.getCount() + value.getCount(),
                                    existingAgg.getSum() + value.getSum(),
                                    Math.min(existingAgg.getMin(), value.getMin()),
                                    Math.max(existingAgg.getMax(), value.getMax()),
                                    DocValueFormat.RAW
                            )
                            existingBucket.getAggregatedValues().put(key, newAgg)
                        } else {
                            existingBucket.put(key, value)
                        }
                    }
                    existingBucket.setDocCount(existingBucket.getDocCount() + it.getDocCount())
                } else {
                    // If there isn't already a bucket with these groupByKeys, then add it to the map
                    mappedBuckets.put(it.getGroupByKeys(), it)
                }
            }
        }
        return mappedBuckets.values().asList()
    }


    private static createInternalStats(name, count, sum, min, max, formatter) {
        return new InternalStats(name, count, sum, min, max, formatter, [], [:])
    }

    private static List<Map<String, Object>> extractMetricsFromBuckets(clauses, buckets) {
        return buckets.collect {
            def result = it.getGroupByKeys()
            result.putAll(extractMetrics(clauses, it.getAggregatedValues(), it.getDocCount()))
            result
        }
    }

    private static List<Map<String, Object>> sortBuckets(List<SortClause> sortClauses,
                                                         List<Map<String, Object>> buckets) {
        if (sortClauses) {
            buckets.sort { a, b ->
                for (def sortClause : sortClauses) {
                    def aField = a[sortClause.fieldName]
                    def bField = b[sortClause.fieldName]
                    def order = sortClause.getSortDirection() * (aField <=> bField)
                    if (order != 0) {
                        return order
                    }
                }
                return 0
            }
        }
        return buckets
    }

    private List<Map<String, Object>> limitBuckets(def buckets, Query query) {
        int offset = ElasticSearchRestConversionStrategy.getOffset(query)
        int limit = ElasticSearchRestConversionStrategy.getLimit(query, true)

        if (limit == 0) {
            limit = buckets.size()
        }

        int endIndex = ((limit - 1) + offset) < (buckets.size() - 1) ? ((limit - 1) + offset) : (buckets.size() - 1)
        endIndex = (endIndex > offset ? endIndex : offset)

        def result = (offset >= buckets.size()) ? [] : buckets[offset..endIndex]

        return result
    }

    /**
     *  Note: This method is not an appropriate check for queries against index mappings as they
     *  allow both the databaseName and tableName to be wildcarded.  This method allows only
     *  the databaseName to be wildcarded to match the behavior of index searches.
     */
    protected void checkDatabaseAndTableExists(String databaseName, String tableName) {
        def databaseMatch = databaseName.replaceAll(/\*/, '.*')
        def tableMatch = tableName
        def mappingData = getMappings()
        boolean found = false
        mappingData.each { databaseKey, databaseValues ->
            if (databaseKey.matches(databaseMatch)) {
                def tableNames = databaseValues['mappings'].keySet() as List
                tableNames.each { String it ->
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
    List<String> showTables(String databaseName) {
        LOGGER.debug("Executing showTables for index " + databaseName + " to get type mappings")
        def databaseMatch = databaseName.replaceAll(/\*/, '.*')
        def tableList = []
        def mappingData = getMappings()
        mappingData.each { databaseKey, databaseValues ->
            if (databaseKey.matches(databaseMatch)) {
                def tableNames = databaseValues['mappings'].keySet()
                tableList.addAll(tableNames)
            }
        }
        return tableList
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        if (databaseName && tableName) {
            def databaseMatch = databaseName.replaceAll(/\*/, '.*')
            def tableMatch = tableName.replaceAll(/\*/, '.*')
            def fields = []
            def mappingData = getMappings()
            mappingData.each { databaseKey, databaseValues ->
                if (databaseKey.matches(databaseMatch)) {
                    def tableMappings = databaseValues['mappings']
                    tableMappings.each { tableKey, tableValues ->
                        if (tableKey.matches(tableMatch)) {
                            fields.addAll(getFieldsInObject(tableValues['properties'], ""))
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

    private List<String> getFieldsInObject(Map fields, String parentFieldName) {
        def fieldNames = []
        fields.each { field ->
            def type = field.getValue().containsKey('type') ? field.getValue().get('type') : 'object'
            String fieldName = field.getKey()
            if(parentFieldName) {
                fieldName = parentFieldName + "." + field.getKey()
            }
            fieldNames.add(fieldName)
            if(type == 'object') {
                fieldNames.addAll(getFieldsInObject(field.getValue().get('properties'), fieldName))
            }
        }
        return fieldNames
    }

    @Override
    Map getFieldTypes(String databaseName, String tableName) {
        def fieldTypes = [:]
        if (databaseName && tableName) {
            def databaseMatch = databaseName.replaceAll(/\*/, '.*')
            def tableMatch = tableName.replaceAll(/\*/, '.*')
            def mappingData = getMappings()
            mappingData.each { databaseKey, databaseValues ->
                if (databaseKey.matches(databaseMatch)) {
                    def tableMappings = databaseValues['mappings']
                    tableMappings.each { tableKey, tableValues ->
                        if (tableKey.matches(tableMatch)) {
                            fieldTypes.putAll(getFieldTypesInObject(tableValues['properties'], null))
                        }
                    }
                }
            }
        }
        return fieldTypes
    }

    private Map getFieldTypesInObject(Map fields, String parentFieldName) {
        def fieldsToTypes = [:]
        fields.each { field ->
            String fieldName = field.getKey()
            if(parentFieldName) {
                fieldName = parentFieldName + "." + field.getKey()
            }
            if(field.getValue().containsKey('type')) {
                fieldsToTypes.put(fieldName, field.getValue().get('type'))
            } else {
                fieldsToTypes.putAll(getFieldTypesInObject(field.getValue().get('properties'), fieldName))
            }
        }
        return fieldsToTypes
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
        Response response = getClient().getLowLevelClient().performRequest("GET", "/_mappings")
        int statusCode = response.statusLine.statusCode
        if (statusCode != 200) {
            LOGGER.warn("Unable to get mappings.  Status code " + statusCode)
            return new Object()
        }
        def json = new JsonSlurper().parse(response.entity.content)
        return json
    }

    protected RestHighLevelClient getClient() {
        return connectionManager.connection.client
    }

}
