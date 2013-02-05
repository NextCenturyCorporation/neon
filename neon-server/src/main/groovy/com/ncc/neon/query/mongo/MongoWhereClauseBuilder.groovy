package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.types.ObjectId

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
 * Creates a WHERE clause for mongodb
 */
class MongoWhereClauseBuilder implements MongoClauseBuilder {


    private static final OPERATOR_MAPPING = ['<':'$lt', '<=':'$lte', '>':'$gt', '>=':'$gte', '!=':'$ne', 'in':'$in']

    DBObject build(def clause) {
        def rhs
        def rhsVal = clause.rhs
        if ( clause.lhs == '_id') {
            rhsVal = createObjIds(rhsVal)
        }

        // no operator actually used for equals - it's just a simple key value pair
        if ( clause.op == '=') {
            rhs = rhsVal
        }
        else {
            def opString = OPERATOR_MAPPING[clause.op]
            rhs = new BasicDBObject(opString,rhsVal)
        }

        return new BasicDBObject(clause.lhs, rhs)
    }


    private def createObjIds(def rhs) {
        def retVal
        if ( rhs instanceof Collection ) {
            retVal = MongoUtils.oidsToObjectIds(rhs)
        }
        else {
            retVal =  new ObjectId(MongoUtils.oidToObjectId(rhs))
        }
        return retVal
    }
}
