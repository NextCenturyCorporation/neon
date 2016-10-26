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

import com.mongodb.MongoClient
import com.mongodb.BasicDBObject
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.query.clauses.SelectClause
import com.ncc.neon.query.clauses.SortClause
import com.ncc.neon.query.clauses.SortOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory



/**
 * Used to execute a "distinct" query (returns distinct field values) against mongo
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

        if (mongoQuery.query.aggregates && mongoQuery.query.aggregates.size() == 1 &&
                mongoQuery.query.aggregates[0].operation == "count" &&
                mongoQuery.query.aggregates[0].field == mongoQuery.query.fields[0]
            ) {
            // If the query requests the distinct values of a field, and then also requests an
            // aggregate count of that field, then what is wanted is the total number of
            // distinct values for that field. This is useful when the client only wants the
            // total number of fields and doesn't want to wait for the full list to be
            // transferred over the network
            return executeDistinctCountQuery(mongoQuery)
        }
        return executeDistinctQuery(mongoQuery)
    }

    private TabularQueryResult executeDistinctCountQuery(MongoQuery mongoQuery) {
        def aggregate = mongoQuery.query.aggregates[0]
        def group = new BasicDBObject()
        group.put('_id', '$' + mongoQuery.query.fields[0])
        def countAll = new BasicDBObject()
        countAll.put('_id', 'all')
        countAll.put(aggregate.name, new BasicDBObject(
            '$sum',
            1
        ))
        def distinctCount = getCollection(mongoQuery).aggregate(
            new BasicDBObject('$match', mongoQuery.whereClauseParams),
            new BasicDBObject('$group', group),
            new BasicDBObject('$group', countAll)
        ).results()
        def distinctRows = distinctCount.collect {[(aggregate.name) : it[aggregate.name] ]}
        return new TabularQueryResult(distinctRows)
    }

    private TabularQueryResult executeDistinctQuery(MongoQuery mongoQuery) {
        def field = mongoQuery.query.fields[0]
        LOGGER.debug("Executing distinct query: {}", mongoQuery)
        def distinct = getCollection(mongoQuery).distinct(field, mongoQuery.whereClauseParams)

        sortDistinctResults(mongoQuery, distinct, field)
        distinct = limitSkipDistinctResults(mongoQuery, distinct)
        // create a mapping of field to distinct values
        def distinctRows = distinct.collect { [(field): it] }

        return new TabularQueryResult(distinctRows)
    }

    private List limitSkipDistinctResults(MongoQuery mongoQuery, List distinct) {
        int listSize = distinct.size()
        int startIndex = mongoQuery.query.offsetClause ? mongoQuery.query.offsetClause.offset : 0
        int endIndex = mongoQuery.query.limitClause ? Math.min(listSize, (startIndex + mongoQuery.query.limitClause.limit)) : listSize

        // only copy the sublist if we're not returning the whole list to avoid an unnecessary copy
        return (startIndex > 0 || endIndex < listSize) ? distinct[startIndex..<endIndex] : distinct
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
