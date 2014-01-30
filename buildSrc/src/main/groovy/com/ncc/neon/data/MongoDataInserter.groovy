/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ncc.neon.data

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.util.JSON
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class MongoDataInserter extends DefaultTask{

    static final ALL_DATA_FILENAME = 'data.json'

    // default value. build will override this
    String host = "localhost"
    String databaseName = "concurrencytest"
    String tableName = "records"

    @TaskAction
    void run(){
        def db = new MongoClient(host).getDB(databaseName)
        def collection = db.getCollection(tableName)
        def dbList = parseJSON("/mongo-json/${ALL_DATA_FILENAME}")
        collection.insert(dbList)
        collection.ensureIndex(new BasicDBObject("location", "2dsphere"))
    }

    private static def parseJSON(resourcePath) {
        def testDataPath = "neon-server/src/test-data" + resourcePath
        return JSON.parse(new File(testDataPath).text)
    }

}
