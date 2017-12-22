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

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder

import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.AggregateClause
import com.ncc.neon.query.clauses.FieldFunction
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState

import groovy.transform.Immutable

/**
 * Converts a Query object into a BasicDbObject
 */
@Immutable
class ElasticSearch5ConversionStrategy {
    static final String TERM_PREFIX = "_term"
    static final String STATS_AGG_PREFIX = "_statsFor_"
    static final String[] DATE_OPERATIONS = ['year', 'month', 'dayOfMonth', 'dayOfWeek', 'hour', 'minute', 'second']

    private final FilterState filterState
    private final SelectionState selectionState

    public SearchRequest convertQuery(Query query, QueryOptions options) {
        def source = createSourceBuilderWithState(query, options)

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        convertAggregations(query, source)

        return buildRequest(query, source)
    }

    /*
     * Create and return an elastic search SourceBuilder that takes into account the current
     * filter state and selection state.  It takes an input query, applies the current
     * filter and selection state associated with this ConverstionStrategy to it and
     * returns a sourcebuilder seeded with the resultant query param.
     */
    public SearchSourceBuilder createSourceBuilderWithState(Query query, QueryOptions options, WhereClause whereClause = null) {
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        def whereClauses = collectWhereClauses(dataSet, query, options, whereClause)

        def whereFilter = ElasticSearchConversionStrategyHelper.convertWhereClauses(whereClauses)

        return createSearchSourceBuilder(query).query(QueryBuilders.boolQuery().filter(whereFilter))
    }

    /*
     * create the metric aggregations by doing a stats aggregation for any field where
     * a calculation is requested - this gives us all of the metrics we could
     * possibly need. Also, don't process the count all clauses here, since
     * that will be available either through the hit count in the results, or as
     * doc_count in the buckets
    */
    private static convertAggregations(Query query, SearchSourceBuilder source) {
        if(query.isDistinct) {
            if(!query.fields || query.fields.size() > 1) {
                throw new NeonConnectionException("Distinct call requires one field")
            }

            def termsAggregations = AggregationBuilders.terms('distinct').field(query.fields[0]).size(ElasticSearchConversionStrategyHelper.RESULT_LIMIT)
            source.aggregation(termsAggregations)
        } else {
            convertMetricAggregations(query, source)
        }
    }

    private static findMatchingSortClause(Query query, matchClause) {
        query.sortClauses.find { sc ->
            if (matchClause instanceof FieldFunction) {
                return matchClause.name == sc.fieldName
            }
            if (matchClause instanceof GroupByFieldClause) {
                return matchClause.field == sc.fieldName
            }
        }
    }

    private static convertMetricAggregations(Query query, SearchSourceBuilder source) {
        def metricAggregations = getMetricAggregations(query)

        if (query.groupByClauses) {
            def bucketAggregations = query.groupByClauses.collect(ElasticSearchConversionStrategy.&convertGroupByClause.curry(query))

            //apply metricAggregations and any associated sorting to the terminal group by clause
            metricAggregations.each(bucketAggregations.last().&subAggregation)
            query.aggregates.each { AggregateClause ac ->
                def sc = findMatchingSortClause(query, ac)
                if (sc) {
                    def sortOrder = sc.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING
                    bucketAggregations.each { bucketAgg ->
                        if (!(bucketAgg instanceof DateHistogramAggregationBuilder)) {
                            bucketAgg.order(Terms.Order.aggregation(TERM_PREFIX, sortOrder))
                        }
                    }
                    def lastAgg = bucketAggregations.last()
                    if (!(lastAgg instanceof DateHistogramAggregationBuilder)) {
                        def aggOrder
                        if (isCountAllAggregation(ac)) {
                            aggOrder = Terms.Order.count(sortOrder)
                        } else {
                            aggOrder = Terms.Order.aggregation("${STATS_AGG_PREFIX}${ac.field}" as String, ac.operation as String, sortOrder)
                        }
                        lastAgg.order(aggOrder)
                    }
                }
            }
            //on each aggregation, except the last - nest the next aggregation
            bucketAggregations.take(bucketAggregations.size() - 1)
                    .eachWithIndex { v, i -> v.subAggregation(bucketAggregations[i + 1]) }
            source.aggregation(bucketAggregations.head() as AbstractAggregationBuilder)
        } else {
            //if there are no groupByClauses, apply sort and metricAggregations directly to source
            metricAggregations.each(source.&aggregation)
            query.sortClauses.collect(ElasticSearchConversionStrategy.&convertSortClause).each(source.&sort)
        }
    }

    private static getMetricAggregations(Query query) {
        return query.aggregates
                .findAll { !isCountAllAggregation(it) && !isCountFieldAggregation(it) }
                .collect { it.field }
                .unique()
                .collect { AggregationBuilders.stats("${STATS_AGG_PREFIX}${it}").field(it as String) }
    }

    private static SearchRequest buildRequest(Query query, SearchSourceBuilder source) {
        def request = createSearchRequest(source, query)

        if (query.filter?.tableName) {
            request.types([query.filter.tableName] as String[])
        }
        request
    }

    private collectWhereClauses(DataSet dataSet, Query query, QueryOptions options, WhereClause whereClause) {
        def whereClauses = whereClause ? [whereClause] : []
        if (query.filter?.whereClause) {
            whereClauses << query.filter.whereClause
        }
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState, options.ignoredFilterIds))
        }
        if (options.selectionOnly) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }

        whereClauses.addAll(getCountFieldClauses(query))

        return whereClauses
    }

    private getCountFieldClauses(query) {
        def clauses = []
        query.aggregates.each {
            if(isCountFieldAggregation(it)) {
                clauses.push(new SingularWhereClause(lhs: it.field, operator: '!=', rhs: null))
            }
        }
        return clauses
    }

    public static SearchSourceBuilder createSearchSourceBuilder(Query params) {
        new SearchSourceBuilder()
            .explain(false)
            .from(getOffset(params))
            .size(getTotalLimit(params))
    }

    public static int getOffset(Query query) {
        return (query?.offsetClause ? query.offsetClause.offset : 0) as int
    }

    public static int getTotalLimit(Query query) {
        if(query.groupByClauses || query.aggregates) {
            return 0
        }

        return getLimit(query)
    }

    public static int getLimit(Query query, Boolean supportsUnlimited = false) {
        if(query?.limitClause) {
            return query.limitClause.limit as int
        }

        if(supportsUnlimited) {
            return 0
        }

        return Math.max(ElasticSearchConversionStrategyHelper.RESULT_LIMIT - getOffset(query), 0)
    }

    public static SearchRequest createSearchRequest(SearchSourceBuilder source, Query params) {
        SearchRequest req = new SearchRequest()
        req.searchType((params?.aggregates) ? SearchType.QUERY_THEN_FETCH : SearchType.DFS_QUERY_THEN_FETCH)
            .source(source)
            .indices(params?.filter?.databaseName ?: '_all')
            .types(params?.filter?.tableName ?: '_all')
        if (req.searchType == SearchType.DFS_QUERY_THEN_FETCH && params.limitClause && params.limitClause.limit > 10000) {
            req = req.scroll(TimeValue.timeValueMinutes(1))
        }
        return req
    }

    public static boolean isCountAllAggregation(clause) {
        clause && clause.operation == 'count' && clause.field == '*'
    }

    public static boolean isCountFieldAggregation(clause) {
        clause && clause.operation == 'count' && clause.field != '*'
    }

    private static createWhereClausesForFilters(DataSet dataSet, filterCache, ignoredFilterIds = []) {
        filterCache.getFilterKeysForDataset(dataSet)
            .findAll { !(it.id in ignoredFilterIds) && it.filter.whereClause }
            .collect { it.filter.whereClause }
    }

    private static SortBuilder convertSortClause(clause) {
        def order = clause.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING ?
                SortOrder.ASC : SortOrder.DESC

        SortBuilders.fieldSort(clause.fieldName as String).order(order)
    }

    @SuppressWarnings("MethodSize")
    private static AggregationBuilder convertGroupByClause(Query query, clause) {
        def applySort = { TermsAggregationBuilder tb ->
            def sc = findMatchingSortClause(query, clause)
            if (sc) {
                def sortOrder = sc.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING
                tb.order(Terms.Order.term(sortOrder))
            }
            return tb
        }

        if (clause instanceof GroupByFieldClause) {
            return applySort(AggregationBuilders.terms(clause.field as String).field(clause.field as String).size(
                ElasticSearchConversionStrategyHelper.RESULT_LIMIT))
        }

        if (clause instanceof GroupByFunctionClause) {
            if (clause.operation in DATE_OPERATIONS) {

                def template = {
                    def groupByClause = AggregationBuilders
                        .dateHistogram(clause.name as String)
                        .field(clause.field as String)
                        .interval(it.interval)
                        .format(it.format)
                                        if (clause.operation == 'dayOfWeek') {
                                                groupByClause.offset("1d")
                                        }
                                        return groupByClause
                }

                switch (clause.operation) {
                    case 'year': return template(interval:ElasticSearchConversionStrategyHelper.YEAR, format:'yyyy')
                    case 'month': return template(interval:ElasticSearchConversionStrategyHelper.MONTH, format:'M')
                    case 'dayOfMonth': return template(interval:ElasticSearchConversionStrategyHelper.DAY, format:'d')
                    case 'dayOfWeek': return template(interval:ElasticSearchConversionStrategyHelper.DAY, format:'e')
                    case 'hour': return template(interval:ElasticSearchConversionStrategyHelper.HOUR, format:'H')
                    case 'minute': return template(interval:ElasticSearchConversionStrategyHelper.MINUTE, format:'m')
                    case 'second': return template(interval:ElasticSearchConversionStrategyHelper.SECOND, format:'s')
                }
            }
        }

        throw new NeonConnectionException("Unknown groupByClause: ${clause.getClass()}")
    }
}
