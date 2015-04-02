/*
 * Copyright 2014 Next Century Corporation
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
describe('filter', function () {

    it('splits a fully qualified table name', function () {
        var filter = new neon.query.Filter().selectFrom("dbName.dbTable");
        expect(filter.databaseName).toEqual("dbName");
        expect(filter.tableName).toEqual("dbTable");
    });

    it('selects both the database and table', function () {
        var filter = new neon.query.Filter().selectFrom("dbName", "dbTable");
        expect(filter.databaseName).toEqual("dbName");
        expect(filter.tableName).toEqual("dbTable");
    });

    it('selects only the table with a single argument', function () {
        // when a filter is used by a query, the query will automatically put the database name is, so that is
        // why this functionality exists
        var filter = new neon.query.Filter().selectFrom("dbTable");
        expect(filter.databaseName).toBeUndefined();
        expect(filter.tableName).toEqual("dbTable");
    });

    it('gets a field name from a filter', function() {
        var filter = '{"databaseName":"database","tableName":"table","whereClause":{"type":"where","lhs":"user","operator":"=","rhs":"abc"}}';
        var filterJsonObject = JSON.parse(filter);
        var fieldNames = neon.query.Filter.getFieldNames(filterJsonObject);
        expect(Object.keys(fieldNames).length).toEqual(1);
        expect(fieldNames["user"]).toEqual(true);
    });

    it('gets multiple field names from a filter', function() {
        var filter = '{"databaseName":"database","tableName":"table","whereClause":{"type":"and","whereClauses":[{"type":"where","lhs":"user","operator":"=","rhs":"abc"},{"type":"where","lhs":"date","operator":"=","rhs":"xyz"}]}}';
        var filterJsonObject = JSON.parse(filter);
        var fieldNames = neon.query.Filter.getFieldNames(filterJsonObject);
        expect(Object.keys(fieldNames).length).toEqual(2);
        expect(fieldNames["date"]).toEqual(true);
        expect(fieldNames["user"]).toEqual(true);
    });

    it('gets unique field names from a filter', function() {
        var filter = '{"databaseName":"database","tableName":"table","whereClause":{"type":"and","whereClauses":[{"type":"where","lhs":"user","operator":"=","rhs":"abc"},{"type":"where","lhs":"user","operator":"=","rhs":"xyz"}]}}';
        var filterJsonObject = JSON.parse(filter);
        var fieldNames = neon.query.Filter.getFieldNames(filterJsonObject);
        expect(Object.keys(fieldNames).length).toEqual(1);
        expect(fieldNames["user"]).toEqual(true);
    });
});
