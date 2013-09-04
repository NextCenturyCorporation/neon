package com.ncc.neon.query

import org.apache.commons.lang.math.NumberUtils

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
class QueryUtils {

    private QueryUtils() {
        // utility class, no public constructor needed
    }

    static def queryFromFilter(def filter) {
        return new Query(filter: filter)
    }

    /**
     * Wraps a query in a data block
     * @param queryResult The query result to be wrapped
     * @param transformClassName A transform on the data. Defaults to null
     * @param transformParams Parameters needed by the transform
     * @return a data json object with an element.
     */

    static def wrapInDataJson(queryResult, transformClassName = null, transformParams = []) {
        def json = queryResult.toJson()
        if (transformClassName) {
            json = applyTransform(transformClassName, transformParams, json)
        }
        return '{"data":' + json + '}'
    }

    private static def applyTransform(transformClassName, transformParams, json) {
        def transform = instantiateTransform(transformClassName, transformParams)
        return transform.apply(json)
    }

    private static instantiateTransform(transformClassName, transformParams) {
        def typedParams = transformParams.collect { NumberUtils.isNumber(it) ? NumberUtils.createNumber(it) : it }
        def transformParamTypes = typedParams.collect { it.class }
        def transformClass = QueryUtils.classLoader.loadClass(transformClassName)
        def constructor = transformClass.getConstructor(transformParamTypes as Class[])
        return constructor.newInstance(typedParams as Object[])
    }

}
