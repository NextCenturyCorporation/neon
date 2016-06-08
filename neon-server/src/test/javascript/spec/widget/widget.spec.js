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

describe('neon.widget', function() {
    var encodedValue = "%23%40!%5C%2F%3F*%26%5E%25%24";
    var widgetService = '/services/widgetservice/';
    var server = 'http://localhost:8080/neon';
    var tableName = "\\/?*&^%$#@!";
    var value = "#@!\\/?*&^%$";

    it("saves the server connection info.", function() {
        expect(neon.widget).toBeDefined();
    });

    it("should encode qualifier appropriately in getInstanceId request url", function() {
        spyOn($, "ajax");

        neon.widget.getInstanceId(value);
        expect($.ajax.calls.mostRecent().args[0].url).toEqual(server + widgetService + 'instanceid?qualifier=' + encodedValue);
    });

    it("should encode qualifier appropriately in getSavedState request url", function() {
        spyOn($, "ajax");

        neon.widget.getSavedState(value);
        expect($.ajax.calls.mostRecent().args[0].url).toEqual(server + widgetService + 'restorestate/' + encodedValue);
    });

    it("should encode qualifier appropriately in saveState request url", function() {
        spyOn($, "ajax");

        neon.widget.saveState(tableName, value);
        expect($.ajax.calls.mostRecent().args[0].url).toEqual(server + widgetService + 'savestate');

        // data values are passed along in json via POST and not url encoded.  The value will be wrapped in an object
        // twice.
        expect($.ajax.calls.mostRecent().args[0].data.indexOf(tableName)).toBeGreaterThan(-1);
        expect($.ajax.calls.mostRecent().args[0].data.indexOf(JSON.stringify(JSON.stringify(value)))).toBeGreaterThan(-1);
    });
});
