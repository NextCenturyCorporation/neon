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

import com.mongodb.DBCursor
import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class SimpleMongoQueryWorker extends AbstractMongoQueryWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMongoQueryWorker)
    SimpleMongoQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        LOGGER.debug("Executing query: {}", mongoQuery)
        DBCursor results = queryDB(mongoQuery)

        if (mongoQuery.query.sortClauses) {
            results = results.sort(createSortDBObject(mongoQuery.query.sortClauses))
        }
        if (mongoQuery.query.limitClause) {
            results = results.limit(mongoQuery.query.limitClause.limit)
        }
        if (mongoQuery.query.offsetClause) {
            results = results.skip(mongoQuery.query.offsetClause.offset)
        }
        return new MongoQueryResult(results)
    }

    private DBCursor queryDB(MongoQuery query) {
        return getCollection(query).find(query.whereClauseParams, query.selectParams)
    }
}
