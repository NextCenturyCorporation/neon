package com.ncc.neon.util

import groovy.transform.ToString

import static com.google.common.base.Preconditions.checkArgument

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
/**
 * A latitude and longitude pair
 */
@ToString(includeNames = true)
class LatLon {

    def latDegrees
    def lonDegrees

    /**
     * validation done on setter
     * @param latDegrees
     */
    void setLatDegrees(latDegrees) {
        checkArgument((-90d..90d).containsWithinBounds(latDegrees), "Latitude %s must be in range [-90,90]", latDegrees)
        this.latDegrees = latDegrees
    }

    /**
     * validation done on setter
     * @param latDegrees
     */
    void setLonDegrees(lonDegrees) {
        checkArgument((-180..180d).containsWithinBounds(lonDegrees), "Longitude %s must be in range [-180,180]", lonDegrees)
        this.lonDegrees = lonDegrees
    }

}
