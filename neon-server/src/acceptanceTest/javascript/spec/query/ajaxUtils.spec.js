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
// neonServerUrl is generated dynamically during the build and included in the acceptance test helper file
neon.SERVER_URL = neonServerUrl;

describe('ajax utils', function () {

    it('cancels ajax request', function () {
        var statusText = "";
        // transformServiceUrl is generated dynamically during the build and included in the acceptance test helper file
        var request = neon.util.ajaxUtils.doGet(transformServiceUrl + '/neon/timeouttest', {
            // we should get an error with an "abort" status
            error: function (xhr, status, msg) {
                statusText = xhr.statusText;
            }
        });
        request.cancel();
        waitsFor(function () {
            return statusText === 'abort';
        });
    });

});