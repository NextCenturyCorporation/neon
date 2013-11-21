package com.ncc.neon.query.clauses
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test
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
        assert deserialized.rhs == false
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
        SingularWhereClause singularWhereClause = new SingularWhereClause(lhs: "column", operator: "=", rhs: rhs)
        ObjectMapper objectMapper = new ObjectMapper()

        def whereClauseString = objectMapper.writeValueAsString(singularWhereClause)

        SingularWhereClause deserialized = objectMapper.readValue(whereClauseString, SingularWhereClause)
        deserialized
    }

}
