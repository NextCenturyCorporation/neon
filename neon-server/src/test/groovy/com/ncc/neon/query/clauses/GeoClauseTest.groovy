package com.ncc.neon.query.clauses

import com.ncc.neon.util.LatLon
import org.junit.Test

class GeoClauseTest {
	@Test
	void testBuildPoint() {
		def latlon = new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d)
		def clause = new GeoClause()
		def point = clause.buildGeoJSONPoint(latlon)
		assert point == [19.5d, 11.95d]
	}
	
	@Test 
	void testBuildLine() {
		def latLonArray = new LatLon[2]
		latLonArray[0] = new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d)
		latLonArray[1] = new LatLon(latDegrees: 35.2d, lonDegrees: 50.1d)
		def clause = new GeoClause()
		def line = clause.buildGeoJSONLine(latLonArray)
		assert line == [[19.5d, 11.95d],[50.1d, 35.2d]]
	}
	
	@Test
	void testBuildArray() {
		def latLonArray = new LatLon[1][2]
		latLonArray[0][0] = new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d)
		latLonArray[0][1] = new LatLon(latDegrees: 35.2d, lonDegrees: 50.1d)
		def clause = new GeoClause()
		def line = clause.buildGeoJSONPointArray(latLonArray)
		assert line == [[[19.5d, 11.95d],[50.1d, 35.2d]]]
	}
}
