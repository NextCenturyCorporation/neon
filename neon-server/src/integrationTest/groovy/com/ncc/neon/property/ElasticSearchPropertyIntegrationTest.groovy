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
import org.junit.Assume
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.annotation.Resource

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = IntegrationTestContext)
class ElasticSearchPropertyIntegrationTest {

    private static final String HOST_STRING = System.getProperty("elasticsearch.host")

    @Resource(name="elasticSearchProperty")
    private ElasticSearchProperty elasticSearchProperty

    @SuppressWarnings('JUnitPublicNonTestMethod')
    @Autowired
    public void setElasticSearchProperty(ElasticSearchProperty elasticSearchProperty) {
        this.elasticSearchProperty = elasticSearchProperty
    }

    @Before
    void before() {
        // Establish the connection, or skip the tests if no host was specified
        Assume.assumeTrue(HOST_STRING != null && HOST_STRING != "")
        elasticSearchProperty.propertiesDatabaseName = "properties"
        elasticSearchProperty.elasticSearchHost = HOST_STRING.split(":")[0]
        elasticSearchProperty.elasticSearchPort = HOST_STRING.split(":").length > 1 ?
            HOST_STRING.split(":")[1] : "9300"
        elasticSearchProperty.removeAll()
    }

    @Test
    void "get empty list of property names"() {
        assert elasticSearchProperty.propertyNames().asList() == []
    }

    @Test
    void "get property that doesn't exist"() {
        assert elasticSearchProperty.getProperty("not there") == [key: "not there", value: null]
    }

    @Test
    void "set property"() {
        final String KEY_NAME = "The key"
        final String VALUE = "Some value"
        elasticSearchProperty.setProperty(KEY_NAME, VALUE)
        assert elasticSearchProperty.propertyNames() == new HashSet([KEY_NAME])
        assert elasticSearchProperty.getProperty(KEY_NAME) == [key: KEY_NAME, value: VALUE]
    }

    @Test
    void "set multiple properties"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        elasticSearchProperty.setProperty(KEY_1, VALUE_1)
        elasticSearchProperty.setProperty(KEY_2, VALUE_2)
        assert elasticSearchProperty.propertyNames() == new HashSet([KEY_1, KEY_2])
        assert elasticSearchProperty.getProperty(KEY_1) == [key: KEY_1, value: VALUE_1]
        assert elasticSearchProperty.getProperty(KEY_2) == [key: KEY_2, value: VALUE_2]
    }

    @Test
    void "remove property"() {
        final String KEY_1 = "first key"
        final String KEY_2 = "Second"
        final String VALUE_1 = "simple"
        final String VALUE_2 = "A 'little' longer\n & more complicated?"
        elasticSearchProperty.setProperty(KEY_1, VALUE_1)
        elasticSearchProperty.setProperty(KEY_2, VALUE_2)
        elasticSearchProperty.remove(KEY_1)
        assert elasticSearchProperty.propertyNames() == new HashSet([KEY_2])
        assert elasticSearchProperty.getProperty(KEY_1) == [key: KEY_1, value: null]
    }
}

