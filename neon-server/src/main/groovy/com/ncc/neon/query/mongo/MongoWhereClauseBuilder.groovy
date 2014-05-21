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
	
	static def build(GeoIntersectionClause clause) {
		def geoType
		if(clause.points.length == 1) {
			geoType = "Point"
		} else if(clause.points.length == 2) {
			geoType = "LineString"
		} else {
			geoType = "Polygon"
		}
		
		def geometry = new BasicDBObject("type","geoType")
		def coordinates = [];
		if(geoType == "Point") {
			coordinates = [clause.points[0].lonDegrees, clause.points[0].latDegrees];
		} else {
			clause.points.each {
				coordinates.add([it.lonDegrees, it.latDegrees]);
			}
		}
		
		def intersection = new BasicDBObject('$geoIntersects', geometry)
		
		return new BasicDBObject(clause.locationField, intersection)
	}
	
	static def build(GeoWithinClause clause) {
		//FIXME: The GeoWithinClause body
		//geoJSON polygon only
		
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
