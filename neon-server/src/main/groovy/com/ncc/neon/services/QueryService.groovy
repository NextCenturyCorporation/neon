package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionState
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryGroup
import com.ncc.neon.query.filter.*
import org.apache.commons.lang.math.NumberUtils
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
    ConnectionState connectionState

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("connect")
    String connect(@FormParam("datastore") String datastore, @FormParam("hostname") String hostname) {
        return connectionState.createConnection(datastore, hostname)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    String executeQuery(Query query,
                        @DefaultValue("false") @QueryParam("includefiltered") boolean includeFiltered,
                        @QueryParam("transform") String transformClassName,
                        @QueryParam("param") List<String> transformParams) {
        return wrapInDataJson(queryExecutor.execute(query, includeFiltered), transformClassName, transformParams)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup")
    String executeQueryGroup(QueryGroup query,
                             @DefaultValue("false") @QueryParam("includefiltered") boolean includeFiltered,
                             @QueryParam("transform") String transformClassName,
                             @QueryParam("param") List<String> transformParams) {
        return wrapInDataJson(queryExecutor.execute(query, includeFiltered), transformClassName, transformParams)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("registerforfilterkey")
    FilterEvent registerForFilterKey(DataSet dataSet) {
        FilterKey filterKey = queryExecutor.registerForFilterKey(dataSet)
        FilterEvent.fromFilterKey(filterKey)
    }

    @POST
    @Path("addfilter")
    @Consumes(MediaType.APPLICATION_JSON)
    void addFilter(FilterContainer container) {
        queryExecutor.addFilter(container.filterKey, container.filter)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removefilter")
    void removeFilter(FilterKey filterKey) {
        queryExecutor.removeFilter(filterKey)
    }

    @POST
    @Path("replacefilter")
    @Consumes(MediaType.APPLICATION_JSON)
    void replaceFilter(FilterContainer container) {
        removeFilter(container.filterKey)
    }

    @POST
    @Path("clearfilters")
    void clearFilters() {
        queryExecutor.clearFilters()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectionwhere")
    void setSelectionWhere(Filter filter) {
        queryExecutor.setSelectionWhere(filter)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectedids")
    void setSelectedIds(Collection<Object> ids) {
        queryExecutor.setSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getselectionwhere")
    String getSelectionWhere(Filter filter,
                             @QueryParam("transform") String transformClassName,
                             @QueryParam("param") List<String> transformParams) {
        return wrapInDataJson(queryExecutor.getSelectionWhere(filter), transformClassName, transformParams)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addselectedids")
    void addSelectedIds(Collection<Object> ids) {
        queryExecutor.addSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removeselectedids")
    void removeSelectedIds(Collection<Object> ids) {
        queryExecutor.removeSelectedIds(ids)
    }

    @POST
    @Path("clearselection")
    void clearSelection() {
        queryExecutor.clearSelection()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fieldnames")
    FieldNames getFieldNames(@QueryParam("databaseName") String databaseName, @QueryParam("tableName") String tableName) {
        def fieldNames = queryExecutor.getFieldNames(databaseName, tableName)
        return new FieldNames(fieldNames: fieldNames)
    }

    private def wrapInDataJson(queryResult, transformClassName = null, transformParams = []) {
        def json = queryResult.toJson()
        if (transformClassName) {
            json = applyTransform(transformClassName, transformParams, json)
        }
        def output = '{"data":' + json + '}'
        return output
    }

    private static def applyTransform(transformClassName, transformParams, json) {
        def transform = instantiateTransform(transformClassName, transformParams)
        return transform.apply(json)
    }

    private static instantiateTransform(transformClassName, transformParams) {
        def typedParams = transformParams.collect { NumberUtils.isNumber(it) ? NumberUtils.createNumber(it) : it }
        def transformParamTypes = typedParams.collect { it.class }
        def transformClass = QueryService.classLoader.loadClass(transformClassName)
        def constructor = transformClass.getConstructor(transformParamTypes as Class[])
        return constructor.newInstance(typedParams as Object[])
    }

    private QueryExecutor getQueryExecutor() {
        connectionState.queryExecutor
    }
}


