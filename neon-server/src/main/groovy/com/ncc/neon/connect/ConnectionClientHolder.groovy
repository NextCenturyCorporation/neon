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

package com.ncc.neon.connect
/**
 * Holds a ConnectionClient. This is used to only create the connection client when it is actually needed.
 * This class is threadsafe and initClient is guaranteed to only initialize the client once, even if called
 * multiple times.
 */
class ConnectionClientHolder {


    private final Object lock = new Object()
    private final ConnectionClientFactory factory
    private volatile ConnectionClient connection


    ConnectionClientHolder(ConnectionClientFactory factory) {
        this.factory = factory
    }


    ConnectionClient initClient(ConnectionInfo connectionInfo) {
        synchronized(lock) {
            // check for the client being initialized in case 2 users of this class both see the client as null
            // and try to initialize it
            if (!connection) {
                connection = factory.createConnectionClient(connectionInfo)
            }
            return connection
        }
    }

    ConnectionClient getConnection() {
        synchronized(lock) {
            return connection
        }
    }

}
