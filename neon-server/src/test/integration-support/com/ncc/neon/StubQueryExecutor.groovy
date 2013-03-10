package com.ncc.neon

import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.filter.Filter
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor

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
class StubQueryExecutor implements QueryExecutor {


    @Override
    QueryResult execute(Query query, boolean includeFiltered) {
        return new StubQueryResult()
    }

    @Override
    Collection<String> getFieldNames(String dataSourceName, String datasetId) {
        return []
    }

    @Override
    UUID addFilter(Filter filter) {
        return UUID.randomUUID()
    }

    @Override
    void removeFilter(UUID id) {
    }

    @Override
    void clearFilters() {
    }

    @Override
    void setSelectionWhere(Filter filter) {
    }

    @Override
    void setSelectedIds(Collection<Object> ids) {
    }

    @Override
    void addSelectedIds(Collection<Object> ids) {
    }

    @Override
    void removeSelectedIds(Collection<Object> ids) {
    }

    @Override
    void clearSelection() {
    }

    @Override
    QueryResult getSelectionWhere(Filter filter) {
        return new StubQueryResult()
    }

}
