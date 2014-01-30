package com.ncc.neon.query.clauses

import org.junit.Before
import org.junit.Test



class SelectClauseTest {

    def selectClause

    @Before
    void before() {
        selectClause = new SelectClause()
    }


    @Test
    void "indicates if all or subset of fields is selected"() {
        // should default to all fields
        assert selectClause.selectAllFields

        selectClause.fields = ["field1","field2"]
        assert !selectClause.selectAllFields
    }

}
