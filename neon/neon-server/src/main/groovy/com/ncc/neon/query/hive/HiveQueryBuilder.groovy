package com.ncc.neon.query.hive

import com.ncc.neon.query.QueryBuilder

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

import com.ncc.neon.query.clauses.AggregateClause
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.DistinctClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.LimitClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.clauses.WithinDistanceClause

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */


class HiveQueryBuilder implements QueryBuilder {

    def selectClause
    def whereClause
    def groupByClauses = []
    def clusterByClauses = []
    def sortClauses = []
    def limitClause

    @Override
    def apply(SelectClause clause) {
        selectClause = clause
    }

    @Override
    def apply(SingularWhereClause clause) {
        whereClause = new HiveWhereClause(whereClause: clause)
    }

    @Override
    def apply(WithinDistanceClause clause) {
        whereClause = clause
    }

    @Override
    def apply(AndWhereClause clause) {
        whereClause = new HiveWhereClause(whereClause: clause)
    }

    @Override
    def apply(OrWhereClause clause) {
        whereClause = new HiveWhereClause(whereClause: clause)
    }

    @Override
    def apply(DistinctClause clause) {
        // TODO: refactor DISTINCT feature in order to support this correctly
    }

    @Override
    def apply(GroupByFunctionClause clause) {
        groupByClauses << clause
    }

    @Override
    def apply(GroupByFieldClause clause) {
        groupByClauses << clause
    }

    @Override
    def apply(AggregateClause clause) {
        // TODO: Figure out if this will be possible for Hive
    }

    @Override
    def apply(SortClause clause) {
        sortClauses << clause
    }

    @Override
    def apply(LimitClause clause) {
        limitClause = clause
    }

    @Override
    def build() {
        return new HiveQuery(
            selectClause: selectClause,
            whereClause: whereClause,
            groupByClauses: groupByClauses,
            clusterByClauses: clusterByClauses,
            sortClauses: sortClauses,
            limitClause: limitClause
        )
    }
}
