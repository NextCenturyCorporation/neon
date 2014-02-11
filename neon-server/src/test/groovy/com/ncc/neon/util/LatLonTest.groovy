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

package com.ncc.neon.util

import org.junit.Test



class LatLonTest {

    @Test
    void "valid lat/lon pairs"() {
        assertLatLon(latLon(25.2, -125), 25.2, -125)
        assertLatLon(latLon(-90, -180), -90, -180)
        assertLatLon(latLon(90, 180), 90, 180)
    }

    @Test(expected = IllegalArgumentException)
    void "latitude greater than max throws exception"() {
        latLon(91, -10)
    }

    @Test(expected = IllegalArgumentException)
    void "latitude less than min throws exception"() {
        latLon(-91, -10)
    }

    @Test(expected = IllegalArgumentException)
    void "longitude greater than max throws exception"() {
        latLon(25, 181)
    }

    @Test(expected = IllegalArgumentException)
    void "longitude less than min throws exception"() {
        latLon(25, -181)
    }

    private static def latLon(lat, lon) {
        return new LatLon(latDegrees: lat, lonDegrees: lon)
    }

    private static def assertLatLon(latLon, expectedLatDegrees, expectedLonDegrees) {
        assert latLon.latDegrees == expectedLatDegrees
        assert latLon.lonDegrees == expectedLonDegrees
    }

}
