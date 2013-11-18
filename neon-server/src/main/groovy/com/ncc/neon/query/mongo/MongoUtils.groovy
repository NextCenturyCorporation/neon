package com.ncc.neon.query.mongo

import org.bson.types.ObjectId
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
 * Utility methods for working with mongodb data
 */
class MongoUtils {

    /**
     * Converts an oid object (typically created from a json mapping) to a mongo  {@link org.bson.types.ObjectId}.
     * If the oid object is already an ObjectId, it is returned as-is.
     * @param oid
     * @return
     */
    static def toObjectId(oid) {
        return oid instanceof ObjectId ? oid : new ObjectId(oid)
    }

    /**
     * Converts a collection of oid objects (typically created from json mappings) to mongo ObjectIds
     * @param oids
     * @return
     */
    static def oidsToObjectIds(oids) {
        def objectIds = []
        oids.each {
            objectIds << toObjectId(it)
        }
        return objectIds
    }

}
