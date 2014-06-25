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

import java.net.UnknownHostException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Provider
class UnknownHostExceptionMapper implements ExceptionMapper<UnknownHostException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnknownHostExceptionMapper)

    @Override
    public Response toResponse(UnknownHostException exception) {
        LOGGER.error("Requested host is unknown to the server. " + exception.getMessage(), exception)
    	return Response.status(502).entity("Requested host is unknown to the server. " + exception.getMessage()).type("text/plain").build()
    }

}