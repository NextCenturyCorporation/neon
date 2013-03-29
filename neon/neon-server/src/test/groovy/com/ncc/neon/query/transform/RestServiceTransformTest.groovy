package com.ncc.neon.query.transform

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import org.junit.After
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
class RestServiceTransformTest {


    def queryParams

    @After
    void after() {
        HTTPBuilder.metaClass = null
    }


    RestServiceTransformTest(queryParams) {
        this.queryParams = queryParams
    }

    @Parameters
    public static Collection<Object[]> data() {
        return [
                [[:]] as Object[],
                [[param1: "value1", param2: "value2"]] as Object[]
        ]

    }


    @Test
    void "json transformed by service"() {
        def inputJson = /[{"in1":"val"}]/
        def outputJson = /[{"abc":"def","ghi":5}]/
        // the output from the rest client is parsed into ta list
        def outputJsonAsList = [ [abc:"def",ghi:5]  ]
        def host = "http://localhost"
        def path = "pathToWebService"
        def queryString = buildQueryString()

        RESTClient.metaClass.post = { Map<String, ?> args ->
            assert delegate.uri.toString() == host
            assertPostParams(args, path, inputJson)
            // the real RESTClient returns an object with a data property so simulate that here
            return [data:outputJsonAsList]
        }
        def transform = new RestServiceTransform(host, path + queryString)
        def transformedJson = transform.apply(inputJson)
        assert transformedJson == outputJson
    }

    private def assertPostParams(actual, expectedPath, expectedBody) {
        assert actual.path == expectedPath
        assert actual.body == expectedBody
        assert actual.query == queryParams

    }

    private def buildQueryString() {
        def queryString = ""
        if (queryParams) {
            queryParams.eachWithIndex { param, val, index ->
                queryString += (index == 0) ? "?" : "&"
                queryString += param + "=" + val
            }
        }
        return queryString
    }

}
