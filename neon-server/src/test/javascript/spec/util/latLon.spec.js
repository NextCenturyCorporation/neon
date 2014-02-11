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


describe('latitude/longitude pair', function () {

    it('accepts valid latitude/longitude pairs', function () {
        assertLatLon(new neon.util.LatLon(25.2, -125), 25.2, -125);
        assertLatLon(new neon.util.LatLon(-90, -180), -90, -180);
        assertLatLon(new neon.util.LatLon(90, 180), 90, 180);
    });

    it('throws an error with an invalid latitude value', function() {
        expect(latLonFunc(-125,25)).toThrow();
        expect(latLonFunc(110,25)).toThrow();
    });

    it('throws an error with an invalid longitude value', function() {
        expect(latLonFunc(40,195)).toThrow();
        expect(latLonFunc(40,-181)).toThrow();
    });

    function assertLatLon(actual, expectedLatDegrees, expectedLonDegrees) {
        expect(actual.latDegrees).toEqual(expectedLatDegrees);
        expect(actual.lonDegrees).toEqual(expectedLonDegrees);
    }

    /**
     * Wraps the latitude/longitude creation in anonymous function, which is needed for exception testing
     * @param latDegrees
     * @param lonDegrees
     */
    function latLonFunc(latDegrees,lonDegrees) {
        return function() {
            return new neon.util.LatLon(latDegrees,lonDegrees);
        };
    }
});