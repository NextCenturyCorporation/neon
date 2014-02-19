/*
 * Copyright 2014 Next Century Corporation
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
package com.ncc.neon.state

import com.ncc.neon.StateIdGeneratorIntegrationTestContext
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.context.WebApplicationContext

/**
 * A test that verifies that the stateId generator is configured to give different ids per session
 */
@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(classes = StateIdGeneratorIntegrationTestContext)
class StateIdGeneratorIntegrationTest {

    @Autowired
    private StateIdGenerator stateIdGenerator

    @Test
    void "id generator is session scoped"() {
        assert stateIdGenerator != null
        def scopeAnnotation = StateIdGenerator.getAnnotation(Scope)
        assert scopeAnnotation.value() == WebApplicationContext.SCOPE_SESSION
    }


}
