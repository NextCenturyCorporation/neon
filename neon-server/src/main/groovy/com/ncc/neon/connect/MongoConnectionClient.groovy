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

package com.ncc.neon.connect
import com.mongodb.MongoClient


/**
 * Holds a connection to mongo
 */

class MongoConnectionClient implements ConnectionClient{

    private MongoClient mongo

    public MongoConnectionClient(ConnectionInfo info){
        mongo = new MongoClient(info.connectionUrl)
    }

    MongoClient getMongo(){
        return mongo
    }

    /**
     * Close the connection to mongo.
     */
    @Override
    void close(){
        mongo?.close()
        mongo = null
    }

}
