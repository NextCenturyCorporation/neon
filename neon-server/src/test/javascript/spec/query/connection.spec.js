/*
 * Copyright 2015 Next Century Corporation
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
describe('neon.query.Connection', function () {

    var databaseName = "!@#$%^&*?/\\";
    var encodedDatabaseName = "!%40%23%24%25%5E%26*%3F%2F%5C";
    var encodedTableName = "%5C%2F%3F*%26%5E%25%24%23%40!";
    var encodedHost = "testHost%20%2F%20string";
    var host = 'testHost / string';
    var languageService = '/services/languageservice/query/';
    var queryService = '/services/queryservice/';
    var server = 'http://localhost:8080/neon';
    var tableName = "\\/?*&^%$#@!";

    var connection;

    beforeEach(function() {
        connection = new neon.query.Connection();
        connection.connect(neon.query.Connection.MONGO, host);
    });

    afterEach(function () {
        delete connection;
    });

    it("saves the server connection info.", function() {
        expect(connection.databaseType_).toBe(neon.query.Connection.MONGO);
        expect(connection.host_).toBe(host);
    });

    it("should make send text queries to the correct URL", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        connection.executeTextQuery("select * from someTable;");
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + languageService + encodedHost + '/' + neon.query.Connection.MONGO);
        expect($.ajax.mostRecentCall.args[0]["data"]["text"]).toBe('select * from someTable;');
    });

    it("should encode host, database, and tablename appropriately in executeQuery request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        var query = new neon.query.Query().selectFrom(databaseName, tableName);
        connection.executeQuery(query);
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'query/' + encodedHost + '/' + neon.query.Connection.MONGO);

        // database and table are passed along in json and not url encoded.
        expect($.ajax.mostRecentCall.args[0]["data"].indexOf(databaseName)).toBeGreaterThan(-1);
        expect($.ajax.mostRecentCall.args[0]["data"].indexOf(tableName)).toBeGreaterThan(-1);
    });

    it("should encode host, database, and tablename appropriately in executeQueryGroup request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        var query = new neon.query.Query().selectFrom(databaseName, tableName);
        connection.executeQueryGroup(new neon.query.QueryGroup().addQuery(query));
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'querygroup/' + encodedHost + '/' + neon.query.Connection.MONGO);

        // database and table are passed along in json and not url encoded.
        expect($.ajax.mostRecentCall.args[0]["data"].indexOf(databaseName)).toBeGreaterThan(-1);
        expect($.ajax.mostRecentCall.args[0]["data"].indexOf(tableName)).toBeGreaterThan(-1);
    });

    it("should encode host, database, and tablename appropriately in getFieldNames request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        connection.getFieldNames(tableName);
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'fields/' + encodedHost + '/' + 
            neon.query.Connection.MONGO + '/' + encodedDatabaseName + '/' + encodedTableName);
    });

    it("should encode host, database, and tablename appropriately in getTableNames request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        connection.getTableNames();
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'tablenames/' + encodedHost + '/' + 
            neon.query.Connection.MONGO + '/' + encodedDatabaseName);
    });

    it("should encode host, database, and tablename appropriately in getDatabaseNames request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        connection.getDatabaseNames();
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'databasenames/' + encodedHost + '/' + 
            neon.query.Connection.MONGO);
    });

    it("should encode host, database, and tablename appropriately in getColumnMetadata request url", function() {
        spyOn($, "ajax");
        connection.use(databaseName);

        connection.getColumnMetadata(tableName);
        expect($.ajax.mostRecentCall.args[0]["url"]).toEqual(server + queryService + 'columnmetadata/' + 
            encodedDatabaseName + '/' + encodedTableName);
    });
});