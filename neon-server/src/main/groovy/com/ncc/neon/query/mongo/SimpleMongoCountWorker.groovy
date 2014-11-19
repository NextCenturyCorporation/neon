/*
 * Copyright 2014 Next Century Corporation
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
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleMongoCountWorker extends AbstractMongoQueryWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMongoCountWorker)
    SimpleMongoCountWorker(MongoClient mongo) {
        super(mongo)
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        LOGGER.debug("Executing count: {}", mongoQuery)
        long count = queryDB(mongoQuery)

        return new TabularQueryResult([[_id: [:], counter: count]])
    }

    private long queryDB(MongoQuery query) {
        return getCollection(query).count(query.whereClauseParams)
    }
}
