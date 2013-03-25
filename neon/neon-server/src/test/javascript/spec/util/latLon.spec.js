/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
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
    };

    /**
     * Wraps the latitude/longitude creation in anonymous function, which is needed for exception testing
     * @param latDegrees
     * @param lonDegrees
     */
    function latLonFunc(latDegrees,lonDegrees) {
        return function() {
            return new neon.util.LatLon(latDegrees,lonDegrees);
        };
    };
});