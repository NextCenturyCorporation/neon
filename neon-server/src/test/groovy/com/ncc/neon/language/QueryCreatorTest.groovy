package com.ncc.neon.language

import com.ncc.neon.query.Query
import org.junit.Test




class QueryCreatorTest {

    @Test
    void "one instance of query creator creates two different queries"(){
        //The query creator has one instance in the Antlr Parser,
        // which has one instance in the Language service.
        // We need to be able to create multiple Query Objects from the same QueryCreator.
        QueryCreator creator = new QueryCreator()
        Query query1 = creator.createQuery()
        Query query2 = creator.createQuery()

        assert query1 != query2
    }

}
