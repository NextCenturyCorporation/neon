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
import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import org.elasticsearch.index.query.FilterBuilder
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Interval

/**
 * Helper methods that are specific to Elasticsearch 1.x
 */
SuppressWarnings("ClassNameSameAsFilename")
class ElasticSearchConversionStrategyHelper {
    // Elasticsearch v2.x only supports getting the first 10000 records
    public static final RESULT_LIMIT = Integer.MAX_VALUE

    public static final YEAR = Interval.YEAR
    public static final MONTH = Interval.MONTH
    public static final DAY = Interval.DAY
    public static final HOUR = Interval.HOUR
    public static final MINUTE = Interval.MINUTE
    public static final SECOND = Interval.SECOND

    public static FilterBuilder convertWhereClauses(whereClauses) {
        //Build the elasticsearch filters for the where clauses
        def inners = whereClauses.collect(ElasticSearchConversionStrategyHelper.&convertWhereClause) as FilterBuilder[]
        def whereFilter = FilterBuilders.boolFilter().must(FilterBuilders.andFilter(inners))
        return whereFilter
    }

    private static FilterBuilder convertWhereClause(clause) {
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
        def inners = clause.whereClauses.collect(ElasticSearchConversionStrategyHelper.&convertWhereClause)
        FilterBuilders.boolFilter().must(combiner(inners as FilterBuilder[]))
    }

    @SuppressWarnings("MethodSize")
    public static FilterBuilder convertSingularWhereClause(clause) {
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
            def hasValue = clause.rhs || clause.rhs == '' || clause.rhs == false

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

    public static def createHeatmapAggregation(HeatmapBoundsQuery boundingBox) {
        def hashGrid = AggregationBuilders.geohashGrid('heatmap').field(boundingBox.locationField).precision(boundingBox.gridCount)
        def filter  = FilterBuilders.geoBoundingBoxFilter(boundingBox.locationField).bottomLeft(boundingBox.minLat, boundingBox.minLon).topRight(boundingBox.maxLat, boundingBox.maxLon)
        def bounds = AggregationBuilders.filter('bounds').filter(filter).subAggregation(hashGrid)
        return bounds
    }
}

