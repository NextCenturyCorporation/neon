package com.ncc.neon.data

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.util.JSON
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
/*
 * ************************************************************************
 * Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
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

class MongoDataInserter extends DefaultTask{

    static final String DATABASE_NAME = "concurrencytest"
    static final String TABLE_NAME = "records"
    static final ALL_DATA_FILENAME = 'data.json'

    String host = "localhost"

    @TaskAction
    void run(){
        def db = new MongoClient(host).getDB(DATABASE_NAME)
        def collection = db.getCollection(TABLE_NAME)
        def dbList = parseJSON("/mongo-json/${ALL_DATA_FILENAME}")
        collection.insert(dbList)
    }

    private static def parseJSON(resourcePath) {
        def testDataPath = "neon-server/src/test-data" + resourcePath
        return JSON.parse(new File(testDataPath).text)
    }

}
