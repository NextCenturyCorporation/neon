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
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.DataSet
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

import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.client.Client
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval
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
        //Gather all the where clauses
        def whereClauses = []
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        if (query.filter?.whereClause) {
            whereClauses << query.filter.whereClause
        }
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState, options.ignoredFilterIds))
        }
        if (options.selectionOnly) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }

        //Build the elasticsearch filters for the where clauses
        def inners = whereClauses.collect(ElasticSearchQueryExecutor.&resolveWhereClause) as FilterBuilder[]
        def whereFilter = FilterBuilders.boolFilter().must(FilterBuilders.andFilter(inners))

        //Build the sorters for the sort clause
        def sorters = (query.sortClauses ?: []).collect(ElasticSearchQueryExecutor.&resolveSortClause)

        //begin constructing the search
        def source = createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        sorters.each(source.&sort)

        //Build the metric aggregations - remove the count all aggregation since we can get that as part of the
        //search result; also, rather than pass individual metrics for each aggregation field, we'll simply
        //get stats on each field for which an aggregation exists thereby getting everything
        def aggregates = query.aggregates
        def aggregations = aggregates
            .findAll({ !isCountAllAggregation(it) })
            .collect({ it.field })
            .unique()
            .collect({ AggregationBuilders.stats("${STATS_AGG_PREFIX}${it}").field(it as String) })

        //build the groupBy statement
        def groupByClauses = query.groupByClauses
        if (groupByClauses) {
            def groupByAggregationBuilders = groupByClauses.collect(ElasticSearchQueryExecutor.&resolveGroupByClause)

            //apply aggregations to the terminal group by clause
            aggregations.each { groupByAggregationBuilders.last().subAggregation(it) }

            //on each aggregation, except the last - nest the next aggregation
            groupByAggregationBuilders
                .take(groupByAggregationBuilders.size() - 1)
                .eachWithIndex({ v, i -> v.subAggregation(groupByAggregationBuilders[i + 1]) })

            source.aggregation(groupByAggregationBuilders.head() as AbstractAggregationBuilder)

        } else {
            //if there are no groupByClauses, apply metrics aggregations directly
            aggregations.each(source.&aggregation)
        }

        def request = createSearchRequest(source, query)

        if (query.filter?.tableName) {
            request.types([query.filter.tableName] as String[])
        }

        //and...  search!
        def results = getClient().search(request).actionGet()

        def aggResults = results.aggregations

        if (aggregates && !groupByClauses) {
            return new TabularQueryResult(extractMetricAggregations(aggregates, aggResults.asMap(), results.hits.totalHits))
        }

        if (groupByClauses) {
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

    private static SearchSourceBuilder createSearchSourceBuilder(Query params) {
        new SearchSourceBuilder()
            .explain(false)
            .from((params?.offsetClause ? params.offsetClause.offset : 0) as int)
            .size((params?.limitClause ? params.limitClause.limit : 45) as int)
    }

    private static SearchRequest createSearchRequest(SearchSourceBuilder source, Query params) {
        new SearchRequest()
            .searchType(SearchType.DFS_QUERY_THEN_FETCH)
            .source(source)
            .indices(params?.filter?.databaseName ?: '_all')
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

    private static boolean isCountAllAggregation(clause) {
        clause.operation == 'count' && clause.field == '*'
    }

    private static resolveSortClause(clause) {
        def order = clause.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING ? SortOrder.ASC : SortOrder.DESC
        SortBuilders.fieldSort(clause.fieldName).order(order)
    }

    private static resolveGroupByClause(clause) {
        if (clause.getClass() == GroupByFieldClause) {
            return AggregationBuilders.terms(clause.field).field(clause.field as String)
        }

        if (clause.getClass() == GroupByFunctionClause && clause.operation in ['year', 'month', 'dayOfMonth', 'hour', 'minute', 'second']) {
            def fn = AggregationBuilders.dateHistogram(clause.name).field(clause.field as String).&interval
            switch(clause.operation) {
            case 'year':
                return fn(Interval.YEAR)
            case 'month':
                return fn(Interval.MONTH)
            case 'dayOfMonth':
                return fn(Interval.DAY)
            case 'hour':
                return fn(Interval.HOUR)
            case 'minute':
                return fn(Interval.MINUTE)
            case 'second':
                return fn(Interval.SECOND)
            }
        }

        throw new RuntimeException("${clause} is not a valid bucket aggregation")
    }

    private static createWhereClausesForFilters(DataSet dataSet, filterCache, ignoredFilterIds = []) {
        filterCache.getFilterKeysForDataset(dataSet)
            .findAll { !(it.id in ignoredFilterIds) && it.filter.whereClause }
            .collect { it.filter.whereClause }
    }

    private static resolveWhereClause(clause) {
        switch (clause.getClass()) {
        case AndWhereClause:
            return resolveBooleanWhereClause(clause, FilterBuilders.&andFilter)
        case OrWhereClause:
            return resolveBooleanWhereClause(clause, FilterBuilders.&orFilter)
        case SingularWhereClause:
            return resolveSingularWhereClause(clause)
        default:
            throw new RuntimeException("class ${clause.getClass()} is not a valid where clause")
        }
    }

    private static resolveBooleanWhereClause(clause, combiner) {
        def inners = clause.whereClauses.collect(ElasticSearchQueryExecutor.&resolveWhereClause) as FilterBuilder[]
        FilterBuilders.boolFilter().must(combiner(inners) as FilterBuilder)
    }

    private static resolveSingularWhereClause(clause) {
        if (clause.operator in ['<', '>', '<=', '>=']) {
            return {
                switch (clause.operator) {
                case '<':
                    return it.&lt
                case '>':
                    return it.&gt
                case '<=':
                    return it.&lte
                case '>=':
                    return it.&gte
                }
            }.call(FilterBuilders.rangeFilter(clause.lhs))(clause.rhs)
        }

        if (clause.operator in ['contains', 'not contains']) {
            def regexFilter = FilterBuilders.regexpFilter(clause.lhs, ".*${clause.rhs}.*")
            return clause.operator == 'contains' ? regexFilter : FilterBuilders.notFilter(regexFilter)
        }

        if (clause.operator in ['=', '!=']) {
            def termFilter = FilterBuilders.termFilter(clause.lhs as String, clause.rhs as String)
            return clause.operator == '=' ? termFilter : FilterBuilders.notFilter(termFilter)
        }

        throw new RuntimeException("${clause.operator} is an invalid operator for a where clause")
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

    private static extractGroupByAggregation(groupByClauses, value, metricAggs, hitCount, Map accumulator = new HashMap(), List results = new ArrayList()) {
        if (value instanceof MultiBucketsAggregation) {
            value.buckets.each {
                def newAcc = new HashMap()
                newAcc.putAll(accumulator)
                extractGroupByAggregation(groupByClauses, it, metricAggs, hitCount, newAcc, results)
            }
            return results
        }

        if (value instanceof MultiBucketsAggregation.Bucket) {
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
    }
}
