package com.ncc.neon.query

import org.junit.Before
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
 */

class BatchQueryResultTest {

    private static final def QUERY1_JSON = '[{"q1a":"v1a"},{"q1b":"v1b"}]'

    private static final def QUERY2_JSON = '[{"q2a":"v2a"},{"q2b":"v2b"}]'

    private static final def QUERY1_RESULT = createQueryResult(QUERY1_JSON)
    private static final def QUERY2_RESULT = createQueryResult(QUERY2_JSON)

    def batchQueryResult

    @Before
    void before() {
        batchQueryResult = new BatchQueryResult()
        batchQueryResult.namedResults.q1 = QUERY1_RESULT
        batchQueryResult.namedResults.q2 = QUERY2_RESULT
    }

    @Test
    void "convert batch query to json"() {
        def actual = batchQueryResult.toJson()
        def expected = '{"q1":' + QUERY1_JSON + ',"q2":' + QUERY2_JSON + '}'

        assert actual == expected
    }

    private static def createQueryResult(json) {
        def result = [
                toJson: { json }
        ] as QueryResult
        return result

    }

}
