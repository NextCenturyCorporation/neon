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
 * A structure that holds a latitude and longitude
 * @namespace neon.util
 * @class LatLon
 */

/**
 * Creates a new latitude/longitude pair with the specified values in degrees
 * @param latDegrees
 * @param lonDegrees
 * @constructor
 */
neon.util.LatLon = function (latDegrees, lonDegrees) {
    neon.util.LatLon.validateArgs_(latDegrees, lonDegrees);

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

neon.util.LatLon.validateArgs_ = function (latDegrees, lonDegrees) {
    if (latDegrees > 90 || latDegrees < -90) {
        throw new Error('Invalid latitude ' + latDegrees + '. Must be in range [-90,90]');
    }

    if (lonDegrees > 180 || lonDegrees < -180) {
        throw new Error('Invalid longitude ' + lonDegrees + '. Must be in range [-180,180]');
    }
};