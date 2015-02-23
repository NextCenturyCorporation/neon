package com.ncc.neon.query.clauses

import com.ncc.neon.util.LatLon

class GeoClause implements WhereClause{
    def buildGeoJSONPoint(LatLon point) {
        return [point.lonDegrees, point.latDegrees]
    }
    
    def buildGeoJSONPointArray(LatLon[][] points) {
        def coordinates = []
        points.each {
            coordinates.add(buildGeoJSONLine(it))
        }
        return coordinates
    }
    
    def buildGeoJSONLine(LatLon[] pointArray) {
        def points = []
        pointArray.each {
            points.add(buildGeoJSONPoint(it))
        }
        return points
    }
}
