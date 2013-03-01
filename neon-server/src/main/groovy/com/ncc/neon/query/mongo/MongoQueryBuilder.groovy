package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.ncc.neon.query.QueryBuilder
import com.ncc.neon.query.clauses.*

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
 */
class MongoQueryBuilder implements QueryBuilder {

    /** the mongodb object that represents the query */
    private def dbObject

    private def selectClause
    private def distinctClause
    private def groupByClauses = []
    private def aggregateClauses = []
    private def sortClauses = []

    MongoQueryBuilder() {
        dbObject = new BasicDBObject()
    }

    @Override
    def apply(SelectClause clause) {
        selectClause = clause
    }

    @Override
    def apply(SingularWhereClause clause) {
        dbObject = MongoWhereClauseBuilder.build(clause)
    }

    @Override
    def apply(AndWhereClause clause) {
        dbObject = MongoWhereClauseBuilder.build(clause)
    }

    @Override
    def apply(OrWhereClause clause) {
        dbObject = MongoWhereClauseBuilder.build(clause)
    }

    @Override
    def apply(DistinctClause clause) {
        distinctClause = clause
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
        aggregateClauses << clause
    }

    @Override
    def apply(SortClause clause) {
        sortClauses << clause
    }

    @Override
    def build() {
        return new MongoQuery(
                dbObject: dbObject,
                selectClause: selectClause,
                distinctClause: distinctClause,
                groupByClauses: groupByClauses,
                aggregateClauses: aggregateClauses,
                sortClauses: sortClauses
        )
    }
}
