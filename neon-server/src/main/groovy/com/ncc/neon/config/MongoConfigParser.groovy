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
