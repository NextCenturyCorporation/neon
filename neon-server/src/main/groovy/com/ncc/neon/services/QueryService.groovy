package com.ncc.neon.services
import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryResult
import com.ncc.neon.result.AssembleClientData
import com.ncc.neon.result.ClientData
import com.ncc.neon.result.InitializingClientData
import com.ncc.neon.result.MetadataResolver
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
 */

@Component
@Path("/queryservice")
class QueryService {

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    @Autowired
    MetadataResolver metadataResolver

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    ClientData executeQuery(Query query) {
        return execute(query)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup")
    ClientData executeQueryGroup(QueryGroup query) {
        return execute(query)
    }

    private ClientData execute(def query) {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        QueryResult queryResult = queryExecutor.execute(query)
        ColumnMetadataList columnMetadataList = metadataResolver.resolveQuery(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, columnMetadataList: columnMetadataList)
        return assembler.createClientData()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querydisregardfilters")
    ClientData executeQueryDisregardFilters(Query query) {
        return executeDisregardFilters(query)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroupdisregardfilters")
    ClientData executeQueryGroupDisregardFilters(QueryGroup query) {
        return executeDisregardFilters(query)
    }

    private ClientData executeDisregardFilters(def query) {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        QueryResult queryResult = queryExecutor.executeDisregardingFilters(query)
        ColumnMetadataList columnMetadataList = metadataResolver.resolveQuery(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, columnMetadataList: columnMetadataList)
        return assembler.createClientData()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fields")
    InitializingClientData getFields(@QueryParam("databaseName") String databaseName,
                                     @QueryParam("tableName") String tableName,
                                     @QueryParam("widgetName") String widgetName) {

        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        QueryResult queryResult = queryExecutor.getFieldNames(databaseName, tableName)
        WidgetAndDatasetMetadataList metadata = metadataResolver.getInitializationData(databaseName, tableName, widgetName)
        ColumnMetadataList columnMetadataList = metadataResolver.resolveQuery(databaseName, tableName)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, columnMetadataList: columnMetadataList, initDataList: metadata)
        return assembler.createClientData()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("databasenames")
    List<String> getDatabaseNames() {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        return queryExecutor.showDatabases()
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tablenames")
    List<String> getTableNames(@FormParam("database") String database) {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        return queryExecutor.showTables(database)
    }
}


