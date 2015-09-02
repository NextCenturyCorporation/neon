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
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
//import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.Query
import com.ncc.neon.query.result.TabularQueryResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

//import org.elasticsearch.search.aggregations.AggregationBuilder
//import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
//import org.elasticsearch.action.search.SearchRequest
//import org.elasticsearch.search.builder.SearchSourceBuilder
//import org.elasticsearch.search.aggregations.AbstractAggregationBuilder
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.client.Client
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest

@Component
class ElasticSearchQueryExecutor extends AbstractQueryExecutor {

    static final String STATS_AGG_PREFIX = "_statsFor_"

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchQueryExecutor)

    @Autowired
    private FilterState filterState

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options) {
        ElasticSearchConversionStrategy conversionStrategy = new ElasticSearchConversionStrategy(filterState: filterState, selectionState: selectionState)
        def request = conversionStrategy.convertQuery(query, options)

        def aggregates = query.aggregates
        def groupByClauses = query.groupByClauses

        def results = getClient().search(request).actionGet()
        def aggResults = results.aggregations

        if (aggregates && !groupByClauses) {
            return new TabularQueryResult(extractMetricAggregations(aggregates, aggResults.asMap(), results.hits.totalHits))
        } else if (groupByClauses) {
            return new TabularQueryResult(extractGroupByAggregation(groupByClauses, aggResults.asList()[0], aggregates, results.hits.totalHits))
        }
        new TabularQueryResult(results.hits.collect { it.getSource() })
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing showDatabases to retrieve indices")
        getClient().admin().cluster().state(new ClusterStateRequest()).actionGet().state.metaData.indices.collect { it.key }
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing showTables for index " + dbName + " to get type mappings")
        getMappings().get(dbName).collect { it.key }
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        LOGGER.debug("Executing getFieldNames for index " + databaseName + " type " + tableName)
        getMappings().get(databaseName).get(tableName).getSourceAsMap().get('properties').collect { it.key }
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit = 40) {
        getClient()
            .search(createSearchRequest(
                createSearchSourceBuilder(new Query(filter: new Filter(databaseName: databaseName)))
                    .aggregation(AggregationBuilders.terms("arrayCount")
                            .field(field)
                            .size(limit))
                , null))
            .actionGet()
            .getAggregations()
            .asList()
            .head()
            .getBuckets()
            .collect { new ArrayCountPair(key: it.key, count: it.docCount) }
    }

    private Client getClient() {
        return connectionManager.connection.client
    }

    private ImmutableOpenMap getMappings() {
        getClient().admin().indices().getMappings(new GetMappingsRequest()).actionGet().mappings
    }

    private static extractMetricAggregations(aggregates, results, hitCount) {
        def (findAllAgg, metricAggs) = aggregates.split(ElasticSearchQueryExecutor.&isCountAllAggregation)
        def extractedAggs = metricAggs.collect({ it -> extractMetricAggregation(it, results) })
        if (findAllAgg) {
            def aggMap = new HashMap()
            aggMap.put(findAllAgg[0].name, hitCount)
            extractedAggs.push(aggMap)
        }
        extractedAggs
    }

    private static Map<String, Object> extractMetricAggregation(clause, aggs) {
        def agg = aggs.get("${STATS_AGG_PREFIX}${clause.field}" as String)
        def aggMap = new HashMap()
        aggMap.put(clause.name, agg[clause.operation])
        aggMap
    }

    private static List<Map<String, Object>> extractGroupByAggregation(groupByClauses, value, metricAggs, hitCount,
                                                                       Map accumulator = new HashMap(),
                                                                       List<Map<String, Object>> results = new ArrayList()) {

        //The aggregation results from ES will be a tree of aggregation -> buckets -> aggregation -> buckets -> etc
        //we want to flatten it into a list of buckets where we have one item in the list for each leaf in the tree.
        //Each item is an accumulation of all the buckets along the path to the leaf.

        if (value instanceof MultiBucketsAggregation) {
            //We process an aggregation by getting the list of buckets and calling extract again for each of them.
            //For each bucket we copy the current accumulator, since we're branching into more paths in the tree.
            value.buckets.each {
                def newAcc = new HashMap()
                newAcc.putAll(accumulator)
                extractGroupByAggregation(groupByClauses, it, metricAggs, hitCount, newAcc, results)
            }
        } else if (value instanceof MultiBucketsAggregation.Bucket) {
            //We process a bucket by getting the current groupByClause and using it to get the value we're interested in
            //from the bucket. Then if there are more groupByClauses, we recurse into extract using the rest of the clauses
            //and the nested aggregation. If there are no more clauses to process, then we've reached the bottom of the tree
            //we add any metric aggregations to the bucket and push it onto the result list.
            def currentClause = groupByClauses.head()

            switch (currentClause.getClass()) {
            case GroupByFieldClause:
                accumulator.put(currentClause.field, value.key)
                break
            case GroupByFunctionClause:
                accumulator.put(groupByClauses.head().name, value.key)
                break
            default:
                throw new RuntimeException("Bad implementation - ${currentClause.getClass()} is not a valid groupByClause")
            }

            if (groupByClauses.tail()) {
                extractGroupByAggregation(groupByClauses.tail(), value.getAggregations().asList().head(), metricAggs, hitCount, accumulator, results)
            } else {
                def terminalAggs = value.getAggregations()
                if (terminalAggs) {
                    extractMetricAggregations(metricAggs, terminalAggs.asMap(), hitCount).each(accumulator.&putAll)
                }
                results.push(accumulator)
            }
        }

        return results
    }
}
