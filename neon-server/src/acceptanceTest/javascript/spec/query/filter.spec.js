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

/**
 * This is an end to end acceptance test to verify that queries can be executed against a mongo instance.
 * These tests parallel those in the MongoQueryExecutorIntegrationTest.
 */

 /*global host*/
 /*global getJSONFixture*/
 /*global neontest*/
describe('neon.query.Filter', function() {
    var databaseName = 'acceptanceTest';
    var tableName = 'records';
    // var allData;

    // var dcStateFilter = baseFilter().where('state', '=', 'DC');

    // jasmine.getJSONFixtures().fixturesPath = 'src/test-data';

    var connection = new neon.query.Connection();
    // var messenger = new neon.eventing.Messenger();
    // var filterId = "filterA";

    // helpers to run async tests against the correct objects
    var executeCallAndWait = function(functionObj, asyncFunction, params) {
        return neontest.executeAndWait(functionObj, asyncFunction, params);
    };

    // var sendMessageAndWait = function(asyncFunction, args) {
    //     return neontest.executeAndWait(messenger, asyncFunction, args);
    // };

    beforeEach(function() {
        // host is generated dynamically during the build and included in the acceptance test helper file
        connection.connect(neon.query.Connection.MONGO, host);
        connection.use(databaseName);

        // if (!allData) {
        //     allData = getJSONFixture('data.json');
        // }
    });

    /**
     * Returns a filter that is configured to select from the test data source
     * @return {neon.query.Filter}
     */
    // function baseFilter() {
    //     return new neon.query.Filter().selectFrom(databaseName, tableName);
    // }

    function assertAsyncResults(functionObj, asyncFunction, params, expectedData) {
        var result = executeCallAndWait(functionObj, asyncFunction, params);
        runs(function() {
            expect(result.get().data).toEqual(expectedData);
        });
    }

    describe("getFilterState", function() {
        it("should return empty results when no filters are set", function() {
            assertAsyncResults(neon.query.Filter, neon.query.Filter.getFilterState, [databaseName, tableName], []);
        });
    });
});