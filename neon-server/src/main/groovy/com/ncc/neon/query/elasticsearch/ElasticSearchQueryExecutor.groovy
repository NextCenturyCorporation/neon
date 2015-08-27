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
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.DataSet
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

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.common.collect.ImmutableOpenMap
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder

@Component
class ElasticSearchQueryExecutor extends AbstractQueryExecutor {

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

        //Build the elasticsearch aggregations - note that count is handled as a special case
        def aggregations = query.aggregates
            .findAll({ it.operation != 'count' })
            .collect(ElasticSearchQueryExecutor.&resolveSingleValueAgg) as AbstractAggregationBuilder[]

        //Build the sorters for the sort clause
        def sorters = (query.sortClauses ?: []).collect(ElasticSearchQueryExecutor.&resolveSortClause)

        //Build the SearchQuery
        def source = new SearchSourceBuilder()
            .postFilter(whereFilter)
            .explain(false)
            .from((query.offsetClause ? query.offsetClause.offset : 0) as int)
            .size((query.limitClause ? query.limitClause.limit : 45) as int)

        if (query.fields && query.fields != SelectClause.ALL_FIELDS) {
            source.fetchSource(query.fields as String[])
        }

        sorters.each { source.sort it }
        aggregations.each { source.aggregation it }

        //Build the request
        def request = new SearchRequest()
            .searchType(SearchType.DFS_QUERY_THEN_FETCH)
            .source(source)
            .indices(query.filter?.databaseName ?: '_all')

        if (query.filter?.tableName) {
            request.types([query.filter.tableName] as String[])
        }

        //and...  search!
        def results = getClient().search(request).actionGet()

        if (query.aggregates?.size() > 0) {
            def aggs = results.aggregations?.asList()?.collect { it ->
                def aggMap = new HashMap<String, Object>()
                aggMap.put(it.name, it.value)
                aggMap
            } ?: []

            def countAgg = query.aggregates.find({ it.operation == 'count'})

            if (countAgg) {
                def countAggMap = new HashMap<String, Object>()
                countAggMap.put(countAgg.name, results.hits.totalHits)
                aggs << countAggMap
            }

            return new TabularQueryResult(aggs)
        }

        new TabularQueryResult(results.hits.collect { it.getSource() })
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing showDatabases to retrieve indices")
        getClient().admin.cluster.state({}).actionGet().state.metaData.indices.collect { it.key }
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
        return null
    }

    private Client getClient() {
        return connectionManager.connection.client
    }

    private ImmutableOpenMap getMappings() {
        getClient().admin.indices.getMappings({}).actionGet().mappings
    }

    private static resolveSortClause(clause) {
        def order = clause.sortOrder == com.ncc.neon.query.clauses.SortOrder.ASCENDING ? SortOrder.ASC : SortOrder.DESC
        SortBuilders.fieldSort(clause.fieldName).order(order)
    }

    private static resolveSingleValueAgg(clause) {
        if (clause.operation in ['avg', 'sum', 'min', 'max']) {
            return {
                switch (clause.operation) {
                case 'avg':
                    return it.&avg
                case 'sum':
                    return it.&sum
                case 'min':
                    return it.&min
                case 'max':
                    return it.&max
                }
            }.call(AggregationBuilders)(clause.name).field(clause.field)
        }

        throw new RuntimeException("${clause.operation} is not a valid aggregation")
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
        FilterBuilders.boolFilter().must(combiner(inners))
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
            def termFilter = FilterBuilders.termFilter(clause.lhs, clause.rhs)
            return clause.operator == '=' ? termFilter : FilterBuilders.notFilter(termFilter)
        }

        throw new RuntimeException("${clause.operator} is an invalid operator for a where clause")
    }
}
