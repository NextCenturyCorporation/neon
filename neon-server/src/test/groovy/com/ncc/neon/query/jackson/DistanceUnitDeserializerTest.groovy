package com.ncc.neon.query.jackson

import com.ncc.neon.query.clauses.DistanceUnit
import groovy.mock.interceptor.StubFor
import org.codehaus.jackson.JsonParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters


@RunWith(Parameterized)
class DistanceUnitDeserializerTest {


    def deserializer
    def distanceUnit
    def expectedEnum


    @Before
    void setUp() {
        deserializer = new DistanceUnitDeserializer()
    }

    DistanceUnitDeserializerTest(distanceUnit, expectedEnum) {
        this.distanceUnit = distanceUnit
        this.expectedEnum = expectedEnum
    }

    @Test
    void "deserialize distance units"() {
        def parser = createJsonParser()
        def actualEnum = deserializer.deserialize(parser, null)
        assert actualEnum == expectedEnum
    }

    @Parameters
    static Collection<Object[]> data() {
        return [
                ["mile", DistanceUnit.MILE] as Object[],
                ["meter", DistanceUnit.METER] as Object[],
                ["km", DistanceUnit.KM] as Object[]
        ]
    }

    private def createJsonParser() {
        def parserStub = new StubFor(JsonParser)
        parserStub.demand.getText { distanceUnit }
        return parserStub.proxyInstance()
    }

}
