package com.ncc.neon.query.mongo
import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.ncc.neon.query.AbstractQueryExecutor
import com.ncc.neon.query.QueryResult
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
 */

/**
 * Executes queries against a mongo data store
 */
class MongoQueryExecutor extends AbstractQueryExecutor {

    private static final ASCENDING_STRING_COMPARATOR = { a, b -> a <=> b }
    private static final DESCENDING_STRING_COMPARATOR = { a, b -> b <=> a }
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoQueryExecutor)

    private final MongoClient mongo

    MongoQueryExecutor(MongoClient mongo){
        this.mongo = mongo
    }

    @Override
    protected QueryResult doExecuteQuery(mongoQuery) {

        def collection = this.getCollection(mongoQuery)
        def result

        if (mongoQuery.distinctClause) {
            result = executeDistinctQuery(collection, mongoQuery)
        } else if (mongoQuery.groupByClauses) {
            result = executeGroupByQuery(collection, mongoQuery)
        } else {
            result = executeSimpleQuery(collection, mongoQuery)
        }

        return new MongoQueryResult(mongoIterable: result)
    }

    private def executeSimpleQuery(collection, mongoQuery) {
        def results = collection.find(mongoQuery.dbObject)
        if (mongoQuery.sortClauses) {
            results = results.sort(createSortDBObject(mongoQuery.sortClauses))
        }
        if (mongoQuery.limitClause) {
            results = results.limit(mongoQuery.limitClause.limit)
        }
        return results
    }

    private static def createSortDBObject(sortClauses) {
        def sortDBObject = new BasicDBObject()
        sortClauses.each {
            sortDBObject.append(it.fieldName, it.sortDirection)
        }
        return sortDBObject
    }

    private def executeDistinctQuery(collection, mongoQuery) {
        def distinctClause = mongoQuery.distinctClause
        def distinct = collection.distinct(distinctClause.fieldName, mongoQuery.dbObject)
        def distinctFieldName = distinctClause.fieldName
        if (mongoQuery.sortClauses) {
            // for now we only have one value in the distinct clause, so just see if that was provided as a sort field
            def sortClause = mongoQuery.sortClauses.find { it.fieldName == distinctFieldName }
            if (sortClause) {
                def comparator = sortClause.sortOrder == SortOrder.ASCENDING ? ASCENDING_STRING_COMPARATOR : DESCENDING_STRING_COMPARATOR
                distinct.sort comparator
            } else {
                LOGGER.warn("Field {} was specified in the distinct clause not but found in the sort clauses {}", distinctFieldName, mongoQuery.sortClauses.collect { it.fieldName })
            }
        }

        return distinct
    }

    private def executeGroupByQuery(collection, mongoQuery) {
        // the "match" clause is the query. the "additionalClauses" are the aggregate/group/sort clauses
        def match = new BasicDBObject('$match', mongoQuery.dbObject)
        def additionalClauses = MongoAggregationClauseBuilder.buildAggregateClauses(mongoQuery.aggregateClauses, mongoQuery.groupByClauses)
        if (mongoQuery.sortClauses) {
            additionalClauses << new BasicDBObject('$sort', createSortDBObject(mongoQuery.sortClauses))
        }
        return collection.aggregate(match, additionalClauses as DBObject[]).results()
    }

    private def getCollection(mongoQuery) {
        def selectClause = mongoQuery.selectClause
        def db = mongo.getDB(selectClause.dataSourceName)
        return db.getCollection(selectClause.datasetId)
    }

    @Override
    public Collection<String> getFieldNames(String dataSourceName, String datasetId) {
        def db = mongo.getDB(dataSourceName)
        def collection = db.getCollection(datasetId)
        def result = collection.findOne()
        return result.keySet()
    }

    @Override
    protected def transformIdFields(Collection<Object> ids) {
        return MongoUtils.oidsToObjectIds(ids)
    }

    @Override
    protected def getIdFieldName() {
        return "_id"
    }

    @Override
    protected createQueryBuilder() {
        return new MongoQueryBuilder()
    }

    @Override
    List<String> showDatabases(){
        mongo.databaseNames
    }

    @Override
    List<String> showTables(String dbName){
        DB database = mongo.getDB(dbName)
        database.getCollectionNames().collect { it }
    }
}
