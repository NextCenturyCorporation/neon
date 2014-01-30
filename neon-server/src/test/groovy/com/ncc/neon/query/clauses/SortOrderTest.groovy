package com.ncc.neon.query.clauses

import org.junit.Test


class SortOrderTest {

    @Test
    void "lookup by direction"() {
        assert SortOrder.ASCENDING == SortOrder.fromDirection(1)
        assert SortOrder.DESCENDING == SortOrder.fromDirection(-1)
    }

}
