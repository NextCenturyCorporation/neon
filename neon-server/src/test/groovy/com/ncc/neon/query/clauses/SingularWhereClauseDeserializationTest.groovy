package com.ncc.neon.query.clauses
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test


/**
 * Test deserialization of singular where clauses.
 */

class SingularWhereClauseDeserializationTest {

    @Test
    void "string deserialization"(){
        SingularWhereClause deserialized = deserializeWithRhsOf("value")
        assert deserialized.rhs == "value"
    }

    @Test
    void "boolean deserialization"(){
        SingularWhereClause deserialized = deserializeWithRhsOf(false)
        assert !deserialized.rhs
    }

    @Test
    void "string representation of false is still false"(){
        SingularWhereClause deserialized = deserializeWithRhsOf("false")
        assert deserialized.rhs == "false"
    }

    @Test
    void "date deserialization"(){
        SingularWhereClause deserialized = deserializeWithRhsOf("2013-10-11")
        assert deserialized.rhs instanceof Date
    }

    private SingularWhereClause deserializeWithRhsOf(def rhs) {
        ObjectMapper objectMapper = new ObjectMapper()
        SingularWhereClause singularWhereClause = new SingularWhereClause(rhs: rhs)

        def whereClauseString = objectMapper.writeValueAsString(singularWhereClause)
        return objectMapper.readValue(whereClauseString, SingularWhereClause)

    }

}
