package com.ncc.neon.config

import org.junit.Test

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
class MongoConfigParserTest {

    @Test
    void "create server addresses from string"() {
        // a valid host must be used, so just use localhost
        def hostsString = "localhost:8440,localhost,127.0.0.1:8100"
        def serverAddresses = MongoConfigParser.createServerAddresses(hostsString)
        assert serverAddresses.size() == 3
        assertServerAddress(serverAddresses[0],"localhost",8440)
        assertServerAddress(serverAddresses[1],"localhost")
        assertServerAddress(serverAddresses[2],"127.0.0.1",8100)
    }

    private static def assertServerAddress(address,host,port=null) {
        assert address.host == host
        if ( port ) {
            assert address.port == port
        }
    }
}
