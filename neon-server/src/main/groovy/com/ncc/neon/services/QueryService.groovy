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

import com.ncc.neon.metadata.model.column.ColumnMetadataList
import com.ncc.neon.metadata.model.dataset.WidgetAndDatasetMetadataList
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.QueryResult
import com.ncc.neon.result.AssembleClientData
import com.ncc.neon.result.ClientData
import com.ncc.neon.result.InitializingClientData
import com.ncc.neon.result.MetadataResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType



/**
 * Service for executing queries against an arbitrary data store.
 */

@Component
@Path("/queryservice")
class QueryService {

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
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup")
    ClientData executeQueryGroup(QueryGroup query) {
        return execute(query, QueryOptions.FILTERED_DATA)
    }

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

    private final def execute = { def query, QueryOptions options ->
        QueryExecutor queryExecutor = queryExecutorFactory.getExecutor()
        QueryResult queryResult = queryExecutor.execute(query, options)
        ColumnMetadataList columnMetadataList = metadataResolver.resolveQuery(query)

        AssembleClientData assembler = new AssembleClientData(queryResult: queryResult, columnMetadataList: columnMetadataList)
        return assembler.createClientData()
    }
}


