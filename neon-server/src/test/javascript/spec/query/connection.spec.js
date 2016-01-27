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
describe('neon.query.Connection', function() {
    var databaseName = "!@#$%^&*?/\\";
    var encodedDatabaseName = "!%40%23%24%25%5E%26*%3F%2F%5C";
    var encodedTableName = "%5C%2F%3F*%26%5E%25%24%23%40!";
    var encodedHost = "testHost%20%2F%20string";
    var host = 'testHost / string';
    var queryService = '/services/queryservice/';
    var server = 'http://localhost:8080/neon';
    var tableName = "\\/?*&^%$#@!";

    var connection;

    // Define this here because we use it multiple times and JShint complains if we have brackets without 
    // a new line
    var testData = {
        data: "testData"
    };

    beforeEach(function() {
        connection = new neon.query.Connection();
        connection.connect(neon.query.Connection.MONGO, host);
    });

    afterEach(function() {
        connection = null;
    });

    it("saves the server connection info.", function() {
        expect(connection.databaseType_).toBe(neon.query.Connection.MONGO);
        expect(connection.host_).toBe(host);
    });

    it("should encode host, database, and tablename appropriately in executeQuery request url", function() {
        spyOn($, "ajax");

        var query = new neon.query.Query().selectFrom(databaseName, tableName);
        connection.executeQuery(query);
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + queryService + 'query/' + encodedHost + '/' + neon.query.Connection.MONGO);

        // database and table are passed along in json and not url encoded.
        expect($.ajax.mostRecentCall.args[0].data.indexOf(databaseName)).toBeGreaterThan(-1);
        expect($.ajax.mostRecentCall.args[0].data.indexOf(tableName)).toBeGreaterThan(-1);
    });

    it("should encode host, database, and tablename appropriately in executeQueryGroup request url", function() {
        spyOn($, "ajax");

        var query = new neon.query.Query().selectFrom(databaseName, tableName);
        connection.executeQueryGroup(new neon.query.QueryGroup().addQuery(query));
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + queryService + 'querygroup/' + encodedHost + '/' + neon.query.Connection.MONGO);

        // database and table are passed along in json and not url encoded.
        expect($.ajax.mostRecentCall.args[0].data.indexOf(databaseName)).toBeGreaterThan(-1);
        expect($.ajax.mostRecentCall.args[0].data.indexOf(tableName)).toBeGreaterThan(-1);
    });

    it("should encode host, database, and tablename appropriately in getFieldNames request url", function() {
        spyOn($, "ajax");

        connection.getFieldNames(databaseName, tableName);
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + queryService + 'fields/' + encodedHost + '/' +
            neon.query.Connection.MONGO + '/' + encodedDatabaseName + '/' + encodedTableName);
    });

    it("should encode host, database, and tablename appropriately in getTableNames request url", function() {
        spyOn($, "ajax");

        connection.getTableNames(databaseName);
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + queryService + 'tablenames/' + encodedHost + '/' +
            neon.query.Connection.MONGO + '/' + encodedDatabaseName);
    });

    it("should encode host, database, and tablename appropriately in getDatabaseNames request url", function() {
        spyOn($, "ajax");

        connection.getDatabaseNames();
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + queryService + 'databasenames/' + encodedHost + '/' +
            neon.query.Connection.MONGO);
    });

    it("should encode host and database type appropriately in executeExport request url", function() {
        spyOn($, "ajax");

        connection.executeExport({
            data: "testData"
        }, null, null, "csv");
        expect($.ajax.mostRecentCall.args[0].data).toEqual('{"data":"testData","fileType":"csv"}');
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/exportservice/export/' +
            encodedHost + '/' + neon.query.Connection.MONGO);
    });

    it("should encode host, database type, and uuid appropriately in executeUploadFile request url", function() {
        spyOn(XMLHttpRequest.prototype, "open");
        spyOn(XMLHttpRequest.prototype, "send");

        connection.executeUploadFile(testData);
        expect(XMLHttpRequest.prototype.open).toHaveBeenCalledWith('POST', server + '/services/importservice/upload/' +
            encodedHost + '/' + neon.query.Connection.MONGO);
        expect(XMLHttpRequest.prototype.send).toHaveBeenCalledWith(testData);

        connection.executeUploadFile(testData, null, null, "differentHost", "differentDatabaseType");
        expect(XMLHttpRequest.prototype.open).toHaveBeenCalledWith('POST', server + '/services/importservice/upload/' +
            'differentHost/differentDatabaseType');
        expect(XMLHttpRequest.prototype.send).toHaveBeenCalledWith(testData);
    });

    it("should encode host, database type, and uuid appropriately in executeCheckTypeGuesses request url", function() {
        spyOn($, "ajax");

        connection.executeCheckTypeGuesses("1234");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/guesses/' +
            encodedHost + '/' + neon.query.Connection.MONGO + '/1234');

        connection.executeCheckTypeGuesses("1234", null, "differentHost", "differentDatabaseType");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/guesses/' +
            'differentHost/differentDatabaseType/1234');
    });

    it("should encode host, database type, and uuid appropriately in executeLoadFileIntoDB request url", function() {
        spyOn($, "ajax");

        connection.executeLoadFileIntoDB(testData, "1234");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/convert/' +
            encodedHost + '/' + neon.query.Connection.MONGO + '/1234');
        expect($.ajax.mostRecentCall.args[0].data).toEqual('{"data":"testData"}');

        connection.executeLoadFileIntoDB({}, "1234", null, null, "differentHost", "differentDatabaseType");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/convert/' +
            'differentHost/differentDatabaseType/1234');
        expect($.ajax.mostRecentCall.args[0].data).toEqual('{}');
    });

    it("should encode host, database type, and uuid appropriately in executeCheckImportProgress request url", function() {
        spyOn($, "ajax");

        connection.executeCheckImportProgress("1234");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/progress/' +
            encodedHost + '/' + neon.query.Connection.MONGO + '/1234');

        connection.executeCheckImportProgress("1234", null, "differentHost", "differentDatabaseType");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/progress/' +
            'differentHost/differentDatabaseType/1234');
    });

    it("should encode host, database type, user, and data appropriately in executeRemoveDataset request url", function() {
        spyOn($, "ajax");

        connection.executeRemoveDataset("testUser", "testData");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/drop/' +
            encodedHost + '/' + neon.query.Connection.MONGO + '?user=testUser&data=testData');

        connection.executeRemoveDataset("testUser", "testData", null, null, "differentHost", "differentDatabaseType");
        expect($.ajax.mostRecentCall.args[0].url).toEqual(server + '/services/importservice/drop/' +
            'differentHost/differentDatabaseType?user=testUser&data=testData');
    });
});
