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
/*global neontest*/
/*global host*/
/*global getJSONFixture*/
/*global neon */

describe('query mapping', function() {
    // aliases for easier test writing
    var where = neon.query.where;
    var or = neon.query.or;
    var and = neon.query.and;

    var databaseName = 'acceptanceTest';
    var tableName = 'records';
    var dcStateFilterId = "dcStateFilter";
    var salaryFilterId = "salaryFilterId";
    var allData;
    var query;
    var expected;
    var expectedData;

    var dcStateFilter = baseFilter().where('state', '=', 'DC');

    jasmine.getJSONFixtures().fixturesPath = 'src/test-data';

    var connection = new neon.query.Connection();
    var messenger = new neon.eventing.Messenger();
    var filterId = "filterA";

    // helpers to run async tests against the correct objects
    var executeQueryAndWait = function(name, asyncFunction, query, test) {
        return neontest.executeAndWait(name, connection, asyncFunction, query, test);
    };

    var sendMessageAndWait = function(name, asyncFunction, args, test) {
        return neontest.executeAndWait(name, messenger, asyncFunction, args, test);
    };

    beforeEach(function() {
        // host is generated dynamically during the build and included in the acceptance test helper file
        connection.connect(neon.query.Connection.MONGO, host);
        if(!allData) {
            allData = getJSONFixture('data.json');
        }
    });

    describe('get Field names', function() {
        var result = '';
        var expected = ['_id', 'firstname', 'lastname', 'city', 'state', 'salary', 'hiredate', 'location.coordinates', 'location.type', 'tags'];
        beforeEach(function(done) {
            connection.getFieldNames(databaseName, tableName, function(res) {
                result = res;
                done();
            }, function(err) {
                result = err;
                done();
            });
        });

        it('get field names', function() {
            expect(result).toBeArrayWithSameElements(expected);
        });
    });

    describe('queries', function() {
        beforeEach(function(done) {
            allData = getJSONFixture('data.json');
            done();
        });

        allData = getJSONFixture('data.json');
        assertQueryResults('query all data', baseQuery(), allData);

        var fields = ['firstname', 'lastname'];
        expected = [];
        allData = getJSONFixture('data.json');
        allData.forEach(function(row) {
            var expectedRow = {};
            // the _id field is always included from mongo
            expectedRow._id = row._id;
            fields.forEach(function(field) {
                // some rows do not have all fields, so skip those
                if(row[field]) {
                    expectedRow[field] = row[field];
                }
            });
            expected.push(expectedRow);
        });
        assertQueryResults('select subset of fields from result', baseQuery().withFields(fields[0], fields[1]), expected);

        expectedData = getJSONFixture('groupByMonth.json');
        var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
        // aggregate fields are automatically included in the select even though withFields is specified
        query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).withFields('hire_month');
        assertQueryResults('select derived field', query, expectedData);

        var whereStateClause = or(where('state', '=', 'VA'), where('state', '=', 'DC'));
        var salaryAndStateClause = and(where('salary', '>=', 100000), whereStateClause);
        query = baseQuery().where(salaryAndStateClause);
        assertQueryResults('query WHERE compound clause', query, rows(0, 2, 4));

        var whereLastNameNullClause = where('lastname', '=', null);
        query = baseQuery().where(whereLastNameNullClause);
        expectedData = getJSONFixture('null_or_missing.json');
        assertQueryResults('query WHERE', query, expectedData);

        var whereLastNameNotNullClause = where('lastname', '!=', null);
        query = baseQuery().where(whereLastNameNotNullClause);
        expectedData = getJSONFixture('not_null_and_not_missing.json');
        assertQueryResults('query WHERE field is not null and not missing', query, expectedData);

        query = baseQuery()
            .groupBy('state', 'city')
            .sortBy('state', neon.query.ASCENDING, 'city', neon.query.DESCENDING)
            .aggregate(neon.query.SUM, 'salary', 'salary_sum');
        expectedData = getJSONFixture('groupByStateAsc_cityDesc_aggregateSalary.json');
        assertQueryResults('group by and sort', query, expectedData);

        query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.AVG, 'salary', 'salary_avg');
        expectedData = getJSONFixture('groupByStateAsc_avgSalary.json');
        assertQueryResults('group by average', query, expectedData);

        query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MAX, 'salary', 'salary_max');
        expectedData = getJSONFixture('groupByStateAsc_maxSalary.json');
        assertQueryResults('group by max', query, expectedData);

        query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MAX, 'salary');
        expectedData = getJSONFixture('groupByStateAsc_maxSalary_generated_field_name.json');
        assertQueryResults('group by with generated aggregate field name', query, expectedData);

        query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.MIN, 'salary', 'salary_min');
        expectedData = getJSONFixture('groupByStateAsc_minSalary.json');
        assertQueryResults('group by min', query, expectedData);

        query = baseQuery()
            .groupBy('state')
            .sortBy('state', neon.query.ASCENDING)
            .aggregate(neon.query.COUNT, '*', 'counter');
        expectedData = getJSONFixture('groupByStateAsc_count.json');
        assertQueryResults('group by count', query, expectedData);

        query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'counter');
        expectedData = getJSONFixture('count.json');
        assertQueryResults('count all fields', query, expectedData);
        jasmine.getJSONFixtures().load('count.json')
        expectedData = getJSONFixture('count.json');

//        query = baseQuery()
//            .aggregate(neon.query.COUNT, '*', 'test');
//        expectedData = lodash.clone(getJSONFixture('count.json'));
//        expectedData[0].test = expectedData[0].counter;
//        delete expectedData[0].counter;
//        assertQueryResults('count all fields with different name', query, expectedData);

        query = baseQuery()
            .aggregate(neon.query.COUNT, '*', 'counter');
        jasmine.getJSONFixtures().load('count.json')
        expectedData = getJSONFixture('count.json');
        assertQueryResults('count all fields', query, expectedData);

        // lastname has one record with no data, so count should return 1 less value
        query = baseQuery()
            .aggregate(neon.query.COUNT, 'lastname', 'counter');
        expectedData = getJSONFixture('count_missing_field.json');
        assertQueryResults('count field with missing value', query, expectedData);

        query = baseQuery().distinct().withFields('state').limit(2).sortBy('state', neon.query.ASCENDING);
        expectedData = getJSONFixture('distinct_limit.json');
        assertQueryResults('distinct fields', query, expectedData);
    });


    describe('apply and remove filter', function() {
        allData = getJSONFixture('data.json');
        sendMessageAndWait('create filter', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });
        expectedData = rows(1, 2, 5);
        assertQueryResults('verify filtered data', baseQuery(), expectedData);

        // verify that if the query is supposed to include the filtered data, all data is returned
        assertQueryResults('verify ignore filters', baseQuery().ignoreFilters(), allData);

        // apply another filter and make sure both are applied
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('set multiple filters', neon.eventing.Messenger.prototype.addFilter, [filterId, salaryFilter], function() {
            expect(true).toBe(true);
        });

        expectedData = rows(2, 5);
        assertQueryResults('verify multiple filters applied', baseQuery(), expectedData);

        // remove the filter key and re-execute the query
        sendMessageAndWait('removing filters', neon.eventing.Messenger.prototype.removeFilter, filterId, function() {
            expect(true).toBe(true);
        });

        assertQueryResults('verify filters removed', baseQuery(), allData);
    });

    describe('ignore specific filters', function() {
        sendMessageAndWait('created a filter', neon.eventing.Messenger.prototype.addFilter, [dcStateFilterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        // apply another filter and make sure both are applied
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('added another filter', neon.eventing.Messenger.prototype.addFilter, [salaryFilterId, salaryFilter], function() {
            expect(true).toBe(true);
        });

        assertQueryResults('returned results with that ignored the filters', baseQuery().ignoreFilters([dcStateFilterId, salaryFilterId]), allData);
        sendMessageAndWait('cleared filters', neon.eventing.Messenger.prototype.clearFilters, [], function() {
            expect(true).toBe(true);
        });

        assertQueryResults('returned unfiltered results from a query', baseQuery(), allData);
    });

    describe('clear filters', function() {
        sendMessageAndWait('created a filter', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        sendMessageAndWait('cleared filters', neon.eventing.Messenger.prototype.clearFilters, [], function() {
            expect(true).toBe(true);
        });

        assertQueryResults('returned unfiltered results from a query', baseQuery(), allData);
    });

    describe('replace filter', function() {
        sendMessageAndWait('added a filter', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        expectedData = rows(1, 2, 5);
        assertQueryResults('returned only rows that pass the filter', baseQuery(), expectedData);

        // apply another filter and make sure both are applied
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('replace the filter', neon.eventing.Messenger.prototype.replaceFilter, [filterId, salaryFilter], function() {
            expect(true).toBe(true);
        });

        expectedData = rows(0, 2, 4, 5, 6);
        assertQueryResults('returned only rows that pass the new filter', baseQuery(), expectedData);

        // remove the filter key and re-execute the query
        sendMessageAndWait('removed the filter', neon.eventing.Messenger.prototype.removeFilter, filterId, function() {
            expect(true).toBe(true);
        });

        assertQueryResults('returned non filtered results from a query', baseQuery(), allData);
    });


    describe('apply and remove selection', function() {
        sendMessageAndWait('added a selection', neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        var expectedData = rows(1, 2, 5);
        assertQueryResults('returned only selected items', baseQuery().selectionOnly(), expectedData);

        // verify that we can still get back all data the data
        assertQueryResults('returned all data when not asked for a selection', baseQuery().ignoreFilters(), allData);

        // apply another selection and make sure both are applied
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('added another selection', neon.eventing.Messenger.prototype.addSelection, [filterId, salaryFilter], function() {
            expect(true).toBe(true);
        });

        expectedData = rows(2, 5);
        assertQueryResults('returned the intersection of the two selections', baseQuery().selectionOnly(), expectedData);

        // remove the selection and re-execute the query
        sendMessageAndWait('removed the selection', neon.eventing.Messenger.prototype.removeSelection, filterId, function() {
            expect(true).toBe(true);
        });

        assertQueryResults('removed selections successfully', baseQuery().selectionOnly(), []);
    });

    describe('clear selection', function() {
        // The first two commands are just test setup to get the server ready for the assert,
        // so their expectations are set to pass.
        sendMessageAndWait('added a selection', neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        sendMessageAndWait('cleared the selection', neon.eventing.Messenger.prototype.clearSelection, [], function() {
            expect(true).toBe(true);
        });

        assertQueryResults('cleared selections successfully', baseQuery().selectionOnly(), []);
    });

    describe('replace selection', function() {
        sendMessageAndWait('created a selection', neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter], function() {
            expect(true).toBe(true);
        });

        var expectedData = rows(1, 2, 5);
        assertQueryResults('returned only selected items', baseQuery().selectionOnly(), expectedData);

        // replace filter and make sure new one is applied.
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('replaced the selection', neon.eventing.Messenger.prototype.replaceSelection, [filterId, salaryFilter], function() {
            expect(true).toBe(true);
        });

        expectedData = rows(0, 2, 4, 5, 6);
        assertQueryResults('returned only the newly selected items', baseQuery().selectionOnly(), expectedData);

        // remove the selection and re-execute the query
        sendMessageAndWait('removed the selection', neon.eventing.Messenger.prototype.removeSelection, filterId, function() {
            expect(true).toBe(true);
        });

        assertQueryResults('returned no results for the selection', baseQuery().selectionOnly(), []);
    });


    describe('sends filter events to the callback functions', function() {
        sendMessageAndWait('sent an add event', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function(result) {
            assertEventType("ADD", result);
        });

        // replace filter and make sure new one is applied.
        var salaryFilter = baseFilter().where('salary', '>', 85000);
        sendMessageAndWait('sent a replace event', neon.eventing.Messenger.prototype.replaceFilter, [filterId, salaryFilter], function(result) {
            assertEventType("REPLACE", result);
        });

        // remove the filter key and re-execute the query
        sendMessageAndWait('sent a remove event', neon.eventing.Messenger.prototype.removeFilter, filterId, function(result) {
            assertEventType("REMOVE", result);
        });
    });

    groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
    query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING);
    expectedData = getJSONFixture('groupByMonth.json');
    assertQueryResults('group by month', query, expectedData);

    var groupByYearClause = new neon.query.GroupByFunctionClause(neon.query.YEAR, 'hiredate', 'hire_year');
    query = baseQuery().groupBy(groupByYearClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_year', neon.query.ASCENDING);
    expectedData = getJSONFixture('groupByYear.json');
    assertQueryResults('group by year', query, expectedData);

    var groupByMonthClause = new neon.query.GroupByFunctionClause(neon.query.MONTH, 'hiredate', 'hire_month');
     query = baseQuery().groupBy(groupByMonthClause).aggregate(neon.query.SUM, 'salary', 'salary_sum').sortBy('hire_month', neon.query.ASCENDING).limit(1);
    // we should only get the first element since we're limiting the query to 1 result
     expectedData = getJSONFixture('groupByMonth.json').slice(0, 1);
    assertQueryResults('group by with limit', query, expectedData);

    query = baseQuery().where('salary', '<', 61000);
    expectedData = rows(3, 7);
    assertQueryResults('query WHERE less than', query, expectedData);

    query = baseQuery().where('salary', '<=', 60000);
    expectedData = rows(3, 7);
    assertQueryResults('query WHERE less than or equal', query, expectedData);

    query = baseQuery().where('salary', '>', 118000);
    expectedData = rows(2);
    assertQueryResults('query WHERE greater than', query, expectedData);

    query = baseQuery().where('salary', '>=', 118000);
    expectedData = rows(2, 4);
    assertQueryResults('query WHERE greater than or equal', query, expectedData);

    query = baseQuery().where('state', '!=', 'VA');
    expectedData = rows(1, 2, 5, 6);
    assertQueryResults('query WHERE not equal', query, expectedData);

    query = baseQuery().where('state', 'in', ['MD', 'DC']);
    expectedData = rows(1, 2, 5, 6);
    assertQueryResults('query WHERE IN', query, expectedData);

    query = baseQuery().where('state', 'notin', ['VA', 'DC']);
    expectedData = rows(6);
    assertQueryResults('query WHERE not IN', query, expectedData);

    describe('should call an error callback if an error occurs', function() {
        var query = baseQuery().groupBy('unknown').aggregate('unknown', 'unknown', 'unknown');
        var callbacks = {};

        beforeEach(function(done) {
            callbacks.successCallback = function() { done(); };
            callbacks.errorCallback = function() { done(); };
            spyOn(callbacks, 'successCallback').and.callThrough();
            spyOn(callbacks, 'errorCallback').and.callThrough();
            connection.executeQuery(query, callbacks.successCallback, callbacks.errorCallback);
        });

        it('did not call the success handler', function() {
            expect(callbacks.successCallback).not.toHaveBeenCalled();
        });

        it('did call the error handler', function() {
            expect(callbacks.errorCallback).toHaveBeenCalled();
        });
    });

    query = baseQuery().limit(2);
    executeQueryAndWait('can limit query results', connection.executeQuery, query, function(result) {
        expect(result.data.length).toEqual(2);
    });

    query = baseQuery().sortBy('salary', neon.query.ASCENDING).offset(4);
    expectedData = getJSONFixture('offset.json');
    assertQueryResults('query with offset', query, expectedData);

    var center = new neon.util.LatLon(11.95, 19.5);
    query = baseQuery().withinDistance('location', center, 35, neon.query.MILE);
    expectedData = rows(2, 0);
    assertQueryResults('query near location', query, expectedData);

    center = new neon.util.LatLon(11.95, 19.5);
    var withinDistanceClause = neon.query.withinDistance('location', center, 35, neon.query.MILE);
    var dcStateClause = neon.query.where('state', '=', 'DC');
    var whereClause = neon.query.and(withinDistanceClause, dcStateClause);
    query = baseQuery().where(whereClause);
    expectedData = rows(2);
    assertQueryResults('query near location and filter on attributes', query, expectedData);

    var whereDateBetweenClause = and(where('hiredate', '>=', '2011-10-15T00:00:00Z'), where('hiredate', '<=', '2011-10-17T00:00:00Z'));
    query = baseQuery().where(whereDateBetweenClause);
    assertQueryResults('query with date clause as value', query, rows(1, 2));

    describe('concatenates the results of a query group', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults('verified concatenated results', queryGroup, expectedData);
    });

    describe('query group uses filters', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        sendMessageAndWait('added a filter', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function() {
            // Adding a tautology here since this call is made to setup data and jasmine complains if you don't pass
            // along an expectation.
            expect(true).toBe(true);
        });

        expectedData = getJSONFixture('queryGroupFiltered.json');
        assertQueryGroupResults('verified filtered group query results', queryGroup, expectedData);

        sendMessageAndWait('removed the filter', neon.eventing.Messenger.prototype.removeFilter, filterId, function() {
            // Adding a tautology here since this call is made to setup data and jasmine complains if you don't pass
            // along an expectation.
            expect(true).toBe(true);
        });

        expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults('verified non filtered group query results', queryGroup, expectedData);
    });

    describe('query group can ignore filters', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        queryGroup.ignoreFilters();
        sendMessageAndWait('added a filter', neon.eventing.Messenger.prototype.addFilter, [filterId, dcStateFilter], function() {
            // Adding a tautology here since this call is made to setup data and jasmine complains if you don't pass
            // along an expectation.
            expect(true).toBe(true);
        });

        expectedData = getJSONFixture('queryGroup.json');
        assertQueryGroupResults('verified the filters were ignored', queryGroup, expectedData);
        sendMessageAndWait('removed the filter', neon.eventing.Messenger.prototype.removeFilter, filterId, function() {
            // Adding a tautology here since this call is made to setup data and jasmine complains if you don't pass
            // along an expectation.
            expect(true).toBe(true);
        });
    });

    describe('query group uses selection', function() {
        var query1 = baseQuery().where('state', '=', 'VA');
        var query2 = baseQuery().where('state', '=', 'MD');
        var query3 = baseQuery().where('state', '=', 'DC');

        var queryGroup = new neon.query.QueryGroup();
        queryGroup.addQuery(query1);
        queryGroup.addQuery(query2);
        queryGroup.addQuery(query3);
        queryGroup.selectionOnly();
        sendMessageAndWait('filtered the dataset', neon.eventing.Messenger.prototype.addSelection, [filterId, dcStateFilter], function() {
            // Adding a tautology here since this call is made to setup data and jasmine complains if you don't pass
            // along an expectation.
            expect(true).toBe(true);
        });

        // queryGroupFiltered.json is used since we select the same data we filter on in the tests
        expectedData = getJSONFixture('queryGroupFiltered.json');
        assertQueryGroupResults('verified query group results', queryGroup, expectedData);

        sendMessageAndWait('removed the filter', neon.eventing.Messenger.prototype.removeSelection, filterId, function() {
            // Adding a tautology here since this call is made to setup data.
            expect(true).toBe(true);
        });
    });

    describe('transforms', function() {
        var transformedData = [];

        beforeEach(function(done) {
            allData = getJSONFixture('data.json');
            for(var i = 0; i < allData.length; i++) {
                transformedData[i] = allData[i];
                transformedData[i].foo = "bar";
            }
            done();
        });
        afterEach(function(done) {
            // Return the alldata object to its original state since object references to this may be cached
            // by other tests.
            for(var i = 0; i < allData.length; i++) {
                delete allData[i].foo;
            }
            done();
        });

        query = baseQuery();
        query.transform(new neon.query.Transform("Sample"));
        assertQueryResults('query with transform added by transform loader', query, transformedData);
    });

    /**
     * Executes the specified query and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryResults(name, query, expectedData) {
        doAssertQueryResults(name, connection.executeQuery, query, expectedData);
    }

    /**
     * Executes the specified query group and verifies that the results match the expected data
     * @param query
     * @param expectedData
     */
    function assertQueryGroupResults(name, query, expectedData) {
        doAssertQueryResults(name, connection.executeQueryGroup, query, expectedData);
    }

    function doAssertQueryResults(name, queryMethod, query, expectedData) {
        assertAsyncQueryResults(name, queryMethod, query, expectedData);
    }

    /**
     * Executes the specified async function with the given argument. The function must also take a second
     * argument with a callback to invoke when the operation completes and a third argument that is an error callback
     * @param asyncFunction The asynchronous function to call
     * @param query The query to pass to the function
     * @param expectedData The data expected to be returned from the function
     */
    function assertAsyncQueryResults(name, asyncFunction, query, expectedData) {
        executeQueryAndWait(name, asyncFunction, query, function(result) {
            expect(result.data).toEqual(expectedData);
        });
    }

    /**
     * Asserts that the event has the expected type and the dataset is correct
     * @param eventType
     * @param event
     * @method assertEventType
     */
    function assertEventType(eventType, event) {
        expect(event.type).toEqual(eventType);
        if(event.addedFilter) {
            expect(event.addedFilter.databaseName).toEqual(databaseName);
            expect(event.addedFilter.tableName).toEqual(tableName);
        }
        if(event.removedFilter) {
            expect(event.removedFilter.databaseName).toEqual(databaseName);
            expect(event.removedFilter.tableName).toEqual(tableName);
        }
    }

    /**
     * Creates an array from the entire dataset that has the specified rows
     * @param indices The indices of the rows from the original data
     * @return
     */
    function rows() {
        var data = [];
        neon.util.arrayUtils.argumentsToArray(arguments).forEach(function(index) {
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
});
