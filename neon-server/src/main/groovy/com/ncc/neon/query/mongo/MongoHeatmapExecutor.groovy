/*
 * Copyright 2016 Next Century Corporation
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

import com.ncc.neon.query.HeatmapBoundsQuery
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult

import org.springframework.stereotype.Component

/**
 * Executes heatmap queries against a mongo data store
 */
@Component
class MongoHeatmapExecutor extends MongoQueryExecutor{
    QueryResult execute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        // cutoff queries where there is no selection but selectionOnly was specified. otherwise the WHERE clause
        // created by the query executors to get the selected data will be empty the request for selectionOnly is
        // effectively ignored
        if (isEmptySelection(query, options)) {
            return TabularQueryResult.EMPTY
        }

        QueryResult result = doExecute(query, options, boundingBox)
        return result
    }

    QueryResult doExecute(Query query, QueryOptions options, HeatmapBoundsQuery boundingBox) {
        AbstractMongoQueryWorker worker = new HeatmapMongoQueryWorker(mongo, boundingBox)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)

        return getQueryResult(worker, mongoQuery)
    }
}
