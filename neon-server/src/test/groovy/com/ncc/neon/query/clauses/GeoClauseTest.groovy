/*
 * Copyright 2016 Next Century Corporation
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
        assert line == [[19.5d, 11.95d], [50.1d, 35.2d]]
    }

    @Test
    void testBuildArray() {
        def latLonArray = new LatLon[1][2]
        latLonArray[0][0] = new LatLon(latDegrees: 11.95d, lonDegrees: 19.5d)
        latLonArray[0][1] = new LatLon(latDegrees: 35.2d, lonDegrees: 50.1d)
        def clause = new GeoClause()
        def line = clause.buildGeoJSONPointArray(latLonArray)
        assert line == [[[19.5d, 11.95d], [50.1d, 35.2d]]]
    }
}
