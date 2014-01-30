package com.ncc.neon.query.jackson

import com.ncc.neon.query.clauses.SortOrder
import org.codehaus.jackson.JsonParser
import org.codehaus.jackson.map.DeserializationContext
import org.codehaus.jackson.map.JsonDeserializer


class SortOrderDeserializer extends JsonDeserializer<SortOrder> {

    @Override
    SortOrder deserialize(JsonParser jp, DeserializationContext ctxt) {
        return SortOrder.fromDirection(jp.valueAsInt)
    }
}
