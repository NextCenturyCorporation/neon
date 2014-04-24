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

package com.ncc.neon.services.demo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.mongo.MongoConversionStrategy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MongoNeonHelper {

    @Autowired
    FilterState filterState

    @Autowired
    SelectionState selectionState

    DBObject mergeWithNeonFilters(DBObject query, String databaseName, String collectionName ) {
        // hook into some methods here that will use neon's filters/selection since this query can't yet be executed through neon
        DataSet dataSet = new DataSet(databaseName: databaseName, tableName: collectionName)
        List neonFiltersAndSelection = []

        neonFiltersAndSelection.addAll(MongoConversionStrategy.createWhereClausesForFilters(dataSet, filterState))

        // the demo only shows selected data - right now selection is basically a temporary filter so only show selected
        neonFiltersAndSelection.addAll(MongoConversionStrategy.createWhereClausesForFilters(dataSet, selectionState))

        // TODO: Do we need to flatten  - the lists added to this should be empty, but it looks like this list contains 2 empty list objects if not flattened first
        neonFiltersAndSelection = neonFiltersAndSelection.flatten()

        if (neonFiltersAndSelection) {
            DBObject matchNeonFilters = MongoConversionStrategy.buildMongoWhereClause((List) neonFiltersAndSelection)
            return new BasicDBObject('$and', [matchNeonFilters, query])
        }
        // no neon filters/selection, just return the original query
        return query
    }

}
