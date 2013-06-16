package com.ncc.neon.query.jdbc

import com.ncc.neon.query.AbstractQueryExecutor
import com.ncc.neon.query.QueryBuilder
import com.ncc.neon.query.QueryResult
import org.springframework.beans.factory.annotation.Autowired


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
 * Executes queries on a JDBC-based data store
 */
class JdbcQueryExecutor extends AbstractQueryExecutor {

    @Autowired
    private JdbcClient jdbcClient

    QueryBuilder queryBuilder

    JdbcQueryExecutor(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder
    }

    @Override
    protected QueryResult doExecuteQuery(query) {
        List<Map> resultList = jdbcClient.executeQuery(query.queryString)

        return new JdbcQueryResult(resultList: resultList)
    }

    @Override
    protected getIdFieldName() {
        // TODO: NEON-75 revisit this - there will not necessarily be an Id field
    }

    @Override
    protected createQueryBuilder() {
        return queryBuilder
    }

    @Override
    Collection<String> getFieldNames(String dataSourceName, String datasetId) {
        return jdbcClient.getColumnNames(dataSourceName, datasetId)
    }

    @Override
    String getDatastoreName() {
        return "JDBC(" + jdbcClient.databaseType + ")@" + jdbcClient.dbHostString
    }
}
