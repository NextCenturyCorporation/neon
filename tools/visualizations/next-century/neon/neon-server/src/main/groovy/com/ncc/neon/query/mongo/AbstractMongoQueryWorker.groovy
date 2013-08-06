package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.MongoClient
import com.ncc.neon.query.QueryResult

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
 *
 * 
 * @author tbrooks
 */

/**
 * Mongo has different operations for distinct, aggregate, and find. Subclasses
 * perform these different operations
 */

abstract class AbstractMongoQueryWorker {
    protected static final ASCENDING_STRING_COMPARATOR = { a, b -> a <=> b }
    protected static final DESCENDING_STRING_COMPARATOR = { a, b -> b <=> a }

    private final MongoClient mongo

    protected AbstractMongoQueryWorker(MongoClient mongo) {
        this.mongo = mongo
    }

    abstract QueryResult executeQuery(MongoQuery query)

    protected DBCollection getCollection(MongoQuery mongoQuery) {
        def db = mongo.getDB(mongoQuery.query.dataStoreName)
        return db.getCollection(mongoQuery.query.databaseName)
    }

    protected def createSortDBObject(sortClauses) {
        def sortDBObject = new BasicDBObject()
        sortClauses.each {
            sortDBObject.append(it.fieldName, it.sortDirection)
        }
        return sortDBObject
    }
}
