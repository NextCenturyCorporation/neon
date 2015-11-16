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
        def parser = createJsonParser("-1")
        def sortOrder = deserializer.deserialize(parser, null)
        assert sortOrder == SortOrder.DESCENDING

    }

    @Test
    void "deserialize ascending sort order"() {
        def parser = createJsonParser("1")
        def sortOrder = deserializer.deserialize(parser, null)
        assert sortOrder == SortOrder.ASCENDING

    }
    private static def createJsonParser(direction) {
        def parserStub = new StubFor(JsonParser)
        parserStub.demand.getText { direction }
        return parserStub.proxyInstance()
    }

}
