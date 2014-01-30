
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