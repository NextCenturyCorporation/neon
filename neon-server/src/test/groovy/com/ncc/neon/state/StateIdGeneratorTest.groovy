package com.ncc.neon.state

import org.junit.Before
import org.junit.Test

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

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

class StateIdGeneratorTest {

    private StateIdGenerator stateIdGenerator

    @Before
    void before() {
        stateIdGenerator = new StateIdGenerator()
    }

    @Test
    void "default id is consistent"() {
        UUID idRequest1 = stateIdGenerator.id
        UUID idRequest2 = stateIdGenerator.id

        assert idRequest1 != null
        assert idRequest2 != null
        assert idRequest1 == idRequest2
    }

    @Test
    void "ids for qualifiers are unique and consistent"() {
        UUID globalId = stateIdGenerator.id
        UUID idQualifier1a = stateIdGenerator.getId("qualifier1")
        UUID idQualifier1b = stateIdGenerator.getId("qualifier1")
        UUID idQualifier2a = stateIdGenerator.getId("qualifier2")
        UUID idQualifier2b = stateIdGenerator.getId("qualifier2")

        assert idQualifier1a != globalId
        assert idQualifier2a != globalId
        assert idQualifier1a != idQualifier2a
        assert idQualifier1a == idQualifier1b
        assert idQualifier2a == idQualifier2b
    }

    @Test
    void "multiple threads get the same ids"() {
        def numThreads = 5
        def latch = new CountDownLatch(numThreads)

        // the ids that each of the threads generated
        def ids = new ConcurrentLinkedQueue<UUID>()
        def threads = createIdRequestThreads(numThreads, ids, latch)
        threads.each { it.start() }
        latch.await()

        assert ids.size() == numThreads
        ids.eachWithIndex { id, index ->
            assert ids[0] == id: "Error matching ID at index ${index}"
        }
    }

    private def createIdRequestThreads(int numThreads, Collection<UUID> idStore, CountDownLatch latch) {
        def threads = []
        // create threads that will each request the same id
        numThreads.times {
            threads << new Thread({ run ->
                idStore << stateIdGenerator.getId("aQualifier")
                latch.countDown()
            }
            as Runnable)
        }
        return threads
    }
}
