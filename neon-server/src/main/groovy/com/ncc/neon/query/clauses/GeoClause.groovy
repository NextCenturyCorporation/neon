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
