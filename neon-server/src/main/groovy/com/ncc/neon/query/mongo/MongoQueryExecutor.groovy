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

import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.BasicDBObject
import com.ncc.neon.connect.ConnectionManager
import com.ncc.neon.util.ResourceNotFoundException
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.clauses.WhereClause
import com.ncc.neon.query.executor.AbstractQueryExecutor
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterState
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.ArrayCountPair

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Executes queries against a mongo data store
 */
@Component
class MongoQueryExecutor extends AbstractQueryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    private static final GET_FIELD_NAMES_LIMIT = 1000

    @Autowired
    private SelectionState selectionState

    @Autowired
    private ConnectionManager connectionManager

    @Override
    QueryResult doExecute(Query query, QueryOptions options, FilterState filterState) {
        AbstractMongoQueryWorker worker = createMongoQueryWorker(query)
        MongoConversionStrategy mongoConversionStrategy = new MongoConversionStrategy(filterState: filterState, selectionState: selectionState)
        MongoQuery mongoQuery = mongoConversionStrategy.convertQuery(query, options)
        return getQueryResult(worker, mongoQuery)
    }

    QueryResult getQueryResult(AbstractMongoQueryWorker worker, MongoQuery mongoQuery) {
        QueryResult queryResult = SimpleQueryCache.getSimpleQueryCacheInstance().get(mongoQuery)
        if(!queryResult) {
            queryResult = worker.executeQuery(mongoQuery)
            SimpleQueryCache.getSimpleQueryCacheInstance().put(mongoQuery, queryResult)
        }
        return queryResult
    }

    @Override
    List<String> showDatabases() {
        LOGGER.debug("Executing getDatabaseNames")
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName) {
        LOGGER.debug("Executing getCollectionNames on database {}", dbName)
        if (!mongo.databaseNames.contains(dbName)) {
            throw new ResourceNotFoundException("Database ${dbName} does not exist")
        }
        DB database = mongo.getDB(dbName)
        database.getCollectionNames().collect { it }
    }

    @Override
    List<String> getFieldNames(String databaseName, String tableName) {
        checkDatabaseAndTableExists(databaseName, tableName)
        def db = mongo.getDB(databaseName)
        def collection = db.getCollection(tableName)
        def resultSet = collection.find().limit(GET_FIELD_NAMES_LIMIT)
        Set<String> fieldNameSet = [] as Set
        resultSet.each { result ->
            fieldNameSet.addAll(getFieldsInObject(result, null))
        }
        return (fieldNameSet as List) ?: []
    }

    @Override
    Map getFieldTypes(String databaseName, String tableName) {
        checkDatabaseAndTableExists(databaseName, tableName)
        def db = mongo.getDB(databaseName)
        if(!db.collectionExists(tableName)) {
            throw new ResourceNotFoundException("Table ${tableName} does not exist")
        }
        def collection = db.getCollection(tableName)
        def resultSet = collection.find().limit(GET_FIELD_NAMES_LIMIT)
        Map fieldTypesMap = [:]
        resultSet.each { result ->
            fieldTypesMap.putAll(getFieldTypeInObject(result, null))
        }
        return fieldTypesMap
    }

    private Map getFieldTypeInObject(BasicDBObject fieldObj, String field) {
        Map fieldTypesMap = [:]
        fieldObj?.keySet().each { subField ->
            def subFieldObj = fieldObj.get(subField)
            String fieldName = subField
            if(field) {
                fieldName = field + "." + subField
            }
            if(subFieldObj != "" && !fieldTypesMap[fieldName]) {
                // Convert types to make consistent with spark and elasticsearch
                if(subFieldObj instanceof List) {
                    fieldTypesMap.put(fieldName, "array")
                } else if(subFieldObj instanceof String || subFieldObj instanceof org.bson.types.ObjectId) {
                    fieldTypesMap.put(fieldName, "string")
                } else if(subFieldObj instanceof Date) {
                    fieldTypesMap.put(fieldName, "date")
                } else if(subFieldObj instanceof Float) {
                    fieldTypesMap.put(fieldName, "float")
                } else if(subFieldObj instanceof Double) {
                    fieldTypesMap.put(fieldName, "double")
                } else if(subFieldObj instanceof Long) {
                    fieldTypesMap.put(fieldName, "long")
                } else if(subFieldObj instanceof Integer) {
                    fieldTypesMap.put(fieldName, "integer")
                } else if(subFieldObj instanceof Boolean) {
                    fieldTypesMap.put(fieldName, "boolean")
                } else if(subFieldObj instanceof org.bson.types.Binary) {
                    fieldTypesMap.put(fieldName, "binary")
                } else if(subFieldObj instanceof Object) {
                    fieldTypesMap.putAll(getFieldTypeInObject(subFieldObj, fieldName))
                }
            }
        }
        return fieldTypesMap
    }

    private Set<String> getFieldsInObject(BasicDBObject fieldObj, String field) {
        Set<String> fieldNameSet = [] as Set
        fieldObj?.keySet().each { subField ->
            def subFieldObj = fieldObj.get(subField)
            String fieldName = subField
            if(field) {
                fieldName = field + "." + subField
            }
            if(subFieldObj instanceof BasicDBObject) {
                fieldNameSet.addAll(getFieldsInObject(subFieldObj, fieldName))
            } else {
                fieldNameSet.add(fieldName)
            }
        }
        return fieldNameSet
    }

    private AbstractMongoQueryWorker createMongoQueryWorker(Query query) {
        if (query.isDistinct) {
            LOGGER.trace("Using distinct mongo query worker")
            return new DistinctMongoQueryWorker(mongo)
        } else if (query.aggregates || query.groupByClauses) {
            if (query.groupByClauses.size() == 0 && query.aggregates.size() == 1) {
                def aggregate = query.aggregates[0]
                if (aggregate.operation == 'count' && aggregate.field == '*') {
                    LOGGER.trace("Using simple count query worker")
                    return new SimpleMongoCountWorker(mongo)
                }
            }
            LOGGER.trace("Using aggregate mongo query worker")
            return new AggregateMongoQueryWorker(mongo)
        }
        LOGGER.trace("Using simple mongo query worker")
        return new SimpleMongoQueryWorker(mongo)
    }

    private MongoClient getMongo() {
        connectionManager.connection.mongo
    }

    private boolean isFieldArray(String databaseName, String tableName, String fieldName) {
        checkDatabaseAndTableExists(databaseName, tableName)
        def db = mongo.getDB(databaseName)
        if(!db.collectionExists(tableName)) {
            throw new ResourceNotFoundException("Table ${tableName} does not exist")
        }
        def collection = db.getCollection(tableName)
        def resultSet = collection.find().limit(GET_FIELD_NAMES_LIMIT)
        def fieldTypeArray = false
        while(resultSet.hasNext()) {
            def result = resultSet.next()
            def fieldObj = getFieldInResult(fieldName, result)
            if(fieldObj != "" && fieldObj != null) {
                fieldTypeArray = fieldObj instanceof List
                break
            }
        }
        return fieldTypeArray
    }

    private Object getFieldInResult(String fieldName, BasicDBObject result) {
        def fieldNameArray = fieldName.split(/\./)
        def fieldObj = result
        fieldNameArray.each { field ->
            if(fieldObj) {
                fieldObj = fieldObj.get(field)
            }
        }
        return fieldObj
    }

    List<ArrayCountPair> getArrayCounts(String databaseName, String tableName, String field, int limit, FilterState filterState, WhereClause whereClause = null) {
        checkDatabaseAndTableExists(databaseName, tableName)
        DB database = mongo.getDB(databaseName)
        ArrayCountQueryWorker worker = new ArrayCountQueryWorker(mongo).withDatabase(database)
        Query query = new Query(filter: new Filter(databaseName: databaseName, tableName: tableName))
        boolean isFieldArray = isFieldArray(databaseName, tableName, field)
        MongoQuery mongoQuery = worker.createArrayCountQuery(new MongoQuery(query: query), field, limit, filterState, selectionState, isFieldArray, whereClause)
        return getQueryResult(worker, mongoQuery).getData()
    }

    private void checkDatabaseAndTableExists(String databaseName, String tableName) {
        if (!mongo.databaseNames.contains(databaseName)) {
            throw new ResourceNotFoundException("Database ${databaseName} does not exist")
        }
        def db = mongo.getDB(databaseName)
        if(!db.collectionExists(tableName)) {
            throw new ResourceNotFoundException("Table ${tableName} does not exist")
        }
    }
}
