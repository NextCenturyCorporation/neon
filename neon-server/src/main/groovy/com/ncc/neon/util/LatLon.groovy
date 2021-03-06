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
