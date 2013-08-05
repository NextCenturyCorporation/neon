package com.ncc.neon.query.hive

import com.ncc.neon.query.clauses.SortOrder
import com.ncc.neon.query.jdbc.SqlQuery

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

/**
 * A container for the information needed to execute a query against a Hive store
 */
class HiveQuery implements SqlQuery {

    def selectClause
    def whereClause
    def groupByClauses
    def clusterByClauses
    def sortClauses
    def limitClause

    @Override
    String getQueryString() {
        StringBuilder builder = new StringBuilder()
        builder << "select " << selectClause.fields.join(",") << " from " << selectClause.dataStoreName << "." << selectClause.databaseName
        if (whereClause != null) {
            builder << " where " << whereClause.toString()
        }
        if (groupByClauses.size > 0) {
            builder << " group by " << groupByClauses.collect { it.field }.join(",")
        }
        if (sortClauses.size > 0) {
            builder << " order by " << sortClauses.collect { it.fieldName + ((it.sortOrder == SortOrder.ASCENDING) ? " ASC" : " DESC") }.join(",")
        }
        if (limitClause != null) {
            builder << " limit " << limitClause.limit
        }
        return builder.toString()
    }
}
