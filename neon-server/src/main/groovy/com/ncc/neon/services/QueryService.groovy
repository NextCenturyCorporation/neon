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
package com.ncc.neon.services

import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.filter.FilterEvent
import com.ncc.neon.query.filter.providers.FilterProvider
import com.ncc.neon.query.filter.providers.QueryBased
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Path("/queryservice")
public class QueryService {

    @Autowired
    QueryExecutor queryExecutor

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query")
    public String executeQuery(Query query,
                               @DefaultValue("false") @QueryParam("includefiltered") boolean includeFiltered,
                               @QueryParam("transform") String transformClassName) {
        return wrapInDataJson(queryExecutor.execute(query, includeFiltered), transformClassName)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("addfilter")
    public FilterEvent addFilter(FilterProvider filterProvider) {
        if (filterProvider instanceof QueryBased) {
            filterProvider.queryExecutor = queryExecutor
        }
        def filter = filterProvider.provideFilter()
        def addedId = queryExecutor.addFilter(filter).toString();
        return new FilterEvent(addedIds: [addedId])
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("removefilter/{id}")
    public FilterEvent removeFilter(@PathParam("id") String filterId) {
        UUID uuid = UUID.fromString(filterId)
        queryExecutor.removeFilter(uuid)
        return new FilterEvent(removedIds: [filterId])
    }

    @POST
    @Path("replacefilter/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public FilterEvent replaceFilter(@PathParam("id") String replaceId, FilterProvider replaceWith) {
        queryExecutor.removeFilter(UUID.fromString(replaceId))
        def addEvent = addFilter(replaceWith)
        return new FilterEvent(addedIds: addEvent.addedIds, removedIds: [replaceId])
    }

    @POST
    @Path("clearfilters")
    public void clearFilters() {
        queryExecutor.clearFilters()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectionwhere")
    public void setSelectionWhere(Filter filter) {
        queryExecutor.setSelectionWhere(filter)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectedids")
    public void setSelectedIds(Collection<Object> ids) {
        queryExecutor.setSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getselectionwhere")
    public String getSelectionWhere(Filter filter,
                                    @QueryParam("transform") String transformClassName) {
        return wrapInDataJson(queryExecutor.getSelectionWhere(filter), transformClassName)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addselectedids")
    public void addSelectedIds(Collection<Object> ids) {
        queryExecutor.addSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removeselectedids")
    public void removeSelectedIds(Collection<Object> ids) {
        queryExecutor.removeSelectedIds(ids)
    }

    @POST
    @Path("clearselection")
    public void clearSelection() {
        queryExecutor.clearSelection()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("fieldnames")
    public String getFieldNames(@QueryParam("datasourcename") String dataSourceName, @QueryParam("datasetid") String datasetId) {
        return '{"fieldNames":' + queryExecutor.getFieldNames(dataSourceName, datasetId) + '}'
    }

    private def wrapInDataJson(queryResult, transformClassName = null) {
        def json = queryResult.toJson()
        if (transformClassName) {
            json = applyTransform(transformClassName, json)
        }
        def output = '{"data":' + json + '}'
        return output
    }

    private static def applyTransform(transformClassName, json) {
        def transform = QueryService.classLoader.loadClass(transformClassName).newInstance()
        return transform.apply(json)
    }

}


