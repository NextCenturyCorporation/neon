/*
 * Copyright 2013 Next Century Corporation
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

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter

/**
 * Enables Cross Origin Resource Sharing (CORS)
 */

class CorsFilter implements ContainerResponseFilter {
    @Override
    void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*")
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")

        String reqHead = requestContext.getHeaders.getFirst("Access-Control-Request-Headers")

        if(reqHead != null) {
            responseContext.getHeaders.add("Access-Control-Allow-Headers", reqHead)
        }
    }
}

/* Old, from jersey v1.X
class CorsFilter implements ContainerResponseFilter{

    @Override
    ContainerResponse filter(ContainerRequest request, ContainerResponse response){
        Response.ResponseBuilder builder = Response.fromResponse(response.response)
        builder.header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")

        String reqHead = request.getHeaderValue("Access-Control-Request-Headers")

        if (reqHead != null){
            builder.header("Access-Control-Allow-Headers", reqHead)
        }

        response.setResponse(builder.build())
        return response
    }
}
*/