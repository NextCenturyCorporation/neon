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

import com.ncc.neon.util.ResourceNotFoundException

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Provider
class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceNotFoundExceptionMapper)

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        LOGGER.error(exception.message, exception)
        ExceptionMapperResponse response = new ExceptionMapperResponse(exception.message, exception)
        return Response.status(Response.Status.NOT_FOUND).entity(response).type("application/json").build()
    }

}

