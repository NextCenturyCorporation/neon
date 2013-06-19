package com.ncc.neon.services
import com.ncc.neon.connect.Connection
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.HiveConnection
import com.ncc.neon.connect.MongoConnection
import com.ncc.neon.database.DatabaseTableSelector
import com.ncc.neon.database.SelectorFactory
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
 * @author tbrooks
 */

@Component
@Path("/filterservice")
class FilterService{

    private final SelectorFactory selectorFactory = new SelectorFactory()
    private def connectionClient

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hostnames")
    List<String> getHostnames() {
        return ["localhost", "xdata2"]
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("connect")
    String connect(@FormParam("datastore") String datastore, @FormParam("hostname") String hostname){
        ConnectionInfo connectionInfo = new ConnectionInfo(connectionUrl: hostname)
        if(datastore == "mongo"){
            Connection connection = new MongoConnection()
            connectionClient = connection.connect(connectionInfo)
        }
        if(datastore == "hive"){
            Connection connection = new HiveConnection()
            connectionClient = connection.connect(connectionInfo)
        }

        return '{ "success": true }'
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("databaseNames")
    List<String> getDatabaseNames() {
        DatabaseTableSelector selector = selectorFactory.create(connectionClient)
        selector.showDatabases()
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tableNames")
    List<String> getTableNames(@FormParam("database") String database) {
        DatabaseTableSelector selector = selectorFactory.create(connectionClient)
        selector.showTables(database)
    }

}
