package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.mongodb.util.JSON
import com.ncc.neon.query.Filter
import com.ncc.neon.query.FilterState
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.selection.SelectionManager
import org.springframework.beans.factory.annotation.Autowired

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
 * Executes queries against a mongodb store
 */
class MongoQueryExecutor implements QueryExecutor {

    private static final def ASC_COMPARATOR = { a,b -> a <=> b }
    private static final def DESC_COMPARATOR = { a,b -> b <=> a }

    private static final MONGO = new MongoClient();

    @Autowired
    private SelectionManager selectionManager

    @Autowired
    private FilterState filterState

    @Override
    UUID addFilter(Filter filter) {
        return filterState.addFilter(filter)
    }

    @Override
    void removeFilter(UUID id) {
        filterState.removeFilter(id)
    }

    @Override
    void clearFilters() {
        filterState.clearFilters()
    }

    @Override
    String execute(Query query, boolean includedFiltered) {
        def res = doExecuteQuery(query, null, includedFiltered)
        return JSON.serialize(res)
    }

    @Override
    void setSelectionWhere(Filter filter) {
        def res = doExecuteQuery(queryFromFilter(filter))
        def ids = res.collect { it.get('_id') }
        selectionManager.replaceSelectionWith(ids)
    }

    @Override
    void setSelectedIds(Collection<Object> ids) {
        selectionManager.replaceSelectionWith(MongoUtils.oidsToObjectIds(ids))
    }

    @Override
    void addSelectedIds(Collection<Object> ids) {
        selectionManager.addIds(MongoUtils.oidsToObjectIds(ids))
    }

    @Override
    void removeSelectedIds(Collection<Object> ids) {
        selectionManager.removeIds(MongoUtils.oidsToObjectIds(ids));
    }

    @Override
    void clearSelection() {
        selectionManager.clear()
    }

    @Override
    String getSelectionWhere(Filter filter) {
        def res = doExecuteQuery(queryFromFilter(filter), this.&wrapInSelectedItems)
        return JSON.serialize(res)
    }

    private static def queryFromFilter(def filter) {
        return new Query(filter: filter)
    }

    private def wrapInSelectedItems(clause) {
        def clauses = []
        def selectedIds = selectionManager.selectedIds
        clauses << new BasicDBObject('_id', new BasicDBObject('$in',selectedIds))
        clauses << clause
        return new BasicDBObject('$and', clauses)

    }

    def doExecuteQuery(query, dbObjPostProcessor = null, includeFiltered = false) {
        def dataSourceName = query.dataSourceName
        def datasetId = query.datasetId
        def db = MONGO.getDB(dataSourceName)
        def collection =  db.getCollection(datasetId)

        DBObject dbObject = createDbObjectForQuery(query, includeFiltered, dataSourceName, datasetId, dbObjPostProcessor)

        if ( query.distinctClause ) {
            return computeDistinctValues(collection,query,dbObject)
        }

        if ( query.groupByClause ) {
            return computeGroupByQuery(collection,query,dbObject)
        }
        return collection.find(dbObject)
    }

    private DBObject createDbObjectForQuery(query, includeFiltered, String dataSourceName, String datasetId, dbObjPostProcessor) {
        // create the DBObject for this query's filter, and then wrap it in any of the globally applied filters
        def dbObject = createDBObjectForFilter(query.filter)
        if (!includeFiltered) {
            dbObject = wrapInFilters(dataSourceName, datasetId, dbObject)
        }
        dbObject = postProcessDbObj(dbObject, dbObjPostProcessor)
        dbObject
    }

    private def wrapInFilters(dataSourceName, datasetId, dbObject) {
        def filters = filterState.getFiltersForDataset(dataSourceName, datasetId)

        if (!filters.isEmpty()) {
            def clauses = []
            filters.each {
                DBObject filterClause = createDBObjectForFilter(it)
                clauses.add(filterClause)
            }
            clauses.add(dbObject)
            return new BasicDBObject('$and', clauses)
        }
        return dbObject
    }


    private def computeDistinctValues(collection,query,dbObject) {
        def distinct = collection.distinct(query.distinctClause.field, dbObject)
        def sortOrder = query.distinctClause.sortOrder
        if ( sortOrder ) {
            distinct.sort sortOrder == 'asc' ? ASC_COMPARATOR : DESC_COMPARATOR
        }
        return distinct
    }

    private def postProcessDbObj(dbObject, clausePostProcessor) {
        if ( clausePostProcessor ) {
            return clausePostProcessor(dbObject)
        }
        return dbObject
    }

    private def computeGroupByQuery(collection, query, dbObj) {
        DBObject match = new BasicDBObject('$match', dbObj)
        def aggregates = MongoAggregator.buildAggregateClauses(query.aggregates, query.groupByClause)
        return collection.aggregate(match, aggregates as DBObject[]).results()
    }

    private def createDBObjectForFilter(def filter) {
        if ( filter.whereClause ) {
            return MongoSelectClauseResolver.buildClause(filter.whereClause)
        }
        return new BasicDBObject();
    }

    @Override
    public String getFieldNames(String dataSourceName, String datasetId) {
        def db = MONGO.getDB(dataSourceName)
        def collection =  db.getCollection(datasetId)
        def res = collection.findOne()
        def keys = res.keySet()
        def json = JSON.serialize(keys)
        return json
    }


}
