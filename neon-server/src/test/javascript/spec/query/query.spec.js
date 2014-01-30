

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