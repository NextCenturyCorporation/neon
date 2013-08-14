package com.ncc.neon.query.convert

import com.mongodb.BasicDBObject
import com.ncc.neon.query.mongo.MongoConversionStrategy

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

/*
 Tests the MongoConversionStrategy.convertQuery()
 correctly converts Query objects into MongoQuery objects
*/
class MongoConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def convertQuery(query) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState)
        conversionStrategy.convertQuery(query)
    }

    @Override
    void assertSimplestConvertQuery(query) {
        standardQueryAsserts(query)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithSortClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithDistinctClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
        standardQueryAsserts(query)
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.query == simpleQuery
        BasicDBObject orClause = createOrClause()

        assert query.whereClauseParams == orClause
        assert query.selectParams == new BasicDBObject()
    }

    protected BasicDBObject createOrClause() {
        BasicDBObject simpleClause1 = new BasicDBObject(FIELD_NAME, COLUMN_VALUE)
        BasicDBObject simpleClause2 = new BasicDBObject(FIELD_NAME_2, COLUMN_VALUE)
        return new BasicDBObject('$or', [simpleClause1, simpleClause2])
    }

    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()

        def selectParams = new BasicDBObject().append(FIELD_NAME, 1).append(FIELD_NAME_2, 1)
        assert query.selectParams == selectParams
    }

    private void standardQueryAsserts(query){
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject()
        assert query.selectParams == new BasicDBObject()

    }

}
