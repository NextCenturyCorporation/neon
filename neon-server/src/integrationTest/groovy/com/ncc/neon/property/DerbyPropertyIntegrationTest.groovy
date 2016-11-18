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
package com.ncc.neon.property

import com.ncc.neon.IntegrationTestContext

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.annotation.Resource

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class DerbyPropertyIntegrationTest {

    @Resource(name="derbyProperty")
    private DerbyProperty derbyProperty

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setDerbyProperty(DerbyProperty derbyProperty) {
        this.derbyProperty = derbyProperty
    }

    @Before
    void before() {
        derbyProperty.propertiesDatabaseName = "properties"
        derbyProperty.derbyClient = "false"
        derbyProperty.derbyClientHost = "localhost"
        derbyProperty.derbyClientPort = "1527"
        derbyProperty.removeAll()
    }

    @Test
    void "get empty list of property names"() {
        assert derbyProperty.propertyNames().asList() == []
    }

    @Test
    void "get property that doesn't exist"() {
        assert derbyProperty.getProperty("not there") == [key: "not there", value: null]
    }

    @Test
    void "set property"() {
        final String KEY_NAME = "The key"
        final String VALUE = "Some value"
        derbyProperty.setProperty(KEY_NAME, VALUE)
        assert derbyProperty.propertyNames() == new HashSet([KEY_NAME])
        assert derbyProperty.getProperty(KEY_NAME) == [key: KEY_NAME, value: VALUE]
    }

    @Test
    void "set multiple properties"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        derbyProperty.setProperty(KEY_1, VALUE_1)
        derbyProperty.setProperty(KEY_2, VALUE_2)
        assert derbyProperty.propertyNames() == new HashSet([KEY_1, KEY_2])
        assert derbyProperty.getProperty(KEY_1) == [key: KEY_1, value: VALUE_1]
        assert derbyProperty.getProperty(KEY_2) == [key: KEY_2, value: VALUE_2]
    }

    @Test
    void "remove property"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        derbyProperty.setProperty(KEY_1, VALUE_1)
        derbyProperty.setProperty(KEY_2, VALUE_2)
        derbyProperty.remove(KEY_1)
        assert derbyProperty.propertyNames() == new HashSet([KEY_2])
        assert derbyProperty.getProperty(KEY_1) == [key: KEY_1, value: null]
    }
}

