package com.ncc.neon.query.jackson

import com.ncc.neon.util.DateUtils
import org.codehaus.jackson.JsonParser
import org.codehaus.jackson.JsonProcessingException
import org.codehaus.jackson.JsonToken
import org.codehaus.jackson.map.DeserializationContext
import org.codehaus.jackson.map.JsonDeserializer
import org.codehaus.jackson.map.deser.std.UntypedObjectDeserializer


/**
 * A deserializer for json values for the right hand side of a query
 */
class QueryValueDeserializer extends JsonDeserializer<Object> {

    private static final def DEFAULT_SERIALIZER = new UntypedObjectDeserializer()

    @Override
    Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        switch (jp.currentToken) {
            case JsonToken.VALUE_STRING:
                // check if the string is a date, if not, fallback to plain string
                return  DateUtils.tryToParseDate(jp.text)
            default:
                return DEFAULT_SERIALIZER.deserialize(jp, ctxt)
        }
    }
}
