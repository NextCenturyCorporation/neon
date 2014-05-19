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

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import groovy.mock.interceptor.StubFor
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.beans.BeanUtils

class MongoConnectionClientTest {

    /** the hosts connected to during the test */
    private static final  List<ServerAddress> HOSTS = []

    @BeforeClass
    static void beforeClass() {
        stubServerAddress()
        stubMongoClient()
    }

    private static void stubServerAddress() {
        // stub out the server addresses since the default implementation actually tries to connect to the hosts
        ServerAddress.metaClass.constructor = { String host ->
            def addrStub = new StubFor(ServerAddress)
            addrStub.demand.getHost { host }
            return addrStub.proxyDelegateInstance()
        }

        ServerAddress.metaClass.constructor = { String host, int port ->
            def addrStub = new StubFor(ServerAddress)
            addrStub.demand.getHost { host }
            addrStub.demand.getPort { port }
            return addrStub.proxyDelegateInstance()
        }

        ServerAddress.metaClass.constructor = {->
            return BeanUtils.instantiateClass(ServerAddress)
        }
    }

    private static void stubMongoClient() {
        // when mongo is instantiated, don't actually create a connection. we're just interested in the hosts.
        // WARNING: the actual instantiated mongo client will not be configured to connect to the hosts - this returns
        // a default mongo client since we don't use it in the tests other than to verify the hsots (which are added to
        // the hosts list in here). for more correct behavior, this constructor would actually need to setup the
        // mongo client with correct server addresses
        // we have to mock the constructor here since ServerAddress is a class and we can't pass the proxied class
        // to the MongoClient object since it's not actually of type ServerAddress
        //
        MongoClient.metaClass.constructor = { List<ServerAddress> addresses ->
            HOSTS.addAll(addresses)
            // as noted in the docs, just return a default client. we don't actually use it in the tests
            return BeanUtils.instantiateClass(MongoClient)
        }
    }

    @AfterClass
    static void afterClass() {
        ServerAddress.metaClass = null
        MongoClient.metaClass = null
    }

    @After
    void afterTest() {
        HOSTS.clear()
    }

    @Test
    void "connect to single host"() {
        String host = "notarealhost1"
        ConnectionInfo info = new ConnectionInfo(dataSource: DataSources.mongo, host: host)
        // trigger the overridden constructor that stores the hosts for the test
        new MongoConnectionClient(info)
        assertHosts([host])

    }

    @Test
    void "connect to a single host with a port"() {
        String host = "notarealhost1:12345"
        ConnectionInfo info = new ConnectionInfo(dataSource: DataSources.mongo, host: host)
        // trigger the overridden constructor that stores the hosts for the test
        new MongoConnectionClient(info)
        assertHosts([host])
    }

    @Test
    void "connect to multiple hosts"() {
        String host1 = "notarealhost1:12345"
        String host2 = "notarealhost2:23456"
        ConnectionInfo info = new ConnectionInfo(dataSource: DataSources.mongo, host: [host1,host2].join(","))
        // trigger the overridden constructor that stores the hosts for the test
        new MongoConnectionClient(info)
        assertHosts([host1,host2])
    }

    private static void assertHosts(def hostPortPairs) {
        assert hostPortPairs.size() == HOSTS.size()
        hostPortPairs.eachWithIndex { pair, index ->
            def addr = HOSTS[index]
            def parts = pair.split(":")
            String host = parts[0]
            assert addr.host == host
            if ( parts.length > 1 ) {
                int port = parts[1].toInteger()
                assert addr.port == port
            }


        }
    }


}
