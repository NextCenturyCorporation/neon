package com.ncc.neon.query.mongo

import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.clauses.SortOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DistinctMongoQueryWorker)
    private static final ASCENDING_STRING_COMPARATOR = { a, b -> a <=> b }
    private static final DESCENDING_STRING_COMPARATOR = { a, b -> b <=> a }


    DistinctMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        if (mongoQuery.query.fields.size() > 1 || mongoQuery.query.fields == SelectClause.ALL_FIELDS) {
            throw new UnsupportedOperationException("mongo only supports distinct clauses on a single field")
        }
        def field = mongoQuery.query.fields[0]
        LOGGER.debug("Executing distinct query: {}", mongoQuery)
        def distinct = getCollection(mongoQuery).distinct(field, mongoQuery.whereClauseParams)

        sortDistinctResults(mongoQuery, distinct, field)
        distinct = limitDistinctResults(mongoQuery, distinct)
        // create a mapping of field to distinct values
        def distinctRows = distinct.collect { [(field): it] }


        return new MongoQueryResult(mongoIterable: distinctRows)
    }

    private List limitDistinctResults(MongoQuery mongoQuery, List distinct) {
        if (mongoQuery.query.limitClause) {
            return distinct[0..<mongoQuery.query.limitClause.limit]
        }
        return distinct
    }

    private void sortDistinctResults(MongoQuery mongoQuery, List distinct, String distinctFieldName) {
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
