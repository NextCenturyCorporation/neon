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
 /*global neontest*/

describe('neon.query.Filter', function() {
    var databaseName = 'db1';
    var tableName = 't1';

    var connection = new neon.query.Connection();
    var messenger = new neon.eventing.Messenger();

    var executeCallAndWait = function(name, functionObj, asyncFunction, params, test) {
        return neontest.executeAndWait(name, functionObj, asyncFunction, params, test);
    };

    beforeEach(function(done) {
        connection.connect(neon.query.Connection.MONGO, host);
        var filterClearComplete = false;

        messenger.clearFiltersSilently(function() {
            filterClearComplete = true;
            done();
        });
    });

    function assertAsyncResults(name, functionObj, asyncFunction, params, expectedData, test) {
        executeCallAndWait(name, functionObj, asyncFunction, params, function(result) {
            expect(result).toEqual(expectedData);
        });
    }

    var addFilters = function(done) {
        var filter = new neon.query.Filter().selectFrom("db1", "t1").name("filter").where("a", "=", 1);
        var filter2 = new neon.query.Filter().selectFrom("db1", "t2").name("filter2").where("b", "=", 2);
        var filter3 = new neon.query.Filter().selectFrom("db2", "t1").name("filter3").where("c", "=", 3);
        var filter4 = new neon.query.Filter().selectFrom("db2", "t3").name("filter4").where("d", "=", 4);

        messenger.addFilter(uuid(), filter, function() {
            messenger.addFilter(uuid(), filter2, function() {
                messenger.addFilter(uuid(), filter3, function() {
                    messenger.addFilter(uuid(), filter4, function() {
                        done();
                    });
                });
            });
        });
    };

    describe("getFilterState when filters exist", function() {
        beforeEach(function(done) {
            addFilters(done);
        });

        executeCallAndWait('should return all filters for table * and database *', this, neon.query.Filter.getFilterState, ["*", "*"], function(result) {
            expect(result.length).toBe(4);
        });

        executeCallAndWait('should return filters for all tables for table * and database', this, neon.query.Filter.getFilterState,
            [databaseName, "*"] ,function(result) {
            expect(result.length).toBe(2);
            expect(result[0].filter.databaseName).toBe(databaseName);
            expect(result[1].filter.databaseName).toBe(databaseName);
        });

        executeCallAndWait('should return filters for all databases for table and database *', this,
            neon.query.Filter.getFilterState, ["*", tableName], function(result) {
            expect(result.length).toBe(2);
            expect(result[0].filter.tableName).toBe(tableName);
            expect(result[0].filter.tableName).toBe(tableName);
        });

        executeCallAndWait('should return filters for database and table', this,
            neon.query.Filter.getFilterState, [databaseName, tableName],function(result) {
            expect(result.length).toBe(1);
            expect(result[0].filter.databaseName).toBe(databaseName);
            expect(result[0].filter.tableName).toBe(tableName);
        });
    });

    describe("getFilterState when no filters exist", function() {
        assertAsyncResults('should return empty array if no filters exist for table and database', this, neon.query.Filter.getFilterState, [databaseName, tableName], []);

        assertAsyncResults('should return empty array if no filters exist for table * and database', this, neon.query.Filter.getFilterState, [databaseName, "*"], []);

        assertAsyncResults('should return empty array if no filters exist for table and database *', this, neon.query.Filter.getFilterState, ["*", tableName], []);

        assertAsyncResults('should return empty array if no filters exist for table * and database *', this, neon.query.Filter.getFilterState, ["*", "*"], []);
    });
});