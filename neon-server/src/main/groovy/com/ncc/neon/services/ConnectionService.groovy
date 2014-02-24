
package com.ncc.neon.services

import com.ncc.neon.NeonProperties
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
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


/**
 * Service creates connections to data sources such as mongo or hive.
 */

@Component
@Path("/connections")
class ConnectionService {

    @Autowired
    ConnectionManager connectionManager

    /**
     * Get the host names of databases.
     * @return The host names
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hostnames")
    List<String> getHostnames() {
        return NeonProperties.instance.hostnames
    }

    /**
     * Gets the set of available connection resources
     * @return A set of collection ids.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("ids")
    Set<String> getAllConnectionIds() {
        connectionManager.allConnectionIds
    }

    /**
     * Gets a connection resource if it exists.
     * @param id The identifier for the connection
     * @return The connection info, or null if it does not exist.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    ConnectionInfo getConnectionById(@PathParam("id") String id) {
        connectionManager.getConnectionById(id)
    }

    /**
     * Removes a connection resource if it exists.
     * @param id The identifer for the connection
     */
    @DELETE
    @Path("{id}")
    void removeConnection(@PathParam("id") String id) {
        connectionManager.removeConnection(id)
    }

    /**
     * Create a connection resource.
     * @param info The information needed to establish a connection
     * @return The connection identifier
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    String createConnection(ConnectionInfo info) {
        return connectionManager.connect(info)
    }

}
