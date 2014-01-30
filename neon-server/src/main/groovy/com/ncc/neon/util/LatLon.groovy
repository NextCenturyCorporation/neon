package com.ncc.neon.util

import groovy.transform.ToString

import static com.google.common.base.Preconditions.checkArgument


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
