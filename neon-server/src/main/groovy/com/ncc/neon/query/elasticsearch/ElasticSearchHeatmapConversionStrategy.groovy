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

import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions

import org.elasticsearch.action.search.SearchType
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import groovy.transform.Immutable

/**
 * Converts a Query object into a BasicDbObject
 */
@Immutable
class ElasticSearchHeatmapConversionStrategy {
    static final String TERM_PREFIX = "_term"
    static final String STATS_AGG_PREFIX = "_statsFor_"
    static final String[] DATE_OPERATIONS = ['year', 'month', 'dayOfMonth', 'dayOfWeek', 'hour', 'minute', 'second']

    private final FilterState filterState
    private final SelectionState selectionState

    public SearchRequest convertQuery(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        def source = createSourceBuilderWithState(query, options)

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        injectHeatmapAggregation(source, boundingBox)

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

        //Build the elasticsearch filters for the where clauses
        def inners = whereClauses.collect(ElasticSearchConversionStrategy.&convertWhereClause) as FilterBuilder[]
        def whereFilter = FilterBuilders.boolFilter().must(FilterBuilders.andFilter(inners))

        return createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))
    }

    /*
     * inject heatmap aggregation and bounding box
     */
    private static injectHeatmapAggregation(SearchSourceBuilder source, HeatmapBoundsQuery boundingBox) {
        def hashGrid = AggregationBuilders.geohashGrid('heatmap').field(boundingBox.locationField).precision(boundingBox.gridCount)
        def filter  = FilterBuilders.geoBoundingBoxFilter(boundingBox.locationField).bottomLeft(boundingBox.minLat, boundingBox.minLon).topRight(boundingBox.maxLat, boundingBox.maxLon)
        def bounds = AggregationBuilders.filter('bounds').filter(filter).subAggregation(hashGrid)

        source.aggregation(bounds)
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

        return (Integer.MAX_VALUE - getOffset(query))
    }

    public static SearchRequest createSearchRequest(SearchSourceBuilder source, Query params) {
        new SearchRequest()
                .searchType((params?.aggregates) ? SearchType.COUNT : SearchType.DFS_QUERY_THEN_FETCH)
                .source(source)
                .indices(params?.filter?.databaseName ?: '_all')
                .types(params?.filter?.tableName ?: '_all')
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

    public static FilterBuilder convertWhereClause(clause) {
        switch (clause.getClass()) {
            case AndWhereClause:
                return convertBooleanWhereClause(clause, FilterBuilders.&andFilter)
            case OrWhereClause:
                return convertBooleanWhereClause(clause, FilterBuilders.&orFilter)
            case SingularWhereClause:
                return convertSingularWhereClause(clause)
            default:
                throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
    }

    private static FilterBuilder convertBooleanWhereClause(clause, Closure<FilterBuilder> combiner) {
        def inners = clause.whereClauses.collect(ElasticSearchConversionStrategy.&convertWhereClause)
        FilterBuilders.boolFilter().must(combiner(inners as FilterBuilder[]))
    }

    @SuppressWarnings("MethodSize")
    private static FilterBuilder convertSingularWhereClause(clause) {
        if (clause.operator in ['<', '>', '<=', '>=']) {
            return { switch (clause.operator) {
                case '<': return it.&lt
                case '>': return it.&gt
                case '<=': return it.&lte
                case '>=': return it.&gte
            }}.call(FilterBuilders.rangeFilter(clause.lhs as String))(clause.rhs as String)
        }

        if (clause.operator in ['contains', 'not contains', 'notcontains']) {
            def regexFilter = FilterBuilders.regexpFilter(clause.lhs as String, ".*${clause.rhs}.*" as String)
            return clause.operator == 'contains' ? regexFilter : FilterBuilders.notFilter(regexFilter)
        }

        if (clause.operator in ['=', '!=']) {
            def hasValue = clause.rhs || clause.rhs == ''

            def filter = hasValue ?
                    FilterBuilders.termFilter(clause.lhs as String, clause.rhs as String) :
                    FilterBuilders.existsFilter(clause.lhs as String)

            return (clause.operator == '!=') == !hasValue ? filter : FilterBuilders.notFilter(filter)
        }

        if (clause.operator in ["in", "notin"]) {
            def filter = FilterBuilders.termsFilter(clause.lhs as String, clause.rhs as String[])
            return (clause.operator == "in") ? filter : FilterBuilders.notFilter(filter)
        }

        throw new NeonConnectionException("${clause.operator} is an invalid operator for a where clause")
    }
}
