package com.ncc.neon.query.mongo
import com.mongodb.BasicDBObject
import com.ncc.neon.query.InvalidQueryException
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause
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
class MongoWhereClauseBuilder {

    /** Maps an operator string to the mongo driver equivalent */
    private static final OPERATOR_MAPPING = ['<': '$lt', '<=': '$lte', '>': '$gt', '>=': '$gte', '!=': '$ne', 'in': '$in', 'notin': '$nin']

    private MongoWhereClauseBuilder() {
        // utility class, no public constructor needed
    }

    static def build(SingularWhereClause clause) {
        def rhs
        def rhsVal = clause.rhs
        if (clause.lhs == '_id') {
            rhsVal = createObjIds(rhsVal)
        }

        // no operator actually used for equals - it's just a simple key value pair
        if (clause.operator == '=') {
            rhs = rhsVal
        } else {
            def opString = OPERATOR_MAPPING[clause.operator]
            rhs = new BasicDBObject(opString, rhsVal)
        }

        return new BasicDBObject(clause.lhs, rhs)
    }

    static def build(WithinDistanceClause clause) {
        def geometry = new BasicDBObject("type", "Point")
        geometry.put("coordinates", [clause.center.lonDegrees, clause.center.latDegrees])

        def near = new BasicDBObject('$near', geometry)
        near.put('$maxDistance', clause.distance * clause.distanceUnit.meters)

        return new BasicDBObject(clause.locationField, near)
    }

    static def build(AndWhereClause clause) {
        return buildBooleanClause(clause, 'and')
    }

    static def build(OrWhereClause clause) {
        return buildBooleanClause(clause, 'or')
    }

    private static def buildBooleanClause(booleanClause, opName) {
        // TODO: NEON-422 Mongo has a bug such that it can't use geospatial clauses in AND or OR operators - https://jira.mongodb.org/browse/SERVER-4572. AND can be partially used by putting the geospatial clause outside the rest of them (as long as the location field is not used inside the AND query as well)
        def geospatialClauses = []
        def clauses = []

        booleanClause.whereClauses.each {
            def clause = build(it)
            if (!(it instanceof WithinDistanceClause)) {
                clauses << clause
            } else {
                geospatialClauses << clause
            }
        }
        def dbObject = new BasicDBObject('$' + opName, clauses)
        appendGeospatialClausesToBooleanClause(booleanClause, dbObject, geospatialClauses)
        return dbObject
    }

    private static def appendGeospatialClausesToBooleanClause(booleanClause, dbObject, geospatialClauses) {
        geospatialClauses.each {
            if (booleanClause instanceof OrWhereClause) {
                throw new InvalidQueryException('MongoDB does not support geospatial queries in OR operators. See https://jira.mongodb.org/browse/SERVER-4572 for details')
            }
            dbObject.putAll(it)
        }
    }

    private static def createObjIds(def rhs) {
        if (rhs instanceof Collection) {
            return MongoUtils.oidsToObjectIds(rhs)
        }
        MongoUtils.toObjectId(rhs)
    }
}
