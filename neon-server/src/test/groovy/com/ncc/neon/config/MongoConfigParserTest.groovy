package com.ncc.neon.config

import org.junit.Test


class MongoConfigParserTest {

    @Test
    void "create server addresses from string"() {
        // a valid host must be used, so just use localhost
        def hostsString = "localhost:8440,localhost,127.0.0.1:8100"
        def serverAddresses = MongoConfigParser.createServerAddresses(hostsString)
        assert serverAddresses.size() == 3
        assertServerAddressMatchesHostAndPort(serverAddresses[0],"localhost",8440)
        assertServerAddressMatchesHostAndPort(serverAddresses[1],"localhost")
        assertServerAddressMatchesHostAndPort(serverAddresses[2],"127.0.0.1",8100)
    }

    private static def assertServerAddressMatchesHostAndPort(address,host,port=null) {
        assert address.host == host
        if ( port ) {
            assert address.port == port
        }
    }
}
