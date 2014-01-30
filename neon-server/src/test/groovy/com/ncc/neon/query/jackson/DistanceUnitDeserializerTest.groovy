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
