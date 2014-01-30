package com.ncc.neon.services

import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter

import javax.ws.rs.core.Response



/**
 * Enables Cross Origin Resource Sharing (CORS)
 */

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
