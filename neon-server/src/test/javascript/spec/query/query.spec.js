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



// some of this class is tested in the integration query.spec.js. This covers the unit tests.
describe('query', function() {

    it('should wrap a string group by clause in a single field clause', function() {
        var fieldName = 'test_field';
        var query = new neon.query.Query();
        query.groupBy(fieldName);
        expect(query.groupByClauses.length).toEqual(1);
        var wrapped = query.groupByClauses[0];
        verifySingleFieldClause(fieldName, wrapped);
    });

    it('should pass a field function clause through directly', function() {
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(1);
        verifyGroupByFunctionClause(groupByFunctionClause, query.groupByClauses[0]);
    });

    it('should allow multiple group by clauses', function() {
        var fieldName = 'test_field';
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(fieldName, groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(2);
        verifySingleFieldClause(fieldName, query.groupByClauses[0]);
        verifyGroupByFunctionClause(groupByFunctionClause, query.groupByClauses[1]);
    });

    function verifySingleFieldClause(fieldName, actual) {
        expect(actual).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(actual.field).toBe(fieldName);
    }

    function verifyGroupByFunctionClause(expected, actual) {
        expect(actual).toBe(expected);
    }

});