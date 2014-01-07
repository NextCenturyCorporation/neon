/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

/**
 * This is an end to end acceptance test to verify that queries can be executed against a mongo instance.
 * These tests parallel those in the MongoQueryExecutorIntegrationTest.
 */
// neonServerUrl is generated dynamically during the build and included in the acceptance test helper file
neon.query.SERVER_URL = neonServerUrl;

describe('query mapping', function () {
    // aliases for easier test writing
    var where = neon.query.where;
    var or = neon.query.or;
    var and = neon.query.and;

    var databaseName = 'acceptanceTest';
    var tableName = 'records';
    var allData;

    var dcStateFilter = baseFilter().where('state', '=', 'DC');
    var filterKey = createFilterKey();
    jasmine.getJSONFixtures().fixturesPath = 'src/test-data';


    /** the result of any asynchronously executed function. this is reset after each test */
    var currentResult;

    beforeEach(function () {
        if (!allData) {
            allData = getJSONFixture('data.json');
        }
    });

    afterEach(function () {
        currentResult = undefined;
    });

    it('get field names', function () {
        executeAndWait(neon.query.getFieldNames, databaseName, tableName, "");
        var expected = ['_id', 'firstname', 'lastname', 'city', 'state', 'salary', 'hiredate', 'location'];
        runs(function () {
            expect(currentResult.data).toBeArrayWithSameElements(expected);
        });

    });

    it('query all data', function () {
        assertQueryResults(baseQuery(), allData);
    });


    it('select subset of fields from result', function () {
        var fields = ['firstname', 'lastname'];
        var expected = [];
        allData.forEach(function (row) {
            var expectedRow = {};
            // the _id field is always included from mongo
            expectedRow._id = row._id;
            fields.forEach(function (field) {
                // some rows do not have all fields, so skip those
                if ( row[field] ) {
                    expectedRow[field] = row[field];
                }
            });
            expected.push(expectedRow);
        });
        assertQueryResults(baseQuery().withFields(fields[0], fields[1]), expected);
    });

    it('select derived field', function () {
        var expectedData = getJSONFixture('groupByMonth.json');
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        // aggregate fields are automatically included in the select even though withFields is specified
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).withFields('hire_month');
        assertQueryResults(query, expectedData);
    });

    it('query WHERE', function () {
        var whereStateClause = or(where('state', '=', 'VA'), where('state', '=', 'DC'));
        var salaryAndStateClause = and(where('salary', '>=', 100000), whereStateClause);
        var query = baseQuery().where(salaryAndStateClause);
        assertQueryResults(query, rows(0, 2, 4));
    });

    it('group by and sort', function () {
        var query = baseQuery()
            .groupBy('state', 'city')
            .sortBy('state', neon.query.ASCENDING, 'city', neon.query.DESCENDING)
            .aggregate(neon.query.SUM, 'salary', 'salary_sum');
        var expectedData = getJSONFixture('groupByStateAsc_cityDesc_aggregateSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by average', function () {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.AVG, 'salary', 'salary_avg');
        var expectedData = getJSONFixture('groupByStateAsc_avgSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by max', function () {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MAX, 'salary', 'salary_max');
        var expectedData = getJSONFixture('groupByStateAsc_maxSalary.json');
        assertQueryResults(query, expectedData);
    });


    it('group by min', function () {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MIN, 'salary', 'salary_min');
        var expectedData = getJSONFixture('groupByStateAsc_minSalary.json');
        assertQueryResults(query, expectedData);
    });

    it('group by count', function () {
        var query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.COUNT, '*', 'counter');
        var expectedData = getJSONFixture('groupByStateAsc_count.json');
        assertQueryResults(query, expectedData);
    });

    it('count all fields', function () {
        var query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'counter');
        var expectedData = getJSONFixture('count.json');
        assertQueryResults(query, expectedData);
    });

    it('count field with missing value', function () {
        // lastname has one record with no data, so count should return 1 less value
        var query = baseQuery()
            .aggregate(neon.query.COUNT, 'lastname', 'counter');
        var expectedData = getJSONFixture('count_missing_field.json');
        assertQueryResults(query, expectedData);
    });


    it('distinct fields', function () {
        var query = baseQuery().distinct().withFields('state').limit(2).sortBy('state', neon.query.ASCENDING);
        var expectedData = getJSONFixture('distinct_limit.json');
        assertQueryResults(query, expectedData);
    });

    it('apply and remove filter', function () {
        executeAndWait(neon.query.addFilter, filterKey, dcStateFilter);
        runs(function () {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);

            // verify that if the query is supposed to include the filtered data, all data is returned
            runs(function () {
                assertQueryResults(baseQuery().allDataMode(), allData);
                runs(function () {
                    // apply another filter and make sure both are applied
                    var salaryFilter = baseFilter().where('salary', '>', 85000);
                    executeAndWait(neon.query.addFilter, filterKey, salaryFilter);
                    runs(function () {
                        expectedData = rows(2, 5);
                        assertQueryResults(baseQuery(), expectedData);
                        runs(function () {
                            // remove the filter key and re-execute the query
                            executeAndWait(neon.query.removeFilter, filterKey);
                            runs(function () {
                                assertQueryResults(baseQuery(), allData);
                            });
                        });
                    });
                });
            });
        });
    });

    it('clear filters', function () {
        executeAndWait(neon.query.addFilter, filterKey, dcStateFilter);
        runs(function () {
            executeAndWait(neon.query.clearFilters);
            runs(function () {
                assertQueryResults(baseQuery(), allData);
            });
        });
    });

    it('replace filter', function () {
        executeAndWait(neon.query.replaceFilter, filterKey, dcStateFilter);
        runs(function () {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);

            runs(function () {
                // replace filter and make sure new one is applied.
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                executeAndWait(neon.query.replaceFilter, filterKey, salaryFilter);
                runs(function () {
                    expectedData = rows(0, 2, 4, 5, 6);
                    assertQueryResults(baseQuery(), expectedData);
                    runs(function () {
                        // remove the filter key and re-execute the query
                        executeAndWait(neon.query.removeFilter, filterKey);
                        runs(function () {
                            assertQueryResults(baseQuery(), allData);
                        });
                    });
                });

            });
        });
    });

    it('apply and remove selection', function () {
        executeAndWait(neon.query.addSelection, filterKey, dcStateFilter);
        runs(function () {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery().selectionMode(), expectedData);

            // verify that we can still get back all data the data
            runs(function () {
                assertQueryResults(baseQuery().allDataMode(), allData);
                runs(function () {
                    // apply another selection and make sure both are applied
                    var salaryFilter = baseFilter().where('salary', '>', 85000);
                    executeAndWait(neon.query.addSelection, filterKey, salaryFilter);
                    runs(function () {
                        expectedData = rows(2, 5);
                        assertQueryResults(baseQuery().selectionMode(), expectedData);
                        runs(function () {
                            // remove the filter key and re-execute the query
                            executeAndWait(neon.query.removeSelection, filterKey);
                            runs(function () {
                                assertQueryResults(baseQuery().selectionMode(), allData);
                            });
                        });
                    });
                });
            });
        });
    });

    it('clear selection', function () {
        executeAndWait(neon.query.addSelection, filterKey, dcStateFilter);
        runs(function () {
            executeAndWait(neon.query.clearSelection);
            runs(function () {
                assertQueryResults(baseQuery().selectionMode(), allData);
            });
        });
    });

    it('replace selection', function () {
        executeAndWait(neon.query.replaceSelection, filterKey, dcStateFilter);
        runs(function () {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery().selectionMode(), expectedData);

            runs(function () {
                // replace filter and make sure new one is applied.
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                executeAndWait(neon.query.replaceSelection, filterKey, salaryFilter);
                runs(function () {
                    expectedData = rows(0, 2, 4, 5, 6);
                    assertQueryResults(baseQuery().selectionMode(), expectedData);
                    runs(function () {
                        // remove the filter key and re-execute the query
                        executeAndWait(neon.query.removeSelection, filterKey);
                        runs(function () {
                            assertQueryResults(baseQuery().selectionMode(), allData);
                        });
                    });
                });

            });
        });
    });

    it('replace filter and selection are independent', function () {
        executeAndWait(neon.query.replaceFilter, filterKey, dcStateFilter);
        runs(function () {
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);
            runs(function () {
                // add a selection and make sure both are applied.
                var salaryFilter = baseFilter().where('salary', '>', 85000);
                executeAndWait(neon.query.replaceSelection, filterKey, salaryFilter);
                runs(function () {
                    expectedData = rows(2, 5);
                    assertQueryResults(baseQuery().selectionMode(), expectedData);
                    runs(function () {
                        // remove the selection and re-execute the query
                        executeAndWait(neon.query.removeSelection, filterKey);
                        runs(function () {
                            expectedData = rows(1, 2, 5);
                            assertQueryResults(baseQuery().selectionMode(), expectedData);
                            runs(function () {
                                // remove the filter key from selection and re-execute the query
                                executeAndWait(neon.query.removeFilter, filterKey);
                                runs(function () {
                                    assertQueryResults(baseQuery().selectionMode(), allData);
                                });
                            });
                        });
                    });
                });

            });
        });
    });

    it('save and restore state', function () {
        executeAndWait(neon.query.saveState, "clientId", "state");
        runs(function () {
            executeAndWait(neon.query.getSavedState, "clientId");
            runs(function () {
                expect(currentResult).toEqual("state");
            });
        });
    });

    it('group by month', function () {
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING);
        var expectedData = getJSONFixture('groupByMonth.json');
        assertQueryResults(query, expectedData);
    });

    it('group by year', function () {
        var groupByYearClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, 'hiredate', 'hire_year');
        var query = baseQuery().groupBy(groupByYearClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_year', neon.query.ASCENDING);
        var expectedData = getJSONFixture('groupByYear.json');
        assertQueryResults(query, expectedData);
    });

    it('group by with limit', function () {
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        var query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).limit(1);
        // we should only get the first element since we're limiting the query to 1 result
        var expectedData = getJSONFixture('groupByMonth.json').slice(0, 1);
        assertQueryResults(query, expectedData);
    });


    it('query WHERE less than', function () {
        var query = baseQuery().where('salary', '<', 61000);
        var expectedData = rows(3, 7);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE less than or equal', function () {
        var query = baseQuery().where('salary', '<=', 60000);
        var expectedData = rows(3, 7);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE greater than', function () {
        var query = baseQuery().where('salary', '>', 118000);
        var expectedData = rows(2);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE greater than or equal', function () {
        var query = baseQuery().where('salary', '>=', 118000);
        var expectedData = rows(2, 4);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE not equal', function () {
        var query = baseQuery().where('state', '!=', 'VA');
        var expectedData = rows(1, 2, 5, 6);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE IN', function () {
        var query = baseQuery().where('state', 'in', ['MD', 'DC']);
        var expectedData = rows(1, 2, 5, 6);
        assertQueryResults(query, expectedData);
    });

    it('query WHERE not IN', function () {
        var query = baseQuery().where('state', 'notin', ['VA', 'DC']);
        var expectedData = rows(6);
        assertQueryResults(query, expectedData);
    });

    it('should call an error callback if an error occurs', function () {
        var query = baseQuery().groupBy('unknown').aggregate('unknown', 'unknown', 'unknown');
        var successCallback = jasmine.createSpy('success');
        var errorCallback = jasmine.createSpy('error');
        neon.query.executeQuery(query, successCallback, errorCallback);
        waitsFor(function () {
            return errorCallback.wasInvoked();
        });
        runs(function () {
            expect(successCallback).not.toHaveBeenCalled();
            expect(errorCallback).toHaveBeenCalled();
        });
    });

    it('query with limit', function () {
        var query = baseQuery().limit(2);
        executeAndWait(neon.query.executeQuery, query);
        runs(function () {
            expect(currentResult.data.length).toEqual(2);
        });
    });

    it('query with offset', function () {
        var query = baseQuery().sortBy('salary', neon.query.ASCENDING).offset(4);
        var expectedData = getJSONFixture('offset.json');
        assertQueryResults(query, expectedData);
    });

    it('query near location', function () {
        var center = new neon.util.LatLon(11.95, 19.5);
        var query = baseQuery().withinDistance('location', center, 35, neon.query.MILE);
        var expectedData = rows(2, 0);
        assertQueryResults(query, expectedData);
    });

    it('query near location and filter on attributes', function () {
        var center = new neon.util.LatLon(11.95, 19.5);
        var withinDistanceClause = neon.query.withinDistance('location', center, 35, neon.query.MILE);
        var dcStateClause = neon.query.where('state', '=', 'DC');
        var whereClause = neon.query.and(withinDistanceClause, dcStateClause);
        var query = baseQuery().where(whereClause);

        var expectedData = rows(2);
        assertQueryResults(query, expectedData);
    });

    it('query with date clause as value', function () {
        var whereDateBetweenClause = and(where('hiredate', '>=', '2011-10-15T00:00:00Z'), where('hiredate', '<=', '2011-10-17T00:00:00Z'));
        var query = baseQuery().where(whereDateBetweenClause);
        assertQueryResults(query, rows(1, 2));
    });

    it('concatenates the results of a query group', function () {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);


        var expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults(queryGroup, expectedData);
    });

    /**
     * Executes the specified query and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryResults(query, expectedData) {
        doAssertQueryResults(neon.query.executeQuery, query, expectedData);
    }

    /**
     * Executes the specified query group and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryGroupResults(query, expectedData) {
        doAssertQueryResults(neon.query.executeQueryGroup, query, expectedData);
    }


    function doAssertQueryResults(queryMethod, query, expectedData) {
        assertAsync(queryMethod, query, expectedData);
    }

    /**
     * Executes the specified async function with the given argument. The function must also take a second
     * argument with a callback to invoke when the operation completes and a third argument that is an error callback
     * @param asyncFunction The asynchronous function to call
     * @param arg The argument to pass to the function
     * @param expectedData The data expected to be returned from the function
     */
    function assertAsync(asyncFunction, arg, expectedData) {
        executeAndWait(asyncFunction, arg);
        runs(function () {
            expect(currentResult.data).toEqual(expectedData);
        });
    }

    /**
     * Executes the asynchronous function and blocks until it completes. The currentResult variable is set to the
     * value of the async call. All calls after this must be run in a "runs" block.
     * @param asyncFunction
     * @param args
     */
    function executeAndWait(asyncFunction, args) {
        var done = false;

        var argsArray = [];
        // the first argument is the function - the rest of the arguments are passed through to the function
        argsArray = argsArray.concat(neon.util.arrayUtils.argumentsToArray(arguments).slice(1));
        argsArray.push(function (res) {
            currentResult = res;
            done = true;
        });

        asyncFunction.apply(null, argsArray);
        waitsFor(function () {
            return done;
        });
    }

    /**
     * Creates an array from the entire dataset that has the specified rows
     * @param indices The indices of the rows from the original data
     * @return
     */
    function rows(indices) {
        var data = [];
        neon.util.arrayUtils.argumentsToArray(arguments).forEach(function (index) {
            data.push(allData[index]);
        });
        return data;
    }

    /**
     * Returns a query that is configured to select from the test data source
     * @return {neon.query.Query}
     */
    function baseQuery() {
        return new neon.query.Query().selectFrom(databaseName, tableName);
    }

    /**
     * Returns a filter that is configured to select from the test data source
     * @return {neon.query.Filter}
     */
    function baseFilter() {
        return new neon.query.Filter().selectFrom(databaseName, tableName);
    }

    function createFilterKey() {
        return {
            uuid: "84bc5064-c837-483b-8454-c8c72abe45f8",
            dataSet: {
                databaseName: databaseName,
                tableName: tableName
            }
        };
    }

});
