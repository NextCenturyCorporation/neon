/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
