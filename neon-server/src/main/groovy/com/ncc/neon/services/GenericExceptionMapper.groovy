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

import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Provider
class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    javax.ws.rs.ext.Providers providers

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper)

    @Override
    public Response toResponse(Exception exception) {
        // Log any unhandled server exceptions and return an internal server error for them.
        LOGGER.error(exception.message, exception)

        return checkCausedBy(exception)
    }

    private checkCausedBy(Exception exception) {
        // Since we don't recognize the exception that was thrown, see if we recognize an exception
        // that caused it.
        if (exception instanceof UnknownHostException) {
            return providers.getExceptionMapper(UnknownHostException).toResponse(exception)
        }
        if (exception instanceof ConnectException) {
            return providers.getExceptionMapper(ConnectException).toResponse(exception)
        }
        if (exception.getCause()) {
            return checkCausedBy(exception.getCause())
        }
        ExceptionMapperResponse response = new ExceptionMapperResponse("Unknown Error", exception)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).type("application/json").build()
    }

}
