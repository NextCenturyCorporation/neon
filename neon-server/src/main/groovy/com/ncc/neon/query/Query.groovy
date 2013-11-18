package com.ncc.neon.query

import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.filter.Filter
import groovy.transform.ToString
import org.codehaus.jackson.annotate.JsonIgnoreProperties

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

/**
 * A query stores a filter for selecting data and optional aggregation methods for grouping the data.
 * The query is translated to a datastore specific operation which returns the appropriate data.
 */
@ToString(includeNames = true)
@JsonIgnoreProperties(value = ['disregardFilters_'])
class Query {

    Filter filter
    boolean isDistinct = false
    List<String> fields = SelectClause.ALL_FIELDS
    List<AggregateClause> aggregates = []
    List<GroupByClause> groupByClauses = []
    List<SortClause> sortClauses = []
    LimitClause limitClause

    def getDatabaseName() {
        filter.databaseName
    }

    def getTableName() {
        filter.tableName
    }

}
