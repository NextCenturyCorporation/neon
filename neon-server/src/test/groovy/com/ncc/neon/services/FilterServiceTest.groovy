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

    private static final String ID = "filterA"
    private FilterService filterService
    private FilterKey filterKey
    Filter filter

    @Before
    void before() {
        filterService = new FilterService()
        filter = new Filter(databaseName: "testDB", tableName: "testTable")
        filterKey = new FilterKey(id: ID, filter: filter)
    }

    @Test
    void "add filter"() {
        def filterStateMock = new MockFor(FilterState)
        filterStateMock.demand.addFilter { key -> assert key.is(filterKey)}
        def filterState = filterStateMock.proxyInstance()
        filterService.filterState = filterState
        FilterEvent event = filterService.addFilter(filterKey)
        filterStateMock.verify(filterState)
        assert event.type == "ADD"
        assert event.filter == filter
    }

    @Test
    void "remove filter"() {
        def filterStateMock = new MockFor(FilterState)
        filterStateMock.demand.removeFilter { id -> assert id == ID; return filterKey }
        def filterState = filterStateMock.proxyInstance()
        filterService.filterState = filterState
        FilterEvent event = filterService.removeFilter(ID)
        filterStateMock.verify(filterState)
        assert event.type == "REMOVE"
        assert event.filter == filter
    }

    @Test
    void "remove filter that does not exist"() {
        def filterStateMock = new MockFor(FilterState)
        filterStateMock.demand.removeFilter { id -> null }
        def filterState = filterStateMock.proxyInstance()
        filterService.filterState = filterState
        FilterEvent event = filterService.removeFilter(ID)
        filterStateMock.verify(filterState)
        assert event.type == "REMOVE"

        // explicitly check for empty string, not null
        assert event.filter.databaseName == ""
        assert event.filter.tableName == ""

    }


    @Test
    void "replace filter"() {
        def filterStateMock = new MockFor(FilterState)
        filterStateMock.demand.removeFilter { id -> assert id == ID }
        filterStateMock.demand.addFilter { key -> assert key.is(filterKey)}
        def filterState = filterStateMock.proxyInstance()
        filterService.filterState = filterState
        FilterEvent event = filterService.replaceFilter(filterKey)
        filterStateMock.verify(filterState)
        assert event.type == "REPLACE"
        assert event.filter == filter
    }

}
