package com.ncc.neon.query.hive

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SortOrder
import com.ncc.neon.query.filter.FilterState

/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

/**
 * Converts a Query object into a hive based query.
 */

class HiveConversionStrategy {

    private final FilterState filterState

    HiveConversionStrategy(FilterState filterState){
        this.filterState = filterState
    }

    String convertQuery(Query query, Closure additionalWhereClauseGenerator = null) {
        helpConvertQuery(query, false, additionalWhereClauseGenerator)
    }

    String convertQueryWithFilterState(Query query, Closure additionalWhereClauseGenerator = null) {
        helpConvertQuery(query, true, additionalWhereClauseGenerator)
    }

    private String helpConvertQuery(Query query, boolean includeFiltersFromFilterState, Closure additionalWhereClauseGenerator) {
        StringBuilder builder = new StringBuilder()
        applySelectFromStatement(builder, query)
        applyWhereStatement(builder, query, includeFiltersFromFilterState, additionalWhereClauseGenerator)
        applyGroupByStatement(builder, query)
        applySortByStatement(builder, query)
        applyLimitStatement(builder, query)
        return builder.toString()
    }

    private static void applySelectFromStatement(StringBuilder builder, Query query) {
        builder << "select " << query.fields.join(",") << " from " << query.filter.databaseName << "." << query.filter.tableName
    }

    private void applyWhereStatement(StringBuilder builder, Query query, boolean includeFiltersFromFilterState, Closure additionalWhereClauseGenerator){
        List whereClauses = assembleWhereClauses(query, additionalWhereClauseGenerator)
        if (includeFiltersFromFilterState) {
            whereClauses.addAll(createWhereClausesForFilters(query))
        }
        HiveWhereClause clause = createWhereClauseParams(whereClauses)
        if (clause) {
            builder << " where " << clause.toString()
        }

    }

    private static void applyGroupByStatement(StringBuilder builder, Query query){
        def groupByClauses = []
        groupByClauses.addAll(query.groupByClauses)
        groupByClauses.addAll(query.aggregates)

        if (groupByClauses) {
            builder << " group by " << groupByClauses.collect { it.field }.join(",")
        }

    }

    private static void applySortByStatement(StringBuilder builder, Query query){
        List sortClauses = query.sortClauses
        if (sortClauses) {
            builder << " order by " << sortClauses.collect { it.fieldName + ((it.sortOrder == SortOrder.ASCENDING) ? " ASC" : " DESC") }.join(",")
        }
    }

    private static void applyLimitStatement(StringBuilder builder, Query query){
        if (query.limitClause != null) {
            builder << " limit " << query.limitClause.limit
        }
    }

    private static List assembleWhereClauses(Query query, Closure additionalWhereClauseGenerator) {
        def whereClauses = []

        if (additionalWhereClauseGenerator) {
            whereClauses << additionalWhereClauseGenerator()
        }
        if (query.filter.whereClause) {
            whereClauses << query.filter.whereClause
        }
        return whereClauses
    }

    private def createWhereClausesForFilters(query) {
        def whereClauses = []
        def filters = filterState.getFiltersForDataset(query.databaseName, query.tableName)
        if (!filters.isEmpty()) {
            filters.each {
                whereClauses << it.whereClause
            }
        }
        return whereClauses
    }

    private static HiveWhereClause createWhereClauseParams(List whereClauses) {
        if (!whereClauses) {
            return null
        }
        if (whereClauses.size() == 1) {
            return new HiveWhereClause(whereClause: whereClauses[0])
        }
        return new HiveWhereClause(whereClause: new AndWhereClause(whereClauses: whereClauses))
    }
}
