/*
 * Copyright 2016 Next Century Corporation
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
package com.ncc.neon.services

import org.junit.Before
import org.junit.Test

/**
 *
 */
class AdminServiceTest {

    private AdminService adminService


    @Before
    void setup() {
        adminService = new AdminService()
    }

    @Test
    void "test clear mongo query cache"() {
        assert adminService.clearMongoQueryCache()
    }

    @Test
    void "test display mongo query cache"() {
        assert adminService.displayMongoQueryCache()
    }

    @Test
    void "test display mongo query cache stats"() {
        assert adminService.displayMongoQueryCacheStatistics() == "Cache hits / misses:  0 0 (100%)"
    }

    @Test
    void "test mongo query cache limit"() {
        assert adminService.displayMongoQueryCacheLimit() == 0
        adminService.setMongoQueryCacheLimit(5)
        assert adminService.displayMongoQueryCacheLimit() == 5
    }

    @Test
    void "test mongo query cache time limit"() {
        assert adminService.displayMongoQueryCacheTimeLimit() == 9999999.0
        adminService.setMongoQueryCacheTimeLimit(1000.0)
        assert adminService.displayMongoQueryCacheTimeLimit() == 1000.0
    }

    @Test
    void "test mongo query caching"() {
        assert adminService.displayMongoCachingQueries()
        adminService.setMongoCachingQueries(false)
        assert !adminService.displayMongoCachingQueries()
    }

    @Test
    void "test display machine information"() {
        Map info = adminService.displayMachineInformation()
        assert info.osName
        assert info.osVersion
        assert info.osArch
        assert info.javaVersion
        assert info.freeMemory
        assert info.totalMemory
        assert info.maxMemory
        assert info.runningTime
    }

    @Test
    void "test display sessions"() {
        assert !adminService.displaySessions()
    }
}
