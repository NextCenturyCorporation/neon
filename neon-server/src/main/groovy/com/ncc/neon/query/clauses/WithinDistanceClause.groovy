package com.ncc.neon.query.clauses

import com.ncc.neon.util.LatLon
import groovy.transform.ToString



/**
 * A query clause to use when checking if a record is within a certain geographic distance of a point
 */
@ToString(includeNames = true)
class WithinDistanceClause implements WhereClause {

    String locationField
    LatLon center
    double distance
    DistanceUnit distanceUnit

}
