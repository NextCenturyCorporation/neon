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
    private static final int TERM_AGGREGATION_SIZE = 100

    public static final YEAR = DateHistogramInterval.YEAR
    public static final MONTH = DateHistogramInterval.MONTH
    public static final DAY = DateHistogramInterval.DAY
    public static final HOUR = DateHistogramInterval.HOUR
    public static final MINUTE = DateHistogramInterval.MINUTE
    public static final SECOND = DateHistogramInterval.SECOND

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRestConversionStrategy)

    private final FilterState filterState
    private final SelectionState selectionState

    SearchRequest convertQuery(Query query, QueryOptions options) {
        LOGGER.debug("Query is " + query + " QueryOptions is " + options)

        def source = createSourceBuilderWithState(query, options)

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        convertAggregations(query, source)

        SearchRequest request = buildRequest(query, source)
        LOGGER.info("request " + request)
        return request
    }

    /*
     * Create and return an elastic search SourceBuilder that takes into account the current
     * filter state and selection state.  It takes an input query, applies the current
     * filter and selection state associated with this ConversionStrategy to it and
     * returns a SourceBuilder seeded with the resultant query param.
     */

    protected SearchSourceBuilder createSourceBuilderWithState(Query query, QueryOptions options,
                                                               WhereClause extraWhereClause = null) {
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)

        // Get all the (top level) WhereClauses, from the Filter and query
        def whereClauses = collectWhereClauses(dataSet, query, options, extraWhereClause)

        // Convert the WhereClauses into a single ElasticSearch QueryBuilder object
        def whereFilter = convertWhereClauses(whereClauses)

        SearchSourceBuilder ssb = createSearchSourceBuilder(query).query(whereFilter)
        return ssb

        // Was:
        // return createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))
    }

    /**
     * Returns a list of Neon WhereClause objects, some of which might have embedded WhereClauses
     */
    private List<WhereClause> collectWhereClauses(DataSet dataSet, Query query, QueryOptions options,
                                                  WhereClause extraWhereClause) {
        // Start the list as empty unless an extra WhereClause is passed
        List<WhereClause> whereClauses = extraWhereClause ? [extraWhereClause] : []

        // If the Query has a WhereClaus, add it.
        if (query.filter?.whereClause) {
            whereClauses << query.filter.whereClause
        }

        // Add the rest of the filters unless told not to.
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState, options.ignoredFilterIds))
        }

        // If selectionOnly, then
        if (options.selectionOnly) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }

        whereClauses.addAll(getCountFieldClauses(query))

        return whereClauses
    }

    /**
     * Given a list of WhereClause objects, convert them into a QueryBuilder.  In this case, a BoolQueryBuilder
     * that combines all the subsidiary QueryBuilder objects
     */
    protected static QueryBuilder convertWhereClauses(whereClauses) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()

        //Build the elasticsearch filters for the where clauses
        def inners = []
        whereClauses.each { inners.add(convertWhereClause(it)) }
        inners.each { queryBuilder.must(it) }
        return queryBuilder
    }

    private static QueryBuilder convertWhereClause(WhereClause clause) {
        switch (clause.getClass()) {
            case SingularWhereClause:
                return convertSingularWhereClause(clause)

            case AndWhereClause:
            case OrWhereClause:
                return convertCompoundWhereClause(clause)

            default:
                throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
    }

    private static QueryBuilder convertCompoundWhereClause(clause) {
        BoolQueryBuilder qb = QueryBuilders.boolQuery()
        def inners = clause.whereClauses.collect(this.&convertWhereClause)

        switch (clause.getClass()) {
            case AndWhereClause:
                inners.each { qb.must(it) }
                break

            case OrWhereClause:
                inners.each { qb.should(it) }
                break

            default:
                throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
        return qb
    }

    @SuppressWarnings("MethodSize")
    static QueryBuilder convertSingularWhereClause(clause) {
        if (clause.operator in ['<', '>', '<=', '>=']) {
            return {
                switch (clause.operator) {
                    case '<': return it.&lt
                    case '>': return it.&gt
                    case '<=': return it.&lte
                    case '>=': return it.&gte
                }
            }.call(QueryBuilders.rangeQuery(clause.lhs as String))(clause.rhs as String)
        }

        if (clause.operator in ['contains', 'not contains', 'notcontains']) {
            def regexFilter = QueryBuilders.regexpQuery(clause.lhs as String, ".*${clause.rhs}.*" as String)
            return clause.operator == 'contains' ? regexFilter : QueryBuilders.boolQuery().mustNot(regexFilter)
        }

        if (clause.operator in ['=', '!=']) {
            def hasValue = clause.rhs || clause.rhs == '' || clause.rhs == false

            def filter = hasValue ?
                    QueryBuilders.termQuery(clause.lhs as String, clause.rhs as String) :
                    QueryBuilders.existsQuery(clause.lhs as String)

            return (clause.operator == '!=') == !hasValue ? filter : QueryBuilders.boolQuery().mustNot(filter)
        }

        if (clause.operator in ["in", "notin"]) {
            def filter = QueryBuilders.termsQuery(clause.lhs as String, clause.rhs as String[])
            return (clause.operator == "in") ? filter : QueryBuilders.boolQuery().mustNot(filter)
        }

        throw new NeonConnectionException("${clause.operator} is an invalid operator for a where clause")
    }

    static createHeatmapAggregation(HeatmapBoundsQuery boundingBox) {
        def hashGrid = AggregationBuilders.geohashGrid('heatmap').field(boundingBox.locationField).precision(boundingBox.gridCount)
        def filter = QueryBuilders.geoBoundingBoxFilter(boundingBox.locationField).bottomLeft(boundingBox.minLat, boundingBox.minLon).topRight(boundingBox.maxLat, boundingBox.maxLon)
        def bounds = AggregationBuilders.filter('bounds').filter(filter).subAggregation(hashGrid)
        return bounds
    }

    /**
     * create the metric aggregations by doing a stats aggregation for any field where
     * a calculation is requested - this gives us all of the metrics we could
     * possibly need. Also, don't process the count all clauses here, since
     * that will be available either through the hit count in the results, or as
     * doc_count in the buckets
    */
    private static convertAggregations(Query query, SearchSourceBuilder source) {
        if (query.isDistinct) {
            if (!query.fields || query.fields.size() > 1) {
                throw new NeonConnectionException("Distinct call requires one field")
            }

            // This used to be set to size 0.  However, in ES5, this is not allowed:
            // https://www.elastic.co/guide/en/elasticsearch/reference/5.0/breaking_50_aggregations_changes.html
            // Here is why:
            // https://www.elastic.co/guide/en/elasticsearch/guide/current/_preventing_combinatorial_explosions.html
            // We are supposed to set it to something reasonable (????).  I have no idea, so using 100
            def termsAggregations = AggregationBuilders.terms('distinct').field(query.fields[0]).size(TERM_AGGREGATION_SIZE)
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


    private static List<SingularWhereClause> getCountFieldClauses(Query query) {
        def clauses = []
        query.aggregates.each { AggregateClause it ->
            if (isCountFieldAggregation(it)) {
                clauses.push(new SingularWhereClause(lhs: it.field, operator: '!=', rhs: null))
            }
        }
        return clauses
    }

    static SearchSourceBuilder createSearchSourceBuilder(Query params) {
        new SearchSourceBuilder()
                .explain(false)
                .from(getOffset(params))
                .size(getTotalLimit(params))
    }

    static int getOffset(Query query) {
        return (query?.offsetClause ? query.offsetClause.offset : 0) as int
    }

    static int getTotalLimit(Query query) {
        if (query.groupByClauses || query.aggregates) {
            return 0
        }

        return getLimit(query)
    }

    static int getLimit(Query query, Boolean supportsUnlimited = false) {
        if (query?.limitClause) {
            return query.limitClause.limit as int
        }

        if (supportsUnlimited) {
            return 0
        }

        return Math.max(RESULT_LIMIT - getOffset(query), 0)
    }

    static SearchRequest createSearchRequest(SearchSourceBuilder source, Query params) {
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

    static boolean isCountAllAggregation(clause) {
        clause && clause.operation == 'count' && clause.field == '*'
    }

    static boolean isCountFieldAggregation(clause) {
        clause && clause.operation == 'count' && clause.field != '*'
    }


    private static List<WhereClause> createWhereClausesForFilters(DataSet dataSet,
                                                                  filterCache,
                                                                  ignoredFilterIds = []) {
        List<WhereClause> filterWhereClauses = filterCache.getFilterKeysForDataset(dataSet)
                .findAll { !(it.id in ignoredFilterIds) && it.filter.whereClause }
                .collect { it.filter.whereClause }
        return filterWhereClauses
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
                    case 'year': return template(interval: YEAR, format: 'yyyy')
                    case 'month': return template(interval: MONTH, format: 'M')
                    case 'dayOfMonth': return template(interval: DAY, format: 'd')
                    case 'dayOfWeek': return template(interval: DAY, format: 'e')
                    case 'hour': return template(interval: HOUR, format: 'H')
                    case 'minute': return template(interval: MINUTE, format: 'm')
                    case 'second': return template(interval: SECOND, format: 's')
                }
            }
        }

        throw new NeonConnectionException("Unknown groupByClause: ${clause.getClass()}")
    }
}
