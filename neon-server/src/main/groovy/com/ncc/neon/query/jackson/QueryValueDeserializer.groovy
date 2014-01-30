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
