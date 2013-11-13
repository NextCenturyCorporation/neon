package com.ncc.neon.services

import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryUtils
import com.ncc.neon.query.filter.Filter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
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
@Path("/selectionservice")
class SelectionService {

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectionwhere")
    void setSelectionWhere(Filter filter) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.setSelectionWhere(filter)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("setselectedids")
    void setSelectedIds(Collection<Object> ids) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.setSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getselectionwhere")
    String getSelectionWhere(Filter filter,
                             @QueryParam("transform") String transformClassName,
                             @QueryParam("param") List<String> transformParams) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        return QueryUtils.wrapJsonInDataElement(queryExecutor.getSelectionWhere(filter), transformClassName, transformParams)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addselectedids")
    void addSelectedIds(Collection<Object> ids) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.addSelectedIds(ids)
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("removeselectedids")
    void removeSelectedIds(Collection<Object> ids) {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.removeSelectedIds(ids)
    }

    @POST
    @Path("clearselection")
    void clearSelection() {
        QueryExecutor queryExecutor = queryExecutorFactory.create()
        queryExecutor.clearSelection()
    }
}
