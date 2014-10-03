/*
 * Copyright 2013 Next Century Corporation
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

package com.ncc.neon.query.shark
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import groovy.transform.Immutable


/**
 * Converts a Query object into a spark SQL based query.
 */

@Immutable
class SparkSQLConversionStrategy {

    private final FilterState filterState
    private final SelectionState selectionState

    String convertQuery(Query query, QueryOptions queryOptions) {
        StringBuilder builder = new StringBuilder()
        applySelectFromStatement(builder, query)
        applyWhereStatement(builder, query, queryOptions)
        applyGroupByStatement(builder, query)
        applySortByStatement(builder, query)
        applyLimitStatement(builder, query)
        return builder.toString()

    }

    private static void applySelectFromStatement(StringBuilder builder, Query query) {
        def modifier = query.isDistinct ? "DISTINCT " : ""
        builder << "select ${modifier}" << buildFieldList(query) << " from " << query.filter.databaseName << "." << query.filter.tableName
    }

    private static def buildFieldList(Query query) {
        def fields = []
        query.aggregates.each { aggregate ->
            fields << functionToString(aggregate)
        }
        query.groupByClauses.each { groupBy ->
            fields << groupByClauseToString(groupBy)
        }
        // if there are aggregates in the field, those and the group by fields are the only valid values to return
        // and the hive/jdbc drivers can return some strange results
        // https://issues.apache.org/jira/browse/HIVE-4392,
        // https://issues.apache.org/jira/browse/HIVE-4522
        // so don't allow fields not grouped on
        if (!fields) {
            fields.addAll(query.fields.collect { escapeFieldName(it) })
        }
        return fields.join(", ")
    }

    private static String escapeFieldName(String fieldName) {
        return fieldName?.startsWith("_") ? "`${fieldName}`" : fieldName
    }

    private static String groupByClauseToString(GroupByClause groupBy) {
        groupBy instanceof GroupByFieldClause ? escapeFieldName(groupBy.field) : functionToString(groupBy)
    }

    private static String functionToString(FieldFunction fieldFunction) {
        return "${fieldFunction.operation}(${escapeFieldName(fieldFunction.field)}) as ${fieldFunction.name}"
    }

    private void applyWhereStatement(StringBuilder builder, Query query, QueryOptions queryOptions) {
        List whereClauses = collectWhereClauses(query, queryOptions)

        SparkSQLWhereClause clause = createWhereClauseParams(whereClauses)
        if (clause) {
            builder << " where " << clause.toString()
        }
    }

    private List collectWhereClauses(Query query, QueryOptions options) {
        def whereClauses = []

        if (query.filter.whereClause) {
            whereClauses << query.filter.whereClause
        }
        DataSet dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState, options.ignoredFilterIds))
        }
        if (options.selectionOnly) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }

        return whereClauses
    }

    private static def createWhereClausesForFilters(DataSet dataSet, def filterCache, def ignoredFilterIds = []) {
        def whereClauses = []

        List<FilterKey> filterKeys = filterCache.getFilterKeysForDataset(dataSet)
        filterKeys.each {
            String id = it.id
            if (!ignoredFilterIds.contains(id)) {
                Filter filter = it.filter
                if (filter.whereClause) {
                    whereClauses << filter.whereClause
                }
            }
        }
        return whereClauses
    }

    private static void applyGroupByStatement(StringBuilder builder, Query query) {
        def groupByClauses = []
        groupByClauses.addAll(query.groupByClauses)

        if (groupByClauses) {
            // spark sql doesn't support grouping by the field alias so we actually need to provide the field function again
            builder << " group by " << groupByClauses.collect { groupByClauseToString(it).split(" ")[0] }.join(", ")
        }

    }

    private static void applySortByStatement(StringBuilder builder, Query query) {
        List sortClauses = query.sortClauses
        if (sortClauses) {
            builder << " order by " << sortClauses.collect { escapeFieldName(it.fieldName) + ((it.sortOrder == SortOrder.ASCENDING) ? " ASC" : " DESC") }.join(", ")
        }
    }

    private static void applyLimitStatement(StringBuilder builder, Query query) {
        if (query.limitClause != null) {
            builder << " limit " << query.limitClause.limit
        }
    }

    private static SparkSQLWhereClause createWhereClauseParams(List whereClauses) {
        if (!whereClauses) {
            return null
        }
        if (whereClauses.size() == 1) {
            return new SparkSQLWhereClause(whereClause: whereClauses[0])
        }
        return new SparkSQLWhereClause(whereClause: new AndWhereClause(whereClauses: whereClauses))
    }

}
