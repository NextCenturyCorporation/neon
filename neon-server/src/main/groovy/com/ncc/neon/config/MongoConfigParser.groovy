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

package com.ncc.neon.config

import com.mongodb.ServerAddress



/**
 * Parses mongo configuration options for instantiating the mongo client
 */
class MongoConfigParser {

    private MongoConfigParser() {
        // utility class, no public constructor needed
    }

    /**
     * Creates a list of server addresses to pass to the mongo client
     * @param hostsString A comma separated string where each entry is <host|host-ip>[:<port>]
     */
    static def createServerAddresses(hostsString) {
        def addresses = []
        def parts = hostsString.split(",")
        parts.each {
            addresses << createServerAddress(it)
        }
        return addresses

    }

    private static def createServerAddress(hostStringPart) {
        def parts = hostStringPart.split(":")
        if (parts.length == 1) {
            return new ServerAddress(parts[0])
        }
        return new ServerAddress(parts[0], parts[1].toInteger())
    }


}
