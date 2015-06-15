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
import com.mongodb.DB
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair
import com.ncc.neon.query.result.ListQueryResult

class ArrayCountQueryWorker extends AbstractMongoQueryWorker {
    private DB database

    ArrayCountQueryWorker(MongoClient mongo) {
        super(mongo)
    }

    ArrayCountQueryWorker withDatabase(DB database) {
        this.database = database
        return this
    }

    MongoQuery createArrayCountQuery(MongoQuery mongoQuery, String field, int limit, FilterState filterState, SelectionState selectionState) {
        addMatchQuery(mongoQuery, filterState, selectionState)

        DBObject project = new BasicDBObject('$project', new BasicDBObject(field, 1))
        mongoQuery.query.aggregates << project
        DBObject unwind = new BasicDBObject('$unwind', '$' + field)
        mongoQuery.query.aggregates << unwind

        addGroupFields(mongoQuery, field)
        addSort(mongoQuery)
        addLimit(mongoQuery, limit)

        return mongoQuery
    }

    @Override
    QueryResult executeQuery(MongoQuery mongoQuery) {
        Map<String, Integer> arrayCounts = [:]
        Iterator<DBObject> results = database.getCollection(mongoQuery.query.getTableName()).aggregate(mongoQuery.query.aggregates[0],
                mongoQuery.query.aggregates[1..mongoQuery.query.aggregates.size()-1].toArray(new DBObject[0])).results().iterator()
        while (results.hasNext()) {
            DBObject row = results.next()
            String key = (String) row.get('_id')
            int count = ((Number) row.get('count')).intValue()
            arrayCounts[key] = count
        }

        List<ArrayCountPair> arrayCountList = convertToList(arrayCounts)
        return new ListQueryResult(arrayCountList)
    }

    private void addMatchQuery(MongoQuery mongoQuery, FilterState filterState, SelectionState selectionState) {
        DBObject matchQuery = mergeWithNeonFilters(new BasicDBObject(), mongoQuery.query.getDatabaseName(), mongoQuery.query.getTableName(), filterState, selectionState)

        if(!((BasicDBObject)matchQuery).isEmpty()) {
            DBObject match = new BasicDBObject('$match',matchQuery)
            mongoQuery.query.aggregates << match
        }
    }

    private void addGroupFields(MongoQuery mongoQuery, String field) {
        DBObject groupFields = new BasicDBObject()
        groupFields.put('_id', '$' + field)
        groupFields.put('count', new BasicDBObject('$sum', 1))
        DBObject group = new BasicDBObject('$group', groupFields)
        mongoQuery.query.aggregates << group
    }

    private void addSort(MongoQuery mongoQuery) {
        DBObject sort = new BasicDBObject('$sort', new BasicDBObject('count', -1))
        mongoQuery.query.aggregates << sort
    }

    private void addLimit(MongoQuery mongoQuery, int limit) {
        if (limit > 0) {
            mongoQuery.query.aggregates << new BasicDBObject('$limit', limit)
        }
    }

    private List<ArrayCountPair> convertToList(Map<String, Integer> arrayCounts) {
        List<ArrayCountPair> arrayCountList = []
        arrayCounts.each { key, count ->
            arrayCountList << new ArrayCountPair(key: key, count: count)
        }
        return arrayCountList
    }

    private DBObject mergeWithNeonFilters(DBObject query, String databaseName, String collectionName, FilterState filterState, SelectionState selectionState) {
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
