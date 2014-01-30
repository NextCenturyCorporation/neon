package com.ncc.neon.query.mongo
import com.mongodb.BasicDBObject
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause


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
        appendGeospatialClausesToBooleanClause(dbObject, geospatialClauses)
        return dbObject
    }

    private static def appendGeospatialClausesToBooleanClause(dbObject, geospatialClauses) {
        geospatialClauses.each {
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
