package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause

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
class MongoAggregationClauseBuilder {


    private MongoAggregationClauseBuilder() {
        // utility class, no public constructor needed
    }

    static def buildAggregateClauses(selectClause, aggregationClauses, groupByClauses) {
        def groupFields = new BasicDBObject()
        def projFields = new BasicDBObject()

        applyGroupByClauses(groupFields, groupByClauses, projFields)
        applyAggregationClauses(aggregationClauses, groupFields, projFields)
        projectOnlySelectedFields(selectClause, projFields)

        def group = new BasicDBObject('$group', groupFields)
        def proj = new BasicDBObject('$project', projFields)

        return [group, proj]
    }

    private static void applyGroupByClauses(groupFields, groupByClauses, projFields) {
        def idFields = new BasicDBObject()
        groupFields.put('_id', idFields)
        groupByClauses.each {
            def projField
            if (it instanceof GroupByFieldClause) {
                idFields.put(it.field, '$' + it.field)
                projField = it.field
            } else if (it instanceof GroupByFunctionClause) {
                idFields.put(it.name, createFunctionDBObject(it.operation, it.field))
                // when using a function to compute a field, the resulting field is projected, not the original field
                projField = it.name
            } else {
                // this shouldn't happen so make it an error
                throw new Error("Unknown group by clause: type = ${it.class}, val = ${it}")
            }
            projFields.put(projField, '$_id.' + projField)
        }
    }

    private static void applyAggregationClauses(aggregationClauses, groupFields, projFields) {
        aggregationClauses.each {
            groupFields.put(it.name, createFunctionDBObject(it.operation, it.field))
            // ensure all of the fields from the aggregation operations are shown in the result
            projFields.put(it.name, 1)
        }
    }

    private static void projectOnlySelectedFields(selectClause, projFields) {
        if (!selectClause.selectAllFields) {
            // create a new map of projected fields that is the intersection between
            // this specified fields and the available projected fields
            // don't use groovy's submap here since we will clear the backing list and replace its contents
            def selectedFields = [:]
            selectClause.fields.each {
                if (projFields.containsKey(it)) {
                    selectedFields[it] = projFields[it]
                }
            }
            projFields.clear()
            projFields.putAll(selectedFields)
        }
    }

    private static def createFunctionDBObject(function, field) {
        def lhs = '$' + function
        def rhs = '$' + field
        // count is implemented as sum with a value of 1
        if (function == 'count') {
            lhs = '$sum'
            rhs = 1
        }
        return new BasicDBObject(lhs, rhs)
    }
}
