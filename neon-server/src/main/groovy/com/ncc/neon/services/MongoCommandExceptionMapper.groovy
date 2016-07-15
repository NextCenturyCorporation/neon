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

import com.mongodb.MongoCommandException

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Provider
class MongoCommandExceptionMapper implements ExceptionMapper<MongoCommandException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCommandExceptionMapper)
    private static final Map<Integer, String> CODE_TO_MESSAGE = [
        16389: "Query execution failed because there was too much data.",
        16945: "Query execution failed because there was too much data."
    ]

    @Override
    public Response toResponse(MongoCommandException exception) {
        LOGGER.error(exception.message, exception)
        final String MESSAGE = CODE_TO_MESSAGE[exception.code] ?: "Query execution failed due to an mongodb unknown error."
        ExceptionMapperResponse response = new ExceptionMapperResponse(MESSAGE, exception)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type("application/json").build()
    }
}
