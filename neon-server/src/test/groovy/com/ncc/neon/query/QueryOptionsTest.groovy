package com.ncc.neon.query

import org.junit.Test




class QueryOptionsTest {

    @Test
    void "all data" (){
        QueryOptions options = QueryOptions.ALL_DATA
        assert options.disregardFilters
        assert options.disregardSelection
    }

    @Test
    void "filtered and selected data" (){
        QueryOptions options = QueryOptions.FILTERED_AND_SELECTED_DATA
        assert !options.disregardFilters
        assert !options.disregardSelection
    }
}
