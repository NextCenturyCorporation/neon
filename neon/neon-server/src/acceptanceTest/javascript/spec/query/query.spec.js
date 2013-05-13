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
neon.query.SERVER_URL = 'http://localhost:10002/neon';

describe('query mapping', function () {
    // aliases for easier test writing
    var where = neon.query.where;
    var or = neon.query.or;
    var and = neon.query.and;


    var dataSourceName = 'acceptanceTest';
    var datasetId = 'records';
    var allData;

    var dcStateFilter = baseFilter().where('state', '=', 'DC');

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
        executeAndWait(neon.query.getFieldNames, dataSourceName, datasetId);
        var expected = ['_id', 'firstname', 'lastname', 'city', 'state', 'salary', 'hiredate', 'location'];
        runs(function () {
            expect(currentResult.fieldNames).toBeArrayWithSameElements(expected);
        });

    });

    it('query all data', function () {
        assertQueryResults(baseQuery(), allData);
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
            .aggregate(neon.query.COUNT, null, 'counter');
        var expectedData = getJSONFixture('groupByStateAsc_count.json');
        assertQueryResults(query, expectedData);
    });


    it('distinct fields', function () {
        var query = baseQuery().distinct('state');
        var expectedData = ["DC", "MD", "VA"];
        executeAndWait(neon.query.executeQuery, query);
        runs(function () {
            // distinct fields are in no particular order
            expect(currentResult.data).toBeArrayWithSameElements(expectedData);
        });
    });

    it('set selection WHERE', function () {
        var filter = baseFilter().where('state', '=', 'DC');
        var expectedData = rows(1, 2, 5);
        executeAndWait(neon.query.setSelectionWhere, filter);
        runs(function () {
            assertAsync(neon.query.getSelectionWhere, baseFilter(), expectedData);
        });

    });

    it('set selection by id', function () {
        var expectedData = rows(1, 2);
        var ids = expectedData.map(function (row) {
            return row._id;
        });

        executeAndWait(neon.query.setSelectedIds, ids);
        runs(function () {
            assertAsync(neon.query.getSelectionWhere, baseFilter(), expectedData);
        });
    });

    it('add remove selection ids', function () {
        var expectedData = rows(1, 2, 5);
        var ids = expectedData.map(function (row) {
            return row._id;
        });
        executeAndWait(neon.query.addSelectedIds, ids);
        runs(function () {
            executeAndWait(neon.query.getSelectionWhere, baseFilter());
            runs(function () {
                expect(currentResult.data).toBeEqualArray(expectedData);
                // remove the items from the expectations since they will be removed from the selected ids
                var removedId1 = ids.splice(2, 1)[0];
                expectedData.splice(2, 1);
                var removedId2 = ids.splice(0, 1)[0];
                expectedData.splice(0, 1);
                executeAndWait(neon.query.removeSelectedIds, [removedId1, removedId2]);
                runs(function () {
                    executeAndWait(neon.query.getSelectionWhere, baseFilter());
                    runs(function () {
                        expect(currentResult.data).toBeEqualArray(expectedData);
                    });
                });
            });

        });
    });

    it('clear selection', function () {
        var ids = rows(1, 2).map(function (row) {
            return row._id;
        });
        // adding ids already been tested, so we can be confident the ids are added properly
        executeAndWait(neon.query.addSelectedIds, ids);
        runs(function () {
            executeAndWait(neon.query.clearSelection);
            runs(function () {
                executeAndWait(neon.query.getSelectionWhere, baseFilter());
                runs(function () {
                    // no results should be returned from the selection query since the selection was cleared
                    expect(currentResult.data.length).toEqual(0);
                });
            });
        });
    });

    it('apply and remove filter', function () {
        executeAndWait(neon.query.addFilter, dcStateFilter);
        runs(function () {
            var dcFilterId = currentResult.addedIds[0];
            var expectedData = rows(1, 2, 5);
            assertQueryResults(baseQuery(), expectedData);

            // verify that if the query is supposed to include the filtered data, all data is returned
            runs(function () {
                assertQueryResults(baseQuery().includeFiltered(true), allData);
                runs(function () {
                    // apply another filter and make sure both are applied
                    var salaryFilter = baseFilter().where('salary', '>', 85000);
                    executeAndWait(neon.query.addFilter, salaryFilter);
                    runs(function () {
                        var salaryFilterId = currentResult.addedIds[0];
                        expectedData = rows(2, 5);
                        assertQueryResults(baseQuery(), expectedData);

                        // remove each filter and re-execute the queries
                        runs(function () {
                            executeAndWait(neon.query.removeFilter, salaryFilterId);
                            runs(function () {
                                expectedData = rows(1, 2, 5);
                                assertQueryResults(baseQuery(), expectedData);
                                runs(function () {
                                    executeAndWait(neon.query.removeFilter, dcFilterId);
                                    runs(function () {
                                        assertQueryResults(baseQuery(), allData);
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    });

    it('clear filters', function () {
        executeAndWait(neon.query.addFilter, dcStateFilter);
        runs(function () {
            executeAndWait(neon.query.clearFilters);
            runs(function () {
                assertQueryResults(baseQuery(), allData);
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

    it('transforms query results with a RESTful service', function () {
        // the port comes from build.gradle
        var host = 'http://localhost:10008';
        var path = '/neon/transformtest?replacethis=VA&replacewith=Virginia';
        var transformClassName = 'com.ncc.neon.query.transform.RestServiceTransform';
        var transformParams = [host, path];

        var query = baseQuery().where('state', '=', 'VA').transform(transformClassName, transformParams);
        executeAndWait(neon.query.executeQuery, query);
        runs(function () {
            expect(currentResult.data.length).toBe(4);
            // the state should be converted from VA to Virginia
            currentResult.data.forEach(function (row) {
                expect(row.state).toEqual('Virginia');
            });
        });
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
        queryGroup.addQuery('Virginia', query1);
        queryGroup.addQuery('Maryland', query2);
        queryGroup.addQuery('DistrictOfColumbia', query3);


        var expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults(queryGroup, expectedData);
    });

    it('transforms query group results with a RESTful service', function () {
        // the port comes from build.gradle
        var host = 'http://localhost:10008';
        var path = '/neon/transformtest?replacethis=Virginia&replacewith=VirginiaState';
        var transformClassName = 'com.ncc.neon.query.transform.RestServiceTransform';
        var transformParams = [host, path];

        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery('Virginia', query1);
        queryGroup.addQuery('Maryland', query2);
        queryGroup.addQuery('DistrictOfColumbia', query3);
        queryGroup.transform(transformClassName, transformParams);

        executeAndWait(neon.query.executeQueryGroup, queryGroup);
        runs(function () {
            expect(currentResult.data.length).toBe(1);
            // the state should be converted from Virginia to VirginiaState
            expect(currentResult.data[0].Virginia).toBeUndefined();
            expect(currentResult.data[0].VirginiaState).toBeDefined();
        });
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
        argsArray = argsArray.concat(neon.util.ArrayUtils.argumentsToArray(arguments).slice(1));
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
        neon.util.ArrayUtils.argumentsToArray(arguments).forEach(function (index) {
            data.push(allData[index]);
        });
        return data;
    }

    /**
     * Returns a query that is configured to select from the test data source
     * @return {neon.query.Query}
     */
    function baseQuery() {
        return new neon.query.Query().selectFrom(dataSourceName, datasetId);
    }

    /**
     * Returns a filter that is configured to select from the test data source
     * @return {neon.query.Filter}
     */
    function baseFilter() {
        return new neon.query.Filter().selectFrom(dataSourceName, datasetId);
    }

});