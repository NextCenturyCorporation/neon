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
import com.mongodb.BasicDBObject
import com.ncc.neon.query.clauses.AndWhereClause
import com.ncc.neon.query.clauses.OrWhereClause
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.clauses.WithinDistanceClause
import com.ncc.neon.query.clauses.GeoWithinClause
import com.ncc.neon.query.clauses.GeoIntersectionClause


/**
 * Creates a WHERE clause for mongodb
 */
class MongoWhereClauseBuilder {

    /** Maps an operator string to the mongo driver equivalent */
    private static final OPERATOR_MAPPING = ['<': '$lt', '<=': '$lte', '>': '$gt', '>=': '$gte', '!=': '$ne',
            'in': '$in', 'notin': '$nin', 'contains': '$regex']

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
        def geometryDefinition = new BasicDBObject("type", "Point")
        geometryDefinition.put("coordinates", clause.buildGeoJSONPoint(clause.center))

        def nearDefinition = new BasicDBObject('$geometry', geometryDefinition)
        nearDefinition.put('$maxDistance', clause.distance * clause.distanceUnit.meters)
        def near = new BasicDBObject('$near', nearDefinition)
        return new BasicDBObject(clause.locationField, near)
    }

    static def build(GeoIntersectionClause clause) {
        def geoType = determineGeometryType(clause)
        def geometryDefinition = new BasicDBObject("type", geoType)

        def coordinates
        if(geoType == "Point") {
            coordinates = clause.buildGeoJSONPoint(clause.points[0][0])
        } else if(geoType == "LineString") {
            coordinates = clause.buildGeoJSONLine(clause.points[0])
        } else {
            coordinates = clause.buildGeoJSONPointArray(clause.points)
        }
        geometryDefinition.put("coordinates", coordinates)

        def geometryBlock = new BasicDBObject('$geometry', geometryDefinition)
        def intersection = new BasicDBObject('$geoIntersects', geometryBlock)
        return new BasicDBObject(clause.locationField, intersection)
    }

    static def determineGeometryType(clause) {
        if(clause.geometryType == "Point") {
            return "Point"
        } else if(clause.geometryType == "Line") {
            return "LineString"
        }
        return "Polygon"
    }

    static def build(GeoWithinClause clause) {
        def geometryDefinition = new BasicDBObject("type", "Polygon")

        geometryDefinition.put("coordinates", clause.buildGeoJSONPointArray(clause.points))

        def geometryBlock = new BasicDBObject('$geometry', geometryDefinition)
        def within = new BasicDBObject('$geoWithin', geometryBlock)
        return new BasicDBObject(clause.locationField, within)
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
