package com.ncc.neon.query

import com.ncc.neon.query.clauses.LimitClause
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.parse.NeonBaseListener
import com.ncc.neon.query.parse.NeonParser

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

class QueryCreator extends NeonBaseListener{

    private String collectionName
    private String databaseName

    Query createQuery(){
        Query query =  new Query()
        query.filter = new Filter(dataSourceName: databaseName, datasetId: collectionName)
        query.limitClause = new LimitClause(limit: 100)
        return query
    }

    @Override
    public void exitDatabase(NeonParser.DatabaseContext ctx) {
        databaseName = ctx.STRING()
    }

    @Override
    public void exitQuery(NeonParser.QueryContext ctx) {
        collectionName = ctx.STRING()
    }

}
