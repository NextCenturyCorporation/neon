package com.ncc.neon.query.mongo

import com.mongodb.BasicDBObject
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.clauses.SelectClause



/**
 * Combines the aggregation (sum, count, etc) and the group by clauses
 */
class MongoAggregationClauseBuilder {


    private MongoAggregationClauseBuilder() {
        // utility class, no public constructor needed
    }

    static def buildAggregateClauses(aggregationClauses, groupByClauses) {
        def groupFields = new BasicDBObject()
        def projFields = new BasicDBObject()

        applyGroupByClauses(groupFields, groupByClauses, projFields)
        applyAggregationClauses(aggregationClauses, groupFields, projFields)

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

    private static def createFunctionDBObject(function, field) {
        def lhs = '$' + function
        def rhs = '$' + field
        // count is implemented as sum with a value of 1
        if (function == 'count') {
            lhs = '$sum'
            rhs = createSumRhs(field)
        }
        return new BasicDBObject(lhs, rhs)
    }

    private static def createSumRhs(field) {
        // when a specified field name is specified in the count, the count is the number of rows where that field
        // is not null
        if (field != SelectClause.ALL_FIELDS[0]) {
            return createDBObjectTOCountNonNullValues(field)
        }
        // otherwise just return the raw count
        return 1

    }

    private static def createDBObjectTOCountNonNullValues(field) {
        // this method is a little confusing but required for the mongo pipeline, but each step is documented below:

        // compare the field to null (either it's null or doesn't exist). if this returns true, return null,
        // otherwise return the original field value (as long as that value is not null, that's all that matters)
        def ifNull = new BasicDBObject('$ifNull',['$'+field,null])

        // now compare that previous value (which is either null or the original field value) against null. if
        // it is not equal to null, this condition will return true. if it is null, this condition returns false
        def notNull = new BasicDBObject('$ne', [null,ifNull])

        // if the final value is not null, add 1 to the field, otherwise don't count it
        def conditionalCount = [notNull, 1, 0]
        return new BasicDBObject('$cond', conditionalCount)

    }
}
