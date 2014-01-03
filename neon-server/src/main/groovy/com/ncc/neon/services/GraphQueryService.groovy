/*
 * ***********************************************************************
 * Copyright (c), ${YEAR} Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property. Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * @author dflynt
 */

package com.ncc.neon.services

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.*
import com.ncc.neon.result.AssembleClientData
import com.ncc.neon.result.ClientData
import com.ncc.neon.result.InitializingClientData
import com.ncc.neon.result.MetadataResolver
import com.nextcentury.exporterjava.ExporterJava
import com.nextcentury.importerjava.ImporterBuilderJava
import org.gephi.graph.api.GraphController
import org.gephi.graph.api.GraphModel
import org.gephi.io.importer.api.Container
import org.gephi.io.importer.api.EdgeDefault
import org.gephi.io.importer.api.ImportController
import org.gephi.io.importer.spi.SpigotImporter
import org.gephi.io.processor.plugin.DefaultProcessor
import org.gephi.layout.plugin.force.StepDisplacement
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout
import org.gephi.project.api.ProjectController
import org.gephi.project.api.Workspace
import org.openide.util.Lookup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Service for executing queries against an arbitrary data store.
 */

@Component
@Path("/graphqueryservice")
class GraphQueryService {

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    @Autowired
    MetadataResolver metadataResolver

    /**
     * Executes a query against the data source the user is currently connected to.
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param query The neon representation of a query. This query is converted to a database specific query language.
     * @return An object that contains the query result data and optional metadata about the query.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    ClientData executeQuery(Query query) {
        return execute(query, QueryOptions.FILTERED_DATA)
    }

    /**
     * Executes a group of queries against the data source the user is currently connected to.
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param query A collection of queries. The results of the queries will be appended together.
     * @return An object that contains the query result data and optional metadata about the query.
     */
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("querygroup")
//    ClientData executeQueryGroup(QueryGroup query) {
//        return execute(query, QueryOptions.FILTERED_DATA)
//    }

    /**
     * Executes a query for selected items against the data source the user is currently connected to.
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param query The neon representation of a query. This query is converted to a database specific query language.
     * @return An object that contains the query result data and optional metadata about the query.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querywithselectiononly")
    ClientData executeQueryWithSelectionOnly(Query query) {
        return execute(query, QueryOptions.FILTERED_AND_SELECTED_DATA)
    }

    /**
     * Executes a group of queries for selected items against the data source the user is currently connected to.
     * This takes into account the user's current filters so the results will be limited by these if they exist.
     * @param query A collection of queries. The results of the queries will be appended together.
     * @return An object that contains the query result data and optional metadata about the query.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroupwithselectiononly")
    ClientData executeQueryGroupWithSelectionOnly(QueryGroup query) {
        return execute(query, QueryOptions.FILTERED_AND_SELECTED_DATA)
    }


    /**
     * Executes a query against the data source the user is currently connected to ignoring the current filters and selection.
     * @param query The neon representation of a query
     * @return An object that contains the query result data and optional metadata about the query.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querydisregardfilters")
    ClientData executeQueryDisregardFilters(Query query) {
        return execute(query, QueryOptions.ALL_DATA)
    }

    /**
     * Executes a group of queries against the data source the user is currently connected to ignoring the current filters and selection.
     * @param query The neon representation of a query
     * @return An object that contains the query result data and optional metadata about the query.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroupdisregardfilters")
    ClientData executeQueryGroupDisregardFilters(QueryGroup query) {
        return execute(query, QueryOptions.ALL_DATA)
    }

    /**
     * Get all the columns for tabular datasets.
     * @param databaseName The database containing the data
     * @param tableName The table containing the data
     * @param widgetName The current widget name. If used, additional metadata may be passed down.
     * @return An object that contains all the column names and metadata about the columns.
     * Metadata includes element id -> field name mappings for automatic widget configuration and also type information
     * about the columns which is useful for generic widgets to configure against.
     */
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

    /**
     * Gets a list of all the databases for the current datastore
     * @return The list of database names
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("databasenames")
    List<String> getDatabaseNames() {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        return queryExecutor.showDatabases()
    }

    /**
     * Gets a list of all the tables
     * @param database The database that contains the tables
     * @return The list of table names
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tablenames")
    List<String> getTableNames(@FormParam("database") String database) {
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        return queryExecutor.showTables(database)
    }

    private QueryResult layout(QueryResult queryResult) {
        // structure in Mongo (for now) is: [{"nodes":[{}, {}, ...]}, {"edges":[{}, {}, ...]}]
        List<Map<String, Object>> nodeList = queryResult.data.head().get("nodes")
        List<Map<String, Object>> edgeList = queryResult.data.head().get("edges")

        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class)
        pc.newProject()
        Workspace workspace = pc.getCurrentWorkspace()

        //Get models and controllers for this new workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel()
        ImportController importController = Lookup.getDefault().lookup(ImportController.class)

        // Init ImporterBuilder with nodes and edges collections
        ImporterBuilderJava importerBuilder = new ImporterBuilderJava()
        importerBuilder.setEdges(edgeList)
        importerBuilder.setNodes(nodeList)
        SpigotImporter importer = importerBuilder.buildImporter()

        //Import graph
        Container container
        container = importController.importSpigot(importer)
        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED)
        importController.process(container, new DefaultProcessor(), workspace)

        //Run AppLayout for 100 passes - The layout always takes the current visible view
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f))
        layout.setGraphModel(graphModel)
        layout.resetPropertiesValues()
        layout.setOptimalDistance(200f)

        layout.initAlgo()
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo()
        }
        layout.endAlgo()

        ExporterJava ej = new ExporterJava()
        ej.setWorkspace(workspace)
        ej.execute()

        def layoutResult = []
        def graph = [nodes:ej.nodeList, edges:ej.edgeList]
        layoutResult.push graph
        new TableQueryResult(data:layoutResult)
    }


    private final def execute = { def query, QueryOptions options ->
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        QueryResult queryResult = queryExecutor.execute(query, options)

        // layout the graph
        QueryResult layoutResult = layout(queryResult);
        ColumnMetadataList columnMetadataList = metadataResolver.resolveQuery(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: layoutResult, columnMetadataList: columnMetadataList)
        assembler.createClientData()
    }
}