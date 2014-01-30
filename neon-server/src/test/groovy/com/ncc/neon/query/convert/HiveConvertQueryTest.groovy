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
 */

/*
 Tests the HiveConversionStrategy.convertQuery()
 correctly converts Query objects into hive queries
*/

class HiveConvertQueryTest extends AbstractConversionTest {

    @Override
    protected def convertQuery(query) {
        HiveConversionStrategy conversionStrategy = new HiveConversionStrategy(filterState: filterState, selectionState: new SelectionState())
        conversionStrategy.convertQuery(query, QueryOptions.ALL_DATA)
    }

    @Override
    void assertSimplestConvertQuery(query) {
        assertStandardHiveQLStatement(query)
    }

    @Override
    void assertQueryWithOneFilterInFilterState(query) {
        assertStandardHiveQLStatement(query)
    }

    @Override
    protected void assertQueryWithSortClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} order by ${FIELD_NAME} ASC".toLowerCase()
    }

    @Override
    protected void assertQueryWithLimitClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} limit $LIMIT_AMOUNT".toLowerCase()
    }

    @Override
    protected void assertQueryWithOffsetClause(query) {
        // OFFSET is not actually implemented in Hive, so it is not included in the query. Neon will adjust the
        // query to include enough results to get the offset and then manually advance the cursor to the correct
        // position.
        assertStandardHiveQLStatement(query)
    }


    @Override
    protected void assertQueryWithDistinctClause(query) {
        assert query.toLowerCase() == "select DISTINCT * from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    @Override
    protected void assertQueryWithAggregateClause(query) {
        assert query.toLowerCase() == "select sum(${FIELD_NAME}) as ${FIELD_NAME}_sum from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    @Override
    protected void assertQueryWithGroupByClauses(query) {
        assert query.toLowerCase() == "select ${FIELD_NAME_2}, sum(${FIELD_NAME}) as ${FIELD_NAME}_sum from ${DATABASE_NAME}.${TABLE_NAME} group by ${FIELD_NAME_2}, sum(${FIELD_NAME})".toLowerCase()
    }

    @Override
    protected void assertQueryWithOrWhereClause(query) {
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME} where (${FIELD_NAME} = '${COLUMN_VALUE}' or ${FIELD_NAME_2} = '${COLUMN_VALUE}')".toLowerCase()
    }

    @Override
    protected void assertQueryWithEmptyFilter(query) {
        assertStandardHiveQLStatement(query)
    }


    @Override
    protected void assertSelectClausePopulated(query) {
        assert query.toLowerCase() == "select $FIELD_NAME, $FIELD_NAME_2 from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

    private void assertStandardHiveQLStatement(query){
        assert query.toLowerCase() == "select * from ${DATABASE_NAME}.${TABLE_NAME}".toLowerCase()
    }

}
