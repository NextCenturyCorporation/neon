package com.ncc.neon.metadata.store

import com.mongodb.BasicDBObject
import com.mongodb.DBObject

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
 */

/**
 * Turns a groovy object into a mongo DbObject and visa-versa.
 */

class MongoObjectConverter {

    private static final String CLASS = "class"

    BasicDBObject convertToMongo(def obj) {
        BasicDBObject document = new BasicDBObject()
        obj.metaClass.properties.each {
            def value = obj[it.name]
            if (it.name == CLASS) {
                value = obj.class.name
            }
            document.append(it.name, value)
        }
        return document
    }

    def convertToObject(DBObject dbObject) {
        def object = MongoObjectConverter.classLoader.loadClass(dbObject.get(CLASS)).newInstance()

        dbObject.each { k, v ->
            if (k == CLASS || k == "_id") {
                return
            }
            object[k] = v
        }
        return object
    }

}
