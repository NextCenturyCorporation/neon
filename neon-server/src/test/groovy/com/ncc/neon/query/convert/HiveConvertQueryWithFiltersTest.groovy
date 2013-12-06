package com.ncc.neon.query.convert

import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.hive.HiveConversionStrategy
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
 Tests the HiveConversionStrategy.convertQueryWithFilterState()
  correctly converts Query objects into hive queries
*/
class HiveConvertQueryWithFiltersTest extends HiveConvertQueryTest{

    @Override
    protected def convertQuery(query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.FILTERED_DATA)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ${COLUMN_NAME} = '${COLUMN_VALUE}'".toLowerCase()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where ((${FIELD_NAME} = '${COLUMN_VALUE}' or ${FIELD_NAME_2} = '${COLUMN_VALUE}') and ${COLUMN_NAME} = '${COLUMN_VALUE}')".toLowerCase()
    }

}
