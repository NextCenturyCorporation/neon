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

import com.ncc.neon.query.filter.FilterKey
import com.ncc.neon.state.executor.DerbyExecutor

class StateServiceTest {

    private StateService stateService

    private final Map results = [
        "stateName-1": [
            "dashboardId": "dashboardStateId-1",
            "filterId": "filterStateId-1",
            "dashboard": [1, 2, 3],
            "dataset": [
                "a": 1,
                "b": 2
            ]
        ],
        "stateName-2": [
            "dashboardId": "dashboardStateId-2",
            "filterId": "filterStateId-2",
            "dashboard": [3, 2, 1],
            "dataset": [
                "c": 3,
                "d": 4
            ]
        ]
    ]


    @Before
    void setup() {
        stateService = new StateService()

        stateService.filterService = [
            getFilters: { databaseName, tableName ->
                assert databaseName == "*"
                assert tableName == "*"
                return []
            },
            clearFilters: {
                return
            },
            replaceFilter: { filter ->
                return
            }
        ] as FilterService

        stateService.derbyExecutor = setupDerbyExecutor()
    }

    @Test
    void "save state"() {
        Map<String, String> result = stateService.saveState("stateName-1", [:])
        assert result.dashboardStateId == "dashboardStateId-1"
        assert result.filterStateId == "filterStateId-1"

        result = stateService.saveState("", [:])
        assert result.dashboardStateId == "dashboardStateId-2"
        assert result.filterStateId == "filterStateId-2"
    }

    @Test
    void "load state"() {
        Map result = stateService.loadState("", "", "stateName-1")
        assert result.dashboardStateId == "dashboardStateId-1"
        assert result.filterStateId == "filterStateId-1"

        result = stateService.loadState("dashboardStateId-1", "filterStateId-2", "")
        assert result.dashboardStateId == "dashboardStateId-1"
        assert result.dashboard == [1, 2, 3]
        assert result.dataset == [
            "a": 1,
            "b": 2
        ]
        assert result.filterStateId == "filterStateId-2"
    }

    @Test
    void "state name"() {
        Map<String, String> result = stateService.stateName("dashboardStateId-1", "filterStateId-1")
        assert result.stateName == "stateName-1"

        result = stateService.stateName("dashboardStateId-1", "filterStateId-2")
        assert result == [:]

        result = stateService.stateName("", "filterStateId-2")
        assert result == [:]
    }

    @Test
    void "get all states names"() {
        List<String> result = stateService.getAllStatesNames()
        assert result.size() == 2
        assert result[0] == "stateName-1"
        assert result[1] == "stateName-2"
    }

    @Test
    void "delete state"() {
        Map result = stateService.deleteState("stateName-1")
        assert result == [:]
    }

    private DerbyExecutor setupDerbyExecutor() {
        return [
            getDashboardStateId: { stateName ->
                return results[stateName].dashboardId
            },
            getFilterStateId: { stateName ->
                return results[stateName].filterId
            },
            editDashboardState: { dashboardState, id ->
                return results["stateName-1"].dashboardId
            },
            saveDashboardState: { dashboardState, name ->
                return results["stateName-2"].dashboardId
            },
            editFilterState: { filters, id ->
                return results["stateName-1"].filterId
            },
            saveFilterState: { filters, name ->
                return results["stateName-2"].filterId
            },
            getDashboardState: { id ->
                return (results["stateName-1"].dashboardId == id ? results["stateName-1"] : results["stateName-2"])
            },
            getFilterState: { id ->
                return [new FilterKey()]
            },
            getDashboardStateName: { id ->
                return (results["stateName-1"].dashboardId == id ? "stateName-1" : "stateName-2")
            },
            getFilterStateName: { id ->
                return (results["stateName-1"].filterId == id ? "stateName-1" : "stateName-2")
            },
            getAllDashboardStateNames: { return new ArrayList<String>(results.keySet()) },
            getAllFilterStateNames: { return new ArrayList<String>(results.keySet()) },
            deleteState: { name ->
                return [:]
            }
        ] as DerbyExecutor
    }
}
