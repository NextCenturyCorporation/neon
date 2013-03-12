package com.ncc.neon.query.filter.providers

import com.ncc.neon.query.QueryUtils
import com.ncc.neon.query.clauses.SingularWhereClause
import com.ncc.neon.query.filter.Filter

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

class FilterFactory {

    private FilterFactory() {
        // utility class, no public constructor needed
    }

    /**
     * Creates a filter that matches items that have a field value contained in any of the items that match this filter
     * @param queryExecutor The executor that runs the query
     * @param subfilter The filter to use to subselect the data
     * @param field The name of the field whose unique values will be used to apply the filter
     * @param operator The operator to apply between the field and the collection (typically in or notin)
     * @return The filter
     */
    static def createFieldFilter(queryExecutor, subfilter, field, operator) {
        def queryResult = queryExecutor.execute(QueryUtils.queryFromFilter(subfilter), false)

        def uniqueFieldValues = queryResult.collect([] as Set) { it.getFieldValue(field) }
        def inClause = new SingularWhereClause(lhs: field, operator: operator , rhs: uniqueFieldValues)
        return new Filter(dataSourceName: subfilter.dataSourceName, datasetId: subfilter.datasetId, whereClause: inClause)
    }

}
