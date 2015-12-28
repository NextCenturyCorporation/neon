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

package com.ncc.neon.query.jackson

import com.ncc.neon.util.DateUtils
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer


/**
 * A deserializer for json values for the right hand side of a query
 */
class QueryValueDeserializer extends JsonDeserializer<Object> {

    private static final def DEFAULT_SERIALIZER = new UntypedObjectDeserializer()

    /**
     * Can accept a json value of a primitive type or a json object containing a 'value' key value pair and optionally
     * a 'type' key value pair. The type acts as an override, enforcing the attempted parsing of known types (e.g. date)
     */
    @Override
    Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        switch (jp.currentToken) {
            case JsonToken.VALUE_STRING:
                // check if the string is a date, if not, fallback to plain string
                if(jp.text ==~ /\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d(?:\.\d\d\d)?Z/) {
                    return  DateUtils.tryToParseDate(jp.text)
                }
                return DEFAULT_SERIALIZER.deserialize(jp, ctxt)
            case JsonToken.START_OBJECT:
                return processRHSObject(jp)
            default:
                return DEFAULT_SERIALIZER.deserialize(jp, ctxt)
        }
    }

    Object processRHSObject(JsonParser jp) {
        def rhsValue
        def type

        jp.nextToken()
        def key = jp.getText()
        jp.nextToken()
        def value = jp.getText()

        if(key == "type") {
            type = value
        } else if (key == "value") {
            rhsValue = value
        }

        if(jp.nextToken() == JsonToken.FIELD_NAME) {
            key = jp.getText()
            jp.nextToken()
            value = jp.getText()

            if(key == "type") {
                type = value
            } else if (key == "value") {
                rhsValue = value
            }
        }
        return tryToParseObject(type, rhsValue)
    }

    Object tryToParseObject(String type, String value) {
        if(type && type.equalsIgnoreCase("date") || !type && value ==~ /\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d(?:\.\d\d\d)?Z/) {
            return DateUtils.tryToParseDate(value)
        }

        return value
    }
}
