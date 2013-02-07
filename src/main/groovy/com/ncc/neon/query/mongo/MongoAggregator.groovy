package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject

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
 * Combines the aggregation (sum, count, etc) and the group by clauses
 */
class MongoAggregator {


    static def buildAggregateClauses(aggregationClauses, groupByClause) {
        def groupFields = new BasicDBObject()
        def projFields = new BasicDBObject()

        applyGroupByClauses(groupFields, groupByClause, projFields)
        applyAggregationClauses(aggregationClauses, groupFields, projFields)

        def group = new BasicDBObject('$group', groupFields)
        def proj = new BasicDBObject('$project',projFields)
        return [group,proj]
    }

    private static void applyGroupByClauses(groupFields, groupByClause, projFields) {
        def idFields = new BasicDBObject()
        groupFields.put('_id', idFields)
        groupByClause.fields.each {
            def val = '$' + it
            idFields.put(it, val)
            // project all of the id fields to separate fields for easier reference on the client side
            projFields.put(it, '$_id.' + it);
        }
    }

    private static void applyAggregationClauses(aggregationClauses, groupFields, projFields) {
        aggregationClauses.each {
            groupFields.put(it.name, resolveAggregationOperation(it.aggregationOperation, it.aggregationField))
            // ensure all of the fields from the aggregation operations are shown in the result
            projFields.put(it.name, 1)
        }
    }

    static def resolveAggregationOperation(aggregationOp, aggregationField) {
        def lhs = '$' + aggregationOp;
        def rhs = '$' + aggregationField;
        // count is implemented as sum with a value of 1
        if ( aggregationOp == 'count') {
            lhs = '$sum';
            rhs = 1;
        }
        return new BasicDBObject(lhs,rhs)
    }
}
