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

import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.state.executor.DerbyExecutor

class PropertyServiceTest {

    private PropertyService propertyService

    @Before
    void setup() {
        propertyService = new PropertyService()
    }

    @Test
    void "get empty list of property names"() {
        assert propertyService.propertyNames() == new HashSet()
    }

    @Test
    void "get property that doesn't exist"() {
        assert propertyService.getProperty("not there") == null
    }

    @Test
    void "set property"() {
        final String keyName = "The key"
        final String value = "Some value"
        propertyService.setProperty(keyName, value)
        assert propertyService.propertyNames() == new HashSet([keyName])
        assert propertyService.getProperty(keyName) == value
    }

    @Test
    void "set multiple properties"() {
        final String key1 = "first key"
        final String key2 = "Second"
        final String value1 = "simple"
        final String value2 = "A little longer & more complicated?"
        propertyService.setProperty(key1, value1)
        propertyService.setProperty(key2, value2)
        assert propertyService.propertyNames() == new HashSet([key1, key2])
        assert propertyService.getProperty(key1) == value1
        assert propertyService.getProperty(key2) == value2
    }

    @Test
    void "remove property"() {
        final String key1 = "first key"
        final String key2 = "Second"
        final String value1 = "simple"
        final String value2 = "A little longer & more complicated?"
        propertyService.setProperty(key1, value1)
        propertyService.setProperty(key2, value2)
        propertyService.remove(key1)
        assert propertyService.propertyNames() == new HashSet([key2])
        assert propertyService.getProperty(key1) == null
    }
}
