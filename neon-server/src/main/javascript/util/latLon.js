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

/**
 * Creates a new latitude/longitude pair with the specified values in degrees
 * @class neon.util.LatLon
 * @constructor
 * @param {double} latDegrees
 * @param {double} lonDegrees

 */
neon.util.LatLon = function(latDegrees, lonDegrees) {
    this.validateArgs_(latDegrees, lonDegrees);

    /**
     * The latitude in degrees
     * @property latDegrees
     * @type {double}
     */
    this.latDegrees = latDegrees;

    /**
     * The longitude in degrees
     * @property lonDegrees
     * @type {double}
     */
    this.lonDegrees = lonDegrees;
};

neon.util.LatLon.prototype.validateArgs_ = function(latDegrees, lonDegrees) {
    if(latDegrees > 90 || latDegrees < -90) {
        throw new Error('Invalid latitude ' + latDegrees + '. Must be in range [-90,90]');
    }

    if(lonDegrees > 180 || lonDegrees < -180) {
        throw new Error('Invalid longitude ' + lonDegrees + '. Must be in range [-180,180]');
    }
};
