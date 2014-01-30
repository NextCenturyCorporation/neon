package com.ncc.neon.query.jackson

import com.ncc.neon.query.clauses.DistanceUnit
import org.codehaus.jackson.JsonParser
import org.codehaus.jackson.map.DeserializationContext
import org.codehaus.jackson.map.JsonDeserializer
import org.codehaus.jackson.map.annotate.JsonDeserialize


class DistanceUnitDeserializer extends JsonDeserializer<DistanceUnit> {

    @Override
    DistanceUnit deserialize(JsonParser jp, DeserializationContext ctxt) {
        def unit = jp.text
        return DistanceUnit.valueOf(unit.toUpperCase(Locale.US))
    }
}
