package com.ncc.neon.services
import com.ncc.neon.config.field.ColumnMapping
import com.ncc.neon.config.field.FieldConfigurationMapping
import com.ncc.neon.config.field.WidgetDataSet
import com.ncc.neon.query.*
import com.ncc.neon.query.filter.DataSet
import com.ncc.neon.result.AssembleClientData
import com.ncc.neon.result.FieldNames
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
    FieldConfigurationMapping configurationMapping

    @Autowired
    MetadataResolver metadataResolver

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    String executeQuery(Query query,
                        @DefaultValue("false") @QueryParam("includefiltered") boolean includeFiltered,
                        @QueryParam("transform") String transformClassName,
                        @QueryParam("param") List<String> transformParams) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        QueryResult queryResult = queryExecutor.execute(query, includeFiltered)
        def columns = metadataResolver.resolveQuery(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, metadataObject: columns,
                transformClassName: transformClassName, transformParams: transformParams)
        return assembler.createClientData()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup")
    String executeQueryGroup(QueryGroup query,
                             @DefaultValue("false") @QueryParam("includefiltered") boolean includeFiltered,
                             @QueryParam("transform") String transformClassName,
                             @QueryParam("param") List<String> transformParams) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        QueryResult queryResult = queryExecutor.execute(query, includeFiltered)
        def columns = metadataResolver.resolveQueryGroup(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, metadataObject: columns,
                transformClassName: transformClassName, transformParams: transformParams)
        return assembler.createClientData()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("databasenames")
    List<String> getDatabaseNames() {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.showDatabases()
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tablenames")
    List<String> getTableNames(@FormParam("database") String database) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.showTables(database)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fieldnames")
    FieldNames getFieldNames(@QueryParam("databaseName") String databaseName,
                             @QueryParam("tableName") String tableName,
                             @QueryParam("widgetName") String widgetName) {

        QueryExecutor queryExecutor = queryExecutorFactory.create()
        ColumnMapping mapping = getColumnMapping(databaseName, tableName, widgetName)
        def fieldNames = queryExecutor.getFieldNames(databaseName, tableName)
        return new FieldNames(fieldNames: fieldNames, metadata: mapping)
    }

    private ColumnMapping getColumnMapping(String databaseName, String tableName, String widgetName) {
        if (widgetName) {
            DataSet dataset = new DataSet(databaseName: databaseName, tableName: tableName)
            return configurationMapping.get(new WidgetDataSet(dataSet: dataset, widgetName: widgetName))
        }
        return new ColumnMapping()
    }

}


