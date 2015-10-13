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
import com.ncc.neon.query.clauses.FieldFunction
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
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder
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
    static final String[] DATE_OPERATIONS = ['year', 'month', 'dayOfMonth', 'dayOfWeek', 'hour', 'minute', 'second']

    private final FilterState filterState
    private final SelectionState selectionState

    public SearchRequest convertQuery(Query query, QueryOptions options) {
        def dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        def whereClauses = collectWhereClauses(dataSet, query, options)


        //Build the elasticsearch filters for the where clauses
        def inners = whereClauses.collect(ElasticSearchConversionStrategy.&convertWhereClause) as FilterBuilder[]
        def whereFilter = FilterBuilders.boolFilter().must(FilterBuilders.andFilter(inners))

        def source = createSearchSourceBuilder(query).query(QueryBuilders.filteredQuery(null, whereFilter))

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        convertAggregations(query, source)

        return buildRequest(query, source)
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
            def bucketAggregations = query.groupByClauses.collect(ElasticSearchConversionStrategy.&convertGroupByClause.curry(query))

            //apply metricAggregations and any associated sorting to the terminal group by clause
            metricAggregations.each(bucketAggregations.last().&subAggregation)
            query.aggregates.each { ac ->
                def sc = findMatchingSortClause(query, ac)
                if (sc) {
                    def sortOrder = sc.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING
                    bucketAggregations.last().order(isCountAllAggregation(ac) ?
                            Terms.Order.count(sortOrder) :
                            Terms.Order.aggregation("${STATS_AGG_PREFIX}${ac.field}" as String,
                                    ac.operation as String, sortOrder)
                    )
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
        if(query.groupByClauses) {
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
            .searchType(SearchType.DFS_QUERY_THEN_FETCH)
            .source(source)
            .indices(params?.filter?.databaseName ?: '_all')
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

    private static SortBuilder convertSortClause(clause) {
        def order = clause.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING ?
                SortOrder.ASC : SortOrder.DESC

        SortBuilders.fieldSort(clause.fieldName as String).order(order)
    }

    @SuppressWarnings("MethodSize")
    private static AggregationBuilder convertGroupByClause(Query query, clause) {
        def applySort = { TermsBuilder tb ->
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
                    def modifier = (it == 'MONTH' ? " + 1" : "")

                    applySort(AggregationBuilders
                        .terms(clause.name as String)
                        .field(clause.field as String)
                        .script("def calendar = java.util.Calendar.getInstance(); calendar.setTime(new Date(doc['${clause.field}'].value)); calendar.get(java.util.Calendar.${it})" + modifier as String)
                        .size(0))
                }

                switch (clause.operation) {
                    case 'year': return template('YEAR')
                    case 'month': return template('MONTH')
                    case 'dayOfMonth': return template('DAY_OF_MONTH')
                    case 'dayOfWeek': return template('DAY_OF_WEEK')
                    case 'hour': return template('HOUR_OF_DAY')
                    case 'minute': return template('MINUTE')
                    case 'second': return template('SECOND')
                }
            }
        }

        throw new NeonConnectionException("Unknown groupByClause: ${clause.getClass()}")
    }
}