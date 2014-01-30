package com.ncc.neon.query.convert

import com.ncc.neon.query.QueryOptions
import com.ncc.neon.query.filter.SelectionState
import com.ncc.neon.query.hive.HiveConversionStrategy


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
