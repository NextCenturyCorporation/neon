package com.ncc.neon.query.jackson

import com.ncc.neon.query.clauses.DistanceUnit
import groovy.mock.interceptor.StubFor
import org.codehaus.jackson.JsonParser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

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
@RunWith(Parameterized)
class DistanceUnitDeserializerTest {


    def deserializer
    def distanceUnit
    def expectedEnum


    @Before
    void setUp() {
        deserializer = new DistanceUnitDeserializer()
    }

    DistanceUnitDeserializerTest(distanceUnit, expectedEnum) {
        this.distanceUnit = distanceUnit
        this.expectedEnum = expectedEnum
    }

    @Test
    void "deserialize distance units"() {
        def parser = createJsonParser()
        def actualEnum = deserializer.deserialize(parser, null)
        assert actualEnum == expectedEnum
    }

    @Parameters
    static Collection<Object[]> data() {
        return [
                ["mile", DistanceUnit.MILE] as Object[],
                ["meter", DistanceUnit.METER] as Object[],
                ["km", DistanceUnit.KM] as Object[]
        ]
    }

    private def createJsonParser() {
        def parserStub = new StubFor(JsonParser)
        parserStub.demand.getText { distanceUnit }
        return parserStub.proxyInstance()
    }

}
