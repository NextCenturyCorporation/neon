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
 Tests the MongoConversionStrategy.convertQueryWithFilterState()
 correctly converts Query objects into MongoQuery objects
*/

class MongoConvertQueryWithFiltersTest extends MongoConvertQueryTest{

    @Override
    protected def whenExecutingConvertQuery(query) {
        MongoConversionStrategy conversionStrategy = new MongoConversionStrategy(filterState)
        conversionStrategy.convertQueryWithFilterState(query)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assert query.query == simpleQuery
        assert query.whereClauseParams == new BasicDBObject(COLUMN_NAME, COLUMN_VALUE)
        assert query.selectParams == new BasicDBObject()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.query == simpleQuery
        BasicDBObject andClause = createAndClause()

        assert query.whereClauseParams == andClause
        assert query.selectParams == new BasicDBObject()
    }

    private BasicDBObject createAndClause() {
        BasicDBObject orClause = createOrClause()
        BasicDBObject simpleClause = new BasicDBObject(COLUMN_NAME, COLUMN_VALUE)
        return new BasicDBObject('$and', [orClause, simpleClause])
    }

}
