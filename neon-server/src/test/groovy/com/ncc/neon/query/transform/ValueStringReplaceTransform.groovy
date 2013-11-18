package com.ncc.neon.query.transform

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
 * A transform used for testing that can replaces the string values in json results
 */
class ValueStringReplaceTransform {

    private final String replaceThis
    private final int replaceWith

    ValueStringReplaceTransform(String replaceThis, Integer replaceWith) {
        this.replaceThis = replaceThis
        this.replaceWith = replaceWith
    }

    /**
     * A no arg constructor for testing transforms with no arguments. It replaces occurrences of "abc"
     * with 10
     */
    ValueStringReplaceTransform() {
        this("abc", 10)
    }

    @Override
    String apply(inputJsonArray) {
        def jsonArray = new JSONArray(inputJsonArray)
        jsonArray.length().times { index ->
            def object = jsonArray.getJSONObject(index)
            object.keys().each { key ->
                def val = object.getString(key)
                if ( val == replaceThis ) {
                    val = replaceWith
                }
                object.put(key, val)
            }
        }
        return jsonArray.toString()
    }

}
