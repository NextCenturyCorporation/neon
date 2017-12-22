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

import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval

import com.ncc.neon.connect.NeonConnectionException
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WhereClause

/**
 * Helper methods that are specific to Elasticsearch 2.x
 */
class ElasticSearch5ConversionStrategyHelper {
    // Elasticsearch v2.x only supports getting the first 10000 records
    public static final RESULT_LIMIT = 10000

    public static final YEAR = DateHistogramInterval.YEAR
    public static final MONTH = DateHistogramInterval.MONTH
    public static final DAY = DateHistogramInterval.DAY
    public static final HOUR = DateHistogramInterval.HOUR
    public static final MINUTE = DateHistogramInterval.MINUTE
    public static final SECOND = DateHistogramInterval.SECOND

    public static QueryBuilder convertWhereClauses(List<WhereClause> whereClauses) {
        //Build the elasticsearch filters for the where clauses
        QueryBuilder[] inners = whereClauses.collect(
                ElasticSearch5ConversionStrategyHelper.&convertWhereClause) as QueryBuilder[]
        def whereFilter = QueryBuilders.boolQuery()
        for (def inner : inners) {
            whereFilter = whereFilter.must(inner)
        }
        return whereFilter
    }

    private static QueryBuilder convertWhereClause(WhereClause clause) {
        switch (clause.getClass()) {
            case AndWhereClause:
                return convertBooleanWhereClause(clause, QueryBuilders.boolQuery().&must)
            case OrWhereClause:
                return convertBooleanWhereClause(clause, QueryBuilders.boolQuery().&should)
            case SingularWhereClause:
                return convertSingularWhereClause(clause)
            default:
                throw new NeonConnectionException("Unknown where clause: ${clause.getClass()}")
        }
    }

    private static QueryBuilder convertBooleanWhereClause(WhereClause clause, Closure<QueryBuilder> combiner) {
        def inners = clause.whereClauses.collect(ElasticSearch5ConversionStrategyHelper.&convertWhereClause)
        QueryBuilders.boolQuery().must(combiner(inners as QueryBuilder[]))
    }

    @SuppressWarnings("MethodSize")
    public static QueryBuilder convertSingularWhereClause(clause) {
        if (clause.operator in ['<', '>', '<=', '>=']) {
            return {
                switch (clause.operator) {
                    case '<': return it.&lt
                    case '>': return it.&gt
                    case '<=': return it.&lte
                    case '>=': return it.&gte
                }}.call(QueryBuilders.rangeQuery(clause.lhs as String))(clause.rhs as String)
        }

        if (clause.operator in [
            'contains',
            'not contains',
            'notcontains'
        ]) {
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

    public static createHeatmapAggregation(HeatmapBoundsQuery boundingBox) {
        def hashGrid = AggregationBuilders.geohashGrid('heatmap').field(boundingBox.locationField).precision(boundingBox.gridCount)
        def filter  = QueryBuilders.geoBoundingBoxFilter(boundingBox.locationField).bottomLeft(boundingBox.minLat, boundingBox.minLon).topRight(boundingBox.maxLat, boundingBox.maxLon)
        def bounds = AggregationBuilders.filter('bounds').filter(filter).subAggregation(hashGrid)
        return bounds
    }
}
