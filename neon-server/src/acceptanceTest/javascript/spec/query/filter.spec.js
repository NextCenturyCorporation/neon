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

    var executeCallAndWait = function(functionObj, asyncFunction, params) {
        return neontest.executeAndWait(functionObj, asyncFunction, params);
    };

    beforeEach(function() {
        connection.connect(neon.query.Connection.MONGO, host);
        var filterClearComplete = false;

        runs(function() {
            messenger.clearFiltersSilently(function() {
                filterClearComplete = true;
            });
        });

        waitsFor(function() {
            return filterClearComplete;
        });
    });

    function assertAsyncResults(functionObj, asyncFunction, params, expectedData) {
        var result = executeCallAndWait(functionObj, asyncFunction, params);
        runs(function() {
            expect(result.get()).toEqual(expectedData);
        });
    }

    var addFilters = function(callback) {
        var filter = new neon.query.Filter().selectFrom("db1", "t1").where("a", "=", 1);
        var filter2 = new neon.query.Filter().selectFrom("db1", "t2").where("b", "=", 2);
        var filter3 = new neon.query.Filter().selectFrom("db2", "t1").where("c", "=", 3);
        var filter4 = new neon.query.Filter().selectFrom("db2", "t3").where("d", "=", 4);

        neontest.executeAndWait(messenger, messenger.addFilter, [uuid(), filter]);
        runs(function() {
            neontest.executeAndWait(messenger, messenger.addFilter, [uuid(), filter2]);
            runs(function() {
                neontest.executeAndWait(messenger, messenger.addFilter, [uuid(), filter3]);
                runs(function() {
                    neontest.executeAndWait(messenger, messenger.addFilter, [uuid(), filter4]);
                    runs(function() {
                        callback();
                    });
                });
            });
        });
    };

    describe("getFilterState", function() {
        it('should return all filters for table * and database *', function() {
            runs(function() {
                addFilters(function() {
                    var result = executeCallAndWait(this, neon.query.Filter.getFilterState, ["*", "*"]);
                    runs(function() {
                        result = result.get();
                        expect(result.length).toBe(4);
                    });
                });
            });
        });

        it('should return filters for all tables for table * and database', function() {
            runs(function() {
                addFilters(function() {
                    var result = executeCallAndWait(this, neon.query.Filter.getFilterState, [databaseName, "*"]);
                    runs(function() {
                        result = result.get();
                        expect(result.length).toBe(2);
                        expect(result[0].databaseName).toBe(databaseName);
                        expect(result[1].databaseName).toBe(databaseName);
                    });
                });
            });
        });

        it('should return filters for all databases for table and database *', function() {
            runs(function() {
                addFilters(function() {
                    var result = executeCallAndWait(this, neon.query.Filter.getFilterState, ["*", tableName]);
                    runs(function() {
                        result = result.get();
                        expect(result.length).toBe(2);
                        expect(result[0].tableName).toBe(tableName);
                        expect(result[0].tableName).toBe(tableName);
                    });
                });
            });
        });

        it('should return filters for database and table', function() {
            runs(function() {
                addFilters(function() {
                    var result = executeCallAndWait(this, neon.query.Filter.getFilterState, [databaseName, tableName]);
                    runs(function() {
                        result = result.get();
                        expect(result.length).toBe(1);
                        expect(result[0].databaseName).toBe(databaseName);
                        expect(result[0].tableName).toBe(tableName);
                    });
                });
            });
        });

        it('should return empty array if no filters exist for table and database', function() {
            assertAsyncResults(this, neon.query.Filter.getFilterState, [databaseName, tableName], []);
        });

        it('should return empty array if no filters exist for table * and database', function() {
            assertAsyncResults(this, neon.query.Filter.getFilterState, [databaseName, "*"], []);
        });

        it('should return empty array if no filters exist for table and database *', function() {
            assertAsyncResults(this, neon.query.Filter.getFilterState, ["*", tableName], []);
        });

        it('should return empty array if no filters exist for table * and database *', function() {
            assertAsyncResults(this, neon.query.Filter.getFilterState, ["*", "*"], []);
        });
    });
});