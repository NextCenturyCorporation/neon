package com.ncc.neon.query.jackson

import com.ncc.neon.query.clauses.SortOrder
import groovy.mock.interceptor.StubFor
import org.codehaus.jackson.JsonParser
import org.junit.Before
import org.junit.Test


class SortOrderDeserializerTest {


    def deserializer

    @Before
    void setUp() {
        deserializer = new SortOrderDeserializer()
    }

    @Test
    void "deserialize descending sort order"() {
        def parser = createJsonParser(-1)
        def sortOrder = deserializer.deserialize(parser, null)
        assert sortOrder == SortOrder.DESCENDING

    }

    @Test
    void "deserialize ascending sort order"() {
        def parser = createJsonParser(1)
        def sortOrder = deserializer.deserialize(parser, null)
        assert sortOrder == SortOrder.ASCENDING

    }
    private static def createJsonParser(direction) {
        def parserStub = new StubFor(JsonParser)
        parserStub.demand.getValueAsInt { direction }
        return parserStub.proxyInstance()
    }

}
