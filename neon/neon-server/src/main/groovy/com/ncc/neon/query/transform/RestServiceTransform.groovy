package com.ncc.neon.query.transform

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.json.JSONArray

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

/**
 * A transform that sends the input data to a RESTful endpoint for transformation.
 */
class RestServiceTransform implements JsonTransform {


    private final def restClient
    private final def path
    private final def queryParams

    RestServiceTransform(String host, String path) {
        restClient = new RESTClient(host)
        def parsed = parsePath(path)
        this.path = parsed[0]
        this.queryParams = parsed[1]
    }

    @Override
    String apply(inputJsonArray) {
        def rows = restClient.post(
                path: path,
                body: inputJsonArray,
                query: queryParams,
                contentType: ContentType.JSON,
                requestContentType: ContentType.JSON
        ).data
        return new JSONArray(rows).toString()
    }

    /**
     * Splits the path between the path component and its query params
     * @param path
     * @return
     */
    private static def parsePath(path) {
        def pathParts = path.split("\\?");
        def queryParams = [:]

        if (pathParts.length == 2) {
            pathParts[1].split("&").each {
                def paramAndValue = it.split("=")
                queryParams[paramAndValue[0]] = paramAndValue[1]
            }
        }
        return [pathParts[0], queryParams]
    }

}


