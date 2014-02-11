/*
 * Copyright 2014 Next Century Corporation
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

package com.ncc.neon.mongo

import com.mongodb.MongoClient
import com.ncc.neon.config.MongoConfigParser

/**
 * Contains a connection to Mongo.
 */

// TODO: NEON-565 another duplication of mongo.hosts in here
class MongoTestClient {

    private MongoTestClient() {}

    private static MongoClient mongo

    @SuppressWarnings('SynchronizedMethod') // this is just simple synchronization for test methods
    static synchronized MongoClient getMongoClient() {
        if (!mongo) {
            def hostsString = System.getProperty("mongo.hosts", "localhost")
            def serverAddresses = MongoConfigParser.createServerAddresses(hostsString)
            mongo = new MongoClient(serverAddresses)
        }
        return mongo
    }

}
