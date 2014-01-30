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
