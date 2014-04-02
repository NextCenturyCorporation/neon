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
        if (!options.ignoreFilters) {
            whereClauses.addAll(createWhereClausesForFilters(dataSet, filterState))
        }
        if (options.selectionOnly) {
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
