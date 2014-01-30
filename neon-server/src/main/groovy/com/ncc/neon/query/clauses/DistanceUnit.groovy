package com.ncc.neon.query.clauses

import com.ncc.neon.query.jackson.DistanceUnitDeserializer
import org.codehaus.jackson.map.annotate.JsonDeserialize



/**
 * A representation of a distance
 */
@JsonDeserialize(using = DistanceUnitDeserializer)
public enum DistanceUnit {


    METER(1), KM(1000), MILE(1609.34)


    /** the distance unit in meters */
    def meters

    DistanceUnit(meters) {
        this.meters = meters
    }

}