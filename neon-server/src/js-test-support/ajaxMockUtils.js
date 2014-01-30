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
 * Utilities for mocking out ajax calls
 */

neon.mock.AjaxMockUtils = {};

/**
 * Mocks the next ajax call to execute the request's success callback with the given results
 * @method mockNextAjaxCall
 * @param {Object} results The results passed to the callback
 */
neon.mock.AjaxMockUtils.mockNextAjaxCall = function(results) {

    $.ajax = function(params) {
        params.success(results);
    };

};