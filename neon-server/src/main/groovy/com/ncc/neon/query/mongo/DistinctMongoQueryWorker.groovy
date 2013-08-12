package com.ncc.neon.query.mongo

import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.clauses.SortOrder

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

class DistinctMongoQueryWorker extends AbstractMongoQueryWorker {

    DistinctMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        def distinctClause = mongoQuery.query.distinctClause
        def distinct = getCollection(mongoQuery).distinct(distinctClause.fieldName, mongoQuery.whereClauseParams)

        sortDistinctResults(mongoQuery, distinct)
        distinct = limitDistinctResults(mongoQuery, distinct)

        return new MongoQueryResult(mongoIterable: distinct)
    }

    private List limitDistinctResults(MongoQuery mongoQuery, List distinct) {
        if (mongoQuery.query.limitClause) {
            return distinct[0..<mongoQuery.query.limitClause.limit]
        }
        return distinct
    }

    private void sortDistinctResults(MongoQuery mongoQuery, List distinct) {
        def distinctFieldName = mongoQuery.query.distinctClause.fieldName
        List<SortClause> sortClauses = mongoQuery.query.sortClauses
        if (sortClauses) {
            // for now we only have one value in the distinct clause, so just see if that was provided as a sort field
            def sortClause = sortClauses.find { it.fieldName == distinctFieldName }
            if (sortClause) {
                def comparator = sortClause.sortOrder == SortOrder.ASCENDING ? ASCENDING_STRING_COMPARATOR : DESCENDING_STRING_COMPARATOR
                distinct.sort comparator
            }
        }
    }
}
