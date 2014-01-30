package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.connect.ConnectionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType



/**
 * Service creates connections to data sources such as mongo or hive.
 */

@Component
@Path("/connectionservice")
class ConnectionService {

    @Autowired
    ConnectionManager connectionManager

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hostnames")
    List<String> getHostnames() {
        return [System.getProperty("mongo.hosts", "localhost")]
    }

    /**
     * Attempt to establish a connection with a data source
     * @param datastore The name of the data source, e.g. mongo or hive
     * @param hostname the connection url for the data store, e.g. localhost
     */

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("connect")
    void connect(@FormParam("datastore") String datastore, @FormParam("hostname") String hostname) {
        ConnectionInfo info = new ConnectionInfo(dataSource: DataSources.valueOf(datastore.toLowerCase()), connectionUrl: hostname)
        connectionManager.connect(info)
    }


}
