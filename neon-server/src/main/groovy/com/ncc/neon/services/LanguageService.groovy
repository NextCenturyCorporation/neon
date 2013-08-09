package com.ncc.neon.services
import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.ConnectionState
import com.ncc.neon.connect.DataSource
import com.ncc.neon.language.QueryParser
import com.ncc.neon.query.Query
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
 * @author tbrooks
 */

@Component
@Path("/languageservice")
class LanguageService{

    @Autowired
    QueryParser queryParser

    @Autowired
    ConnectionState connectionState

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    String executeQuery(@FormParam("text") String text, @FormParam("datastore") String datastore){
        Query query = queryParser.parse(text)
        // TODO NEON-369 Hard coded for illustration, eventually this service will not require this
        ConnectionInfo connectionInfo
        if(datastore.toLowerCase().startsWith("mongo")){
            connectionInfo = new ConnectionInfo(dataStoreName: DataSource.MONGO, connectionUrl: "localhost")
        }
        else{
            connectionInfo = new ConnectionInfo(dataStoreName: DataSource.HIVE, connectionUrl: "xdata2")
        }
        connectionState.createConnection(connectionInfo)

        return wrapInDataJson(connectionState.queryExecutor.execute(query, false))
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datastores")
    List getDatastoreNames() {
        // TODO NEON-369 Hard coded for illustration, eventually this service will not require this
        return ["Mongo@localhost","JDBC(hive2)@xdata2"]
    }

    private String wrapInDataJson(queryResult) {
        def json = queryResult.toJson()

        '{"data":' + json + '}'
    }
}


