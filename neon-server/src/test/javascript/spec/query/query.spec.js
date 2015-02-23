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
    it('withFields should wrap a non-array single param in an array', function() {
        var query = new neon.query.Query();
        query.withFields('fieldA');
        expect(query.fields.length).toBe(1);
        expect(query.fields[0]).toBe('fieldA');
    });

    it('withFields should convert params to an array', function() {
        var query = new neon.query.Query();
        query.withFields('fieldA', 'fieldB', 'fieldC');
        expect(query.fields.length).toBe(3);
        expect(query.fields[0]).toBe('fieldA');
        expect(query.fields[1]).toBe('fieldB');
        expect(query.fields[2]).toBe('fieldC');
    });

    it('withFields should accept single param as an argument array', function() {
        var query = new neon.query.Query();
        var array = ['fieldA', 'fieldB', 'fieldC'];
        query.withFields(array);
        expect(query.fields.length).toBe(3);
        expect(query.fields[0]).toBe('fieldA');
        expect(query.fields[1]).toBe('fieldB');
        expect(query.fields[2]).toBe('fieldC');
    });

    it('groupBy should wrap a string group by clause in a single field clause', function() {
        var fieldName = 'test_field';
        var query = new neon.query.Query();
        query.groupBy(fieldName);
        expect(query.groupByClauses.length).toEqual(1);
        var wrapped = query.groupByClauses[0];
        expect(wrapped).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(wrapped.field).toBe(fieldName);
    });

    it('groupBy should pass a field function clause through directly', function() {
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(1);
        expect(groupByFunctionClause).toBe(query.groupByClauses[0]);
    });

    it('groupBy should allow multiple group by clauses from multiple params', function() {
        var fieldName = 'test_field';
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var query = new neon.query.Query();
        query.groupBy(fieldName, groupByFunctionClause);
        expect(query.groupByClauses.length).toEqual(2);

        expect(query.groupByClauses[0]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[0].field).toBe(fieldName);

        expect(groupByFunctionClause).toBe(query.groupByClauses[1]);
    });

    it('groupBy should allow multiple group by clauses from a single param as a string array', function() {
        var query = new neon.query.Query();
        var array = ['foo', 'bar', 'baz'];
        query.groupBy(array);
        expect(query.groupByClauses.length).toEqual(3);

        expect(query.groupByClauses[0]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[0].field).toBe('foo');
        expect(query.groupByClauses[1]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[1].field).toBe('bar');
        expect(query.groupByClauses[2]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[2].field).toBe('baz');
    });

    it('groupBy should allow multiple group by clauses from a single param as a mixed string and groupByClause array', function() {
        var query = new neon.query.Query();
        var groupByFunctionClause = new neon.query.GroupByFunctionClause('op','group_field','output_field');
        var array = ['foo', groupByFunctionClause, 'baz'];
        query.groupBy(array);
        expect(query.groupByClauses.length).toEqual(3);

        expect(query.groupByClauses[0]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[0].field).toBe('foo');
        expect(query.groupByClauses[1]).toBeInstanceOf(neon.query.GroupByFunctionClause);
        expect(query.groupByClauses[2]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[2].field).toBe('baz');
    });

    it('groupBy should replace existing groupBy clauses', function() {
        var query = new neon.query.Query();
        var array = ['foo', 'bar', 'baz'];
        query.groupBy(array);
        expect(query.groupByClauses.length).toEqual(3);

        expect(query.groupByClauses[0]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[0].field).toBe('foo');
        expect(query.groupByClauses[1]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[1].field).toBe('bar');
        expect(query.groupByClauses[2]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[2].field).toBe('baz');

        var array2 = ['ichi', 'ni', 'san', 'yon'];
        query.groupBy(array2);
        expect(query.groupByClauses.length).toEqual(4);

        expect(query.groupByClauses[0]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[0].field).toBe('ichi');
        expect(query.groupByClauses[1]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[1].field).toBe('ni');
        expect(query.groupByClauses[2]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[2].field).toBe('san');
        expect(query.groupByClauses[3]).toBeInstanceOf(neon.query.GroupBySingleFieldClause);
        expect(query.groupByClauses[3].field).toBe('yon');
    });

    it('aggregate should use a name if provided', function(){
        var query = new neon.query.Query();
        query.aggregate('op', 'foo', 'bar');
        expect(query.aggregates.length).toBe(1);
        expect(query.aggregates[0].name).toBe('bar');
    });

    it('aggregate should generate a name if none is provided', function() {
        var query = new neon.query.Query();
        query.aggregate('op', 'foo');
        expect(query.aggregates.length).toBe(1);
        expect(query.aggregates[0].name).toBe('op(foo)');
    });

    it('aggregate should append to existing aggregates', function() {
        var query = new neon.query.Query();
        query.aggregate('op', 'ichi', 'ni');
        expect(query.aggregates.length).toBe(1);
        query.aggregate('op2', 'san', 'yon');
        expect(query.aggregates.length).toBe(2);
    });

    it('sortBy should accept a single sort by clause from 2 params', function() {
        var query = new neon.query.Query();
        query.sortBy('ichi', 1);
        expect(query.sortClauses.length).toBe(1);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
    });

    it('sortBy should accept multiple sort by clauses from multiple params', function() {
        var query = new neon.query.Query();
        query.sortBy('ichi', 1, 'ni', -1);
        expect(query.sortClauses.length).toBe(2);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
        expect(query.sortClauses[1].fieldName).toBe('ni');
        expect(query.sortClauses[1].sortOrder).toBe(-1);
    });

    it('sortBy should accept multiple sort by clauses from a single param in the form of an array', function() {
        var query = new neon.query.Query();
        var array = ['ichi', 1, 'ni', -1];
        query.sortBy(array);
        expect(query.sortClauses.length).toBe(2);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
        expect(query.sortClauses[1].fieldName).toBe('ni');
        expect(query.sortClauses[1].sortOrder).toBe(-1);
    });

    it('sortBy should ignore the last item in an uneven number of params', function() {
        var query = new neon.query.Query();
        query.sortBy('ichi', 1, 'ni', -1, 'san');
        expect(query.sortClauses.length).toBe(2);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
        expect(query.sortClauses[1].fieldName).toBe('ni');
        expect(query.sortClauses[1].sortOrder).toBe(-1);
    });

    it('sortBy should ignore the last item in an uneven length argument array', function() {
        var query = new neon.query.Query();
        var array = ['ichi', 1, 'ni', -1, 'san'];
        query.sortBy(array);
        expect(query.sortClauses.length).toBe(2);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
        expect(query.sortClauses[1].fieldName).toBe('ni');
        expect(query.sortClauses[1].sortOrder).toBe(-1);
    });

    it('sortBy should replace the existing sort by clauses', function() {
        var query = new neon.query.Query();
        var array = ['ichi', 1, 'ni', -1];
        query.sortBy(array);
        expect(query.sortClauses.length).toBe(2);
        expect(query.sortClauses[0].fieldName).toBe('ichi');
        expect(query.sortClauses[0].sortOrder).toBe(1);
        expect(query.sortClauses[1].fieldName).toBe('ni');
        expect(query.sortClauses[1].sortOrder).toBe(-1);

        query.sortBy('san', -1);
        expect(query.sortClauses.length).toBe(1);
        expect(query.sortClauses[0].fieldName).toBe('san');
        expect(query.sortClauses[0].sortOrder).toBe(-1);
    });

    it('ignoreFilters should accept no params', function() {
        var query = new neon.query.Query();
        query.ignoreFilters();
        expect(query.ignoreFilters_).toBe(true);
    });

    it('ignoreFilters should accept a single filter from a single param', function() {
        var query = new neon.query.Query();
        query.ignoreFilters('ichi');
        expect(query.ignoredFilterIds_.length).toBe(1);
        expect(query.ignoredFilterIds_[0]).toBe('ichi');
    });

    it('ignoreFilters should accept multiple filters from multiple params', function() {
        var query = new neon.query.Query();
        query.ignoreFilters('ichi', 'ni', 'san');
        expect(query.ignoredFilterIds_.length).toBe(3);
        expect(query.ignoredFilterIds_[0]).toBe('ichi');
        expect(query.ignoredFilterIds_[1]).toBe('ni');
        expect(query.ignoredFilterIds_[2]).toBe('san');
    });

    it('ignoreFilters should accept multiple filters from a single param in the form of an array', function() {
        var query = new neon.query.Query();
        var array = ['ichi', 'ni', 'san'];
        query.ignoreFilters(array);
        expect(query.ignoredFilterIds_.length).toBe(3);
        expect(query.ignoredFilterIds_[0]).toBe('ichi');
        expect(query.ignoredFilterIds_[1]).toBe('ni');
        expect(query.ignoredFilterIds_[2]).toBe('san');
    });

    it('and should accept mutiple clauses from multiple params', function() {
        var where = neon.query.where('ichi', '=', 'ni');
        var where2 = neon.query.where('san', '!=', 'yon');
        var andClause = neon.query.and(where, where2);
        expect(andClause.type).toBe('and');
        expect(andClause.whereClauses.length).toBe(2);
        expect(andClause.whereClauses[0]).toBe(where);
        expect(andClause.whereClauses[1]).toBe(where2);
    });

    it('and should accept multiple clauses from a single param in the form of an array', function() {
        var where = neon.query.where('ichi', '=', 'ni');
        var where2 = neon.query.where('san', '!=', 'yon');
        var where3 = neon.query.where('go', '<', 'roku');
        var array = [where, where2, where3];
        var andClause = neon.query.and(array);
        expect(andClause.type).toBe('and');
        expect(andClause.whereClauses.length).toBe(3);
        expect(andClause.whereClauses[0]).toBe(where);
        expect(andClause.whereClauses[1]).toBe(where2);
        expect(andClause.whereClauses[2]).toBe(where3);
    });

    it('or should accept mutiple clauses from multiple params', function() {
        var where = neon.query.where('ichi', '=', 'ni');
        var where2 = neon.query.where('san', '!=', 'yon');
        var orClause = neon.query.or(where, where2);
        expect(orClause.type).toBe('or');
        expect(orClause.whereClauses.length).toBe(2);
        expect(orClause.whereClauses[0]).toBe(where);
        expect(orClause.whereClauses[1]).toBe(where2);
    });

    it('or should accept multiple clauses from a single param in the form of an array', function() {
        var where = neon.query.where('ichi', '=', 'ni');
        var where2 = neon.query.where('san', '!=', 'yon');
        var where3 = neon.query.where('go', '<', 'roku');
        var array = [where, where2, where3];
        var orClause = neon.query.or(array);
        expect(orClause.type).toBe('or');
        expect(orClause.whereClauses.length).toBe(3);
        expect(orClause.whereClauses[0]).toBe(where);
        expect(orClause.whereClauses[1]).toBe(where2);
        expect(orClause.whereClauses[2]).toBe(where3);
    });
});
