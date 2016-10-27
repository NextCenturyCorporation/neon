/*
 * Copyright 2016 Next Century Corporation
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
package com.ncc.neon.services

import org.junit.Before
import org.junit.Test

class PropertyServiceTest {

    private PropertyService propertyService

    @Before
    void setup() {
        propertyService = new PropertyService()
    }

    @Test
    void "get empty list of property names"() {
        assert propertyService.propertyNames().asList() == []
    }

    @Test
    void "get property that doesn't exist"() {
        assert propertyService.getProperty("not there") == [key: "not there", value: null]
    }

    @Test
    void "set property"() {
        final String KEY_NAME = "The key"
        final String VALUE = "Some value"
        propertyService.setProperty(KEY_NAME, VALUE)
        assert propertyService.propertyNames() == new HashSet([KEY_NAME])
        assert propertyService.getProperty(KEY_NAME) == [key: KEY_NAME, value: VALUE]
    }

    @Test
    void "set multiple properties"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        propertyService.setProperty(KEY_1, VALUE_1)
        propertyService.setProperty(KEY_2, VALUE_2)
        assert propertyService.propertyNames() == new HashSet([KEY_1, KEY_2])
        assert propertyService.getProperty(KEY_1) == [key: KEY_1, value: VALUE_1]
        assert propertyService.getProperty(KEY_2) == [key: KEY_2, value: VALUE_2]
    }

    @Test
    void "remove property"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        propertyService.setProperty(KEY_1, VALUE_1)
        propertyService.setProperty(KEY_2, VALUE_2)
        propertyService.remove(KEY_1)
        assert propertyService.propertyNames() == new HashSet([KEY_2])
        assert propertyService.getProperty(KEY_1) == [key: KEY_1, value: null]
    }
}
