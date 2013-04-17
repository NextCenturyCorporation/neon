package com.ncc.neon.query.jackson

import org.codehaus.jackson.JsonParser
import org.codehaus.jackson.JsonToken
import org.codehaus.jackson.map.DeserializationConfig
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.deser.StdDeserializationContext
import org.codehaus.jackson.map.introspect.VisibilityChecker
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test

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
class QueryValueDeserializerTest {

    def deserializer

    // the config/context are just populated with enough default values to allow them to be instantiated
    def config = new DeserializationConfig(ObjectMapper.DEFAULT_INTROSPECTOR, null, VisibilityChecker.Std.defaultInstance(), null, null, null, null)
    def context = new StdDeserializationContext(config, null, null, null)

    @Before
    void setUp() {
        deserializer = new QueryValueDeserializer()
    }

    @Test
    void "deserialize string"() {
        def parser = createJsonParser(JsonToken.VALUE_STRING, [getText: { "abc" }])
        def actual = deserializer.deserialize(parser, context)
        assert actual == "abc"
    }

    @Test
    void "deserialize date string with millis"() {
        def parser = createJsonParser(JsonToken.VALUE_STRING, [getText: { "2011-10-15T14:55:22.123Z" }])
        def actual = deserializer.deserialize(parser, context)
        def date = new DateTime(2011, 10, 15, 14, 55, 22, 123, DateTimeZone.UTC)
        assert actual == date.toDate()
    }

    @Test
    void "deserialize date string no millis"() {
        def parser = createJsonParser(JsonToken.VALUE_STRING, [getText: { "2011-10-15T14:55:22Z" }])
        def actual = deserializer.deserialize(parser, context)
        def date = new DateTime(2011, 10, 15, 14, 55, 22, DateTimeZone.UTC)
        assert actual == date.toDate()
    }

    @Test
    void "deserialize int"() {
        int val = 10
        def parser = createJsonParser(JsonToken.VALUE_NUMBER_INT, [getNumberValue: { val }])
        def actual = deserializer.deserialize(parser, context)
        assert actual == val
    }

    @Test
    void "deserialize double"() {
        double val = 10.3
        def parser = createJsonParser(JsonToken.VALUE_NUMBER_FLOAT, [getDoubleValue: { val }])
        def actual = deserializer.deserialize(parser, context)
        assert actual == val
    }

    private static def createJsonParser(currentToken, additionalMethods = [:]) {
        def mock = [:]
        mock['getCurrentToken'] = { currentToken }
        mock.putAll(additionalMethods)
        return mock as JsonParser
    }

}
