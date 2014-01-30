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

package com.ncc.neon.services

import com.ncc.neon.query.filter.*
import groovy.mock.interceptor.MockFor
import org.junit.Before
import org.junit.Test


class FilterServiceTest {

    private static final String UUID_STRING = "1af29529-86bb-4f2c-9928-7f4484b9cc49"
    private FilterService filterService
    private FilterKey filterKey
    private DataSet dataSet

    @Before
    void before() {
        filterService = new FilterService()
        dataSet = new DataSet(databaseName: "testDB", tableName: "testTable")
        filterKey = new FilterKey(uuid: UUID.fromString(UUID_STRING),
                dataSet: dataSet)
    }

    @Test
    void "register for filter key"() {
        FilterEvent event = filterService.registerForFilterKey(dataSet)
        assert event.dataSet == dataSet
        assert event.uuid
    }

    @Test
    void "replace filter"() {
        def filter = new Filter(databaseName: dataSet.databaseName, tableName: dataSet.tableName)

        def filterStateMock = new MockFor(FilterState)
        filterStateMock.demand.removeFilter { key -> assert key == filterKey }
        filterStateMock.demand.addFilter { key, f -> assert key == filterKey; f.is(filter)}
        def filterState = filterStateMock.proxyInstance()

        filterService.filterState = filterState
        filterService.replaceFilter(new FilterContainer(filterKey: filterKey, filter: filter))
        filterStateMock.verify(filterState)
    }

}
