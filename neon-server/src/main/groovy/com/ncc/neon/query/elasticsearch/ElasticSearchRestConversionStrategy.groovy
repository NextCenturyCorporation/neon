//package com.ncc.neon.query.elasticsearch
/**
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

import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.AggregateClause
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.FieldFunction
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import groovy.transform.Immutable
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Converts a Query object into a BasicDbObject
 */
@Immutable
class ElasticSearchRestConversionStrategy {
    static final String TERM_PREFIX = "_term"
    static final String STATS_AGG_PREFIX = "_statsFor_"
    static final String[] DATE_OPERATIONS = ['year', 'month', 'dayOfMonth', 'dayOfWeek', 'hour', 'minute', 'second']

    public static final RESULT_LIMIT = 10000

    public static final YEAR = DateHistogramInterval.YEAR
    public static final MONTH = DateHistogramInterval.MONTH
    public static final DAY = DateHistogramInterval.DAY
    public static final HOUR = DateHistogramInterval.HOUR
    public static final MINUTE = DateHistogramInterval.MINUTE
    public static final SECOND = DateHistogramInterval.SECOND

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRestConversionStrategy)

    private final FilterState filterState
    private final SelectionState selectionState

    public SearchRequest convertQuery(Query query, QueryOptions options) {
        LOGGER.debug("Query is " + query)
        LOGGER.debug("QueryOptions is " + options)

        def source = createSourceBuilderWithState(query, options)

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        convertAggregations(query, source)

        return buildRequest(query, source)
    }


    public static QueryBuilder convertWhereClauses(whereClauses) {
        //Build the elasticsearch filters for the where clauses
        QueryBuilder[] inners = whereClauses.collect(this.&convertWhereClause) as QueryBuilder[]
        // Old, when andqQuery existed
        // def whereFilter = QueryBuilders.boolQuery().must(QueryBuilders.andQuery(inners))
        // New, where andQuery has been replaced by boolQuery
        def whereFilter = QueryBuilders.boolQuery().must(inners)
        return whereFilter
    }

    private static QueryBuilder convertWhereClause(clause) {
        switch (clause.getClass()) {
            case AndWhereClause:
                return convertBooleanWhereClause(clause, BoolQueryBuilder.&must)
            case OrWhereClause:
                return convertBooleanWhereClause(clause, BoolQueryBuilder.&should)
            case SingularWhereClause:
                return convertSingularWhereClause(clause)
            default:
                throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
    }

    private static QueryBuilder convertBooleanWhereClause(clause, Closure<QueryBuilder> combiner) {
        def inners = clause.whereClauses.collect(this.&convertWhereClause)
        QueryBuilders.boolQuery().must(combiner(inners as QueryBuilder[]))
    }

    @SuppressWarnings("MethodSize")
    public static QueryBuilder convertSingularWhereClause(clause) {
        if (clause.operator in ['<', '>', '<=', '>=']) {
            return { switch (clause.operator) {
                case '<': return it.&lt
                case '>': return it.&gt
                case '<=': return it.&lte
                case '>=': return it.&gte
            }}.call(QueryBuilders.rangeQuery(clause.lhs as String))(clause.rhs as String)
        }

        if (clause.operator in ['contains', 'not contains', 'notcontains']) {
            def regexFilter = QueryBuilders.regexpQuery(clause.lhs as String, ".*${clause.rhs}.*" as String)
            return clause.operator == 'contains' ? regexFilter : QueryBuilders.notQuery(regexFilter)
        }

        if (clause.operator in ['=', '!=']) {
            def hasValue = clause.rhs || clause.rhs == '' || clause.rhs == false

            def filter = hasValue ?
                    QueryBuilders.termQuery(clause.lhs as String, clause.rhs as String) :
                    QueryBuilders.existsQuery(clause.lhs as String)

            return (clause.operator == '!=') == !hasValue ? filter : QueryBuilders.notQuery(filter)
        }

        if (clause.operator in ["in", "notin"]) {
            def filter = QueryBuilders.termsQuery(clause.lhs as String, clause.rhs as String[])
            return (clause.operator == "in") ? filter : QueryBuilders.notQuery(filter)
        }

        throw new NeonConnectionException("${clause.operator} is an invalid operator for a where clause")
    }

    public static createHeatmapAggregation(HeatmapBoundsQuery boundingBox) {
        def hashGrid = AggregationBuilders.geohashGrid('heatmap').field(boundingBox.locationField).precision(boundingBox.gridCount)
        def filter  = QueryBuilders.geoBoundingBoxFilter(boundingBox.locationField).bottomLeft(boundingBox.minLat, boundingBox.minLon).topRight(boundingBox.maxLat, boundingBox.maxLon)
        def bounds = AggregationBuilders.filter('bounds').filter(filter).subAggregation(hashGrid)
        return bounds
    }


    /*
     * Create and return an elastic search SourceBuilder that takes into account the current
     * filter state and selection state.  It takes an input query, applies the current
     * filter and selection state associated with this ConverstionStrategy to it and
     * returns a sourcebuilder seeded with the resultant query param.
     */
    protected SearchSourceBuilder createSourceBuilderWithState(Query query, QueryOptions options, WhereClause whereClause = null) {
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        def whereClauses = collectWhereClauses(dataSet, query, options, whereClause)

        def whereFilter = convertWhereClauses(whereClauses)

        return createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))
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

            def termsAggregations = AggregationBuilders.terms('distinct').field(query.fields[0]).size(0)
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
            def bucketAggregations = query.groupByClauses.collect(this.&convertGroupByClause.curry(query))

            //apply metricAggregations and any associated sorting to the terminal group by clause
            metricAggregations.each(bucketAggregations.last().&subAggregation)
//            query.aggregates.each { ac ->
//                def sc = findMatchingSortClause(query, ac)
//                if (sc) {
//                    def sortOrder = sc.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING
//                    bucketAggregations.each { bucketAgg ->
//                        if (!(bucketAgg instanceof DateHistogramBuilder)) {
//                            bucketAgg.order(Terms.Order.aggregation(TERM_PREFIX, sortOrder))
//                        }
//                    }
//                    def lastAgg = bucketAggregations.last()
//                    if (!(lastAgg instanceof DateHistogramBuilder)) {
//                        def aggOrder
//                        if (isCountAllAggregation(ac)) {
//                            aggOrder = Terms.Order.count(sortOrder)
//                        } else {
//                            aggOrder = Terms.Order.aggregation("${STATS_AGG_PREFIX}${ac.field}" as String, ac.operation as String, sortOrder)
//                        }
//                        lastAgg.order(aggOrder)
//                    }
//                }
//            }
            //on each aggregation, except the last - nest the next aggregation
            bucketAggregations.take(bucketAggregations.size() - 1)
                    .eachWithIndex { v, i -> v.subAggregation(bucketAggregations[i + 1]) }
            source.aggregation(bucketAggregations.head() as AbstractAggregationBuilder)
        } else {
            //if there are no groupByClauses, apply sort and metricAggregations directly to source
            metricAggregations.each(source.&aggregation)
            query.sortClauses.collect(this.&convertSortClause).each(source.&sort)
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

    private static getCountFieldClauses(Query query) {
        def clauses = []
        query.aggregates.each { AggregateClause it ->
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

        return Math.max(RESULT_LIMIT - getOffset(query), 0)
    }

    public static SearchRequest createSearchRequest(SearchSourceBuilder source, Query params) {
        SearchRequest req = new SearchRequest()

        // NOTE:  IN version 5, count was replaced by Query_Then_Fetch with a size=0
        // See: https://www.elastic.co/guide/en/elasticsearch/reference/2.3/search-request-search-type.html
        // req.searchType((params?.aggregates) ? SearchType.COUNT : SearchType.DFS_QUERY_THEN_FETCH)
        //
        // TODO:  Set size=0 (i.e. limit clause) when type is counts
        req.searchType(SearchType.DFS_QUERY_THEN_FETCH)
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
        def applySort = { tb ->    //  TermsBuilder tb ->
            def sc = findMatchingSortClause(query, clause)
            if (sc) {
                def sortOrder = sc.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING
                tb.order(Terms.Order.term(sortOrder))
            }
            return tb
        }

        if (clause instanceof GroupByFieldClause) {
            return applySort(AggregationBuilders.terms(clause.field as String).field(clause.field as String).size(0))
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
                    case 'year': return template(interval:YEAR, format:'yyyy')
                    case 'month': return template(interval:MONTH, format:'M')
                    case 'dayOfMonth': return template(interval:DAY, format:'d')
                    case 'dayOfWeek': return template(interval:DAY, format:'e')
                    case 'hour': return template(interval:HOUR, format:'H')
                    case 'minute': return template(interval:MINUTE, format:'m')
                    case 'second': return template(interval:SECOND, format:'s')
                }
            }
        }

        throw new NeonConnectionException("Unknown groupByClause: ${clause.getClass()}")
    }
}
