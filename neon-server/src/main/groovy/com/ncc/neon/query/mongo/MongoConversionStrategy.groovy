package com.ncc.neon.query.mongo
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import groovy.transform.Immutable

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
 * Converts a Query object into a BasicDbObject
 */
@Immutable
class MongoConversionStrategy {

    private final FilterState filterState
    private final SelectionState selectionState

    /**
     * Converts a query object into an object that we can more easily use to execute mongo queries
     * @param query The query object to convert
     * @param options Determines whether we should include filters or selection
     * @return A MongoQuery object that we can use for mongo queries.
     */

    MongoQuery convertQuery(Query query, QueryOptions options) {
        MongoQuery mongoQuery = new MongoQuery(query: query)
        mongoQuery.selectParams = createSelectParams(query)
        List whereClauses = collectWhereClauses(query, options)
        mongoQuery.whereClauseParams = buildMongoWhereClause(whereClauses)
        return mongoQuery
    }

    private List collectWhereClauses(Query query, QueryOptions options) {
        def whereClauses = []

        if (query.filter.whereClause) {
            whereClauses << query.filter.whereClause
        }
        DataSet dataSet = new DataSet(databaseName: query.databaseName, tableName: query.tableName)
        if (!options.disregardFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState))
        }
        if (!options.disregardSelection) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, selectionState))
        }

        return whereClauses
    }

    private static def createWhereClausesForFilters(DataSet dataSet, def filterCache) {
        def whereClauses = []

        List<Filter> filters = filterCache.getFiltersForDataset(dataSet)
        filters.each {
            if (it.whereClause) {
                whereClauses << it.whereClause
            }
        }
        return whereClauses
    }

    private static DBObject createSelectParams(Query query) {
        BasicDBObject params = new BasicDBObject()

        if (query.fields != SelectClause.ALL_FIELDS) {
            query.fields.each {
                params[it] = 1
            }
        }

        return params
    }

    private static DBObject buildMongoWhereClause(List whereClauses) {
        if (!whereClauses) {
            return new BasicDBObject()
        }
        if (whereClauses.size() == 1) {
            return MongoWhereClauseBuilder.build(whereClauses[0])
        }
        return MongoWhereClauseBuilder.build(new AndWhereClause(whereClauses: whereClauses))
    }

}
