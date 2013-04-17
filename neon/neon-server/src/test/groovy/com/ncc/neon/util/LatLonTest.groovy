package com.ncc.neon.util

import org.junit.Test

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
