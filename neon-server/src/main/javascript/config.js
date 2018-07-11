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

/**
 * @class neon
 */

/**
 * The url of the query server. Defaults to http://localhost:8080/neon.
 * @property SERVER_URL
 * @type {String}
 */
neon.SERVER_URL = 'http://localhost:8080/neon';

neon.serviceUrl = function(servicePath, serviceName, queryParamsString) {
    var queryString = '';

    if(queryParamsString) {
        queryString = '?' + queryParamsString;
    }

    return neon.SERVER_URL + '/services/' + servicePath + '/' + serviceName + queryString;
};

neon.setNeonServerUrl = function(serverUrl) {
    neon.SERVER_URL = serverUrl;
};
