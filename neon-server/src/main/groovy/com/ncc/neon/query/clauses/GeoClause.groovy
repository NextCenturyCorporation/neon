package com.ncc.neon.query.clauses

import com.ncc.neon.util.LatLon

class GeoClause implements WhereClause{
	def buildGeoJSONPoint(LatLon point) {
		return [point.lonDegrees, point.latDegrees]
	}
	
	def buildGeoJSONLine(LatLon[][] points) {
		def coordinates = []
		points.each {
			coordinates.add(buildGeoJSONPointArray(it))
		}
		return coordinates
	}
	
	def buildGeoJSONPointArray(LatLon[] pointArray) {
		def points = []
		pointArray.each {
			points.add([it.lonDegrees, it.latDegrees])
		}
		return points
	}
}
