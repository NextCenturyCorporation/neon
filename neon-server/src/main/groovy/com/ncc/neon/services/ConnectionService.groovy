package com.ncc.neon.services
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hostnames")
    List<String> getHostnames() {
        String mongo = System.getProperty("mongo.hosts", "localhost")
        String hive = System.getProperty("hive.host")

        def names = [mongo]
        if (hive) {
            names << hive
        }
        return names
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    ConnectionInfo getConnectionById(@PathParam("id") String id) {
        connectionManager.getConnectionById(id)
    }

    @DELETE
    @Path("{id}")
    void removeConnection(@PathParam("id") String id) {
        connectionManager.removeConnection(id)
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    String createConnection(ConnectionInfo info) {
        return connectionManager.connect(info)
    }

}
