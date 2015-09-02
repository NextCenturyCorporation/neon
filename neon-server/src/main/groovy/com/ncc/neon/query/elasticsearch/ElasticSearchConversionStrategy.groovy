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

import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
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
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder

import groovy.transform.Immutable

/**
 * Converts a Query object into a BasicDbObject
 */
@Immutable
class ElasticSearchConversionStrategy {
    static final String STATS_AGG_PREFIX = "_statsFor_"

    private final FilterState filterState
    private final SelectionState selectionState

    public SearchRequest convertQuery(Query query, QueryOptions options) {
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        def whereClauses = collectWhereClauses(dataSet, query, options)

        //Build the elasticsearch filters for the where clauses and sorters
        def inners = whereClauses.collect(ElasticSearchConversionStrategy.&translateWhereClause) as FilterBuilder[]
        def whereFilter = FilterBuilders.boolFilter().must(FilterBuilders.andFilter(inners))
        def sorters = (query.sortClauses ?: []).collect(ElasticSearchConversionStrategy.&translateSortClause)

        def source = createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        sorters.each(source.&sort)
        def aggregates = query.aggregates
        def aggregations = buildAggregations(aggregates)
        def groupByClauses = query.groupByClauses
        buildGroupBy(groupByClauses, source, aggregations)

        def request = buildRequest(query, source)

        return request
    }

    private buildAggregations(def aggregates) {
        //Build the metric aggregations - remove the count all aggregation since we can get that as part of the
        //search result; also, rather than pass individual metrics for each aggregation field, we'll simply
        //get stats on each field for which an aggregation exists thereby getting everything
        def aggregations = aggregates
            .findAll { !isCountAllAggregation(it) }
            .collect { it.field }
            .unique()
            .collect { AggregationBuilders.stats("${STATS_AGG_PREFIX}${it}").field(it as String) }
        return aggregations
    }

    private buildGroupBy(def groupByClauses, def source, def aggregations) {
        if (groupByClauses) {
            def groupByAggregationBuilders = groupByClauses.collect(ElasticSearchConversionStrategy.&translateGroupByClause)

            //apply aggregations to the terminal group by clause
            aggregations.each { groupByAggregationBuilders.last().subAggregation(it) }

            //on each aggregation, except the last - nest the next aggregation
            groupByAggregationBuilders
                .take(groupByAggregationBuilders.size() - 1)
                .eachWithIndex { v, i -> v.subAggregation(groupByAggregationBuilders[i + 1]) }

            source.aggregation(groupByAggregationBuilders.head() as AbstractAggregationBuilder)

        } else {
            //if there are no groupByClauses, apply metrics aggregations directly
            aggregations.each(source.&aggregation)
        }
    }

    private SearchRequest buildRequest(Query query, def source) {
        def request = createSearchRequest(source, query)

        if (query.filter?.tableName) {
            request.types([query.filter.tableName] as String[])
        }
    }

    private collectWhereClauses(DataSet dataSet, Query query, QueryOptions options) {
        def whereClauses = []
        if (query.filter?.whereClause) {
            whereClauses << query.filter.whereClause
        }
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState, options.ignoredFilterIds))
        }
        if (options.selectionOnly) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }
        return whereClauses
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

    private static boolean isCountAllAggregation(clause) {
        clause.operation == 'count' && clause.field == '*'
    }

    private static createWhereClausesForFilters(DataSet dataSet, filterCache, ignoredFilterIds = []) {
        filterCache.getFilterKeysForDataset(dataSet)
            .findAll { !(it.id in ignoredFilterIds) && it.filter.whereClause }
            .collect { it.filter.whereClause }
    }

    private static FilterBuilder translateBooleanWhereClause(clause, Closure<FilterBuilder> combiner) {
        def inners = clause.whereClauses.collect(ElasticSearchConversionStrategy.&translateWhereClause)
        FilterBuilders.boolFilter().must(combiner(inners as FilterBuilder[]))
    }

    public static FilterBuilder translateWhereClause(clause) {
        switch (clause.getClass()) {
        case AndWhereClause:
            return translateBooleanWhereClause(clause, FilterBuilders.&andFilter)
        case OrWhereClause:
            return translateBooleanWhereClause(clause, FilterBuilders.&orFilter)
        case SingularWhereClause:
            return translateSingularWhereClause(clause)
        default:
            throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
    }

    private static FilterBuilder translateSingularWhereClause(clause) {
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

        if (clause.operator in ['contains', 'not contains', 'notcontains']) {
            def regexFilter = FilterBuilders.regexpFilter(clause.lhs, ".*${clause.rhs}.*")
            return clause.operator == 'contains' ? regexFilter : FilterBuilders.notFilter(regexFilter)
        }

        if (clause.operator in ['=', '!=']) {
            def termFilter = FilterBuilders.termFilter(clause.lhs as String, clause.rhs as String)
            return clause.operator == '=' ? termFilter : FilterBuilders.notFilter(termFilter)
        }

        throw new NeonConnectionException("${clause.operator} is an invalid operator for a where clause")
    }

    private static SortBuilder translateSortClause(clause) {
        def order = clause.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING ? SortOrder.ASC : SortOrder.DESC
        SortBuilders.fieldSort(clause.fieldName).order(order)
    }

    private static AggregationBuilder translateGroupByClause(clause) {
        if (clause instanceof GroupByFieldClause) {
            return AggregationBuilders.terms(clause.field).field(clause.field as String)
        }

        if (clause instanceof GroupByFunctionClause && clause.operation in ['year', 'month', 'dayOfMonth', 'hour', 'minute', 'second']) {
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

        throw new NeonConnectionException("Unknown groupByClause: ${clause.getClass()}")
    }
}