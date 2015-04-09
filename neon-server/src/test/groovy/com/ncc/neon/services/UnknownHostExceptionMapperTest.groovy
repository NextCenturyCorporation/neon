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
package com.ncc.neon.services

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.junit.Before
import org.junit.Test

class UnknownHostExceptionMapperTest {
    private UnknownHostException testException
    private UnknownHostExceptionMapper mapper

    @Before
    void before() {
        testException = new UnknownHostException("sampleServer")
        mapper = new UnknownHostExceptionMapper()
    }

    @Test
    void "map exception to response"() {
        Response response = mapper.toResponse(testException)
        assert response.getStatus() == 502
        assert (response.getEntity() instanceof ExceptionMapperResponse)
        assert ((ExceptionMapperResponse) response.getEntity()).getError() == 'Requested host is unknown to the server.'
        assert ((ExceptionMapperResponse) response.getEntity()).getMessage() == 'sampleServer'
        assert response.getMetadata().getFirst('Content-Type') == MediaType.APPLICATION_JSON_TYPE
    }

}
