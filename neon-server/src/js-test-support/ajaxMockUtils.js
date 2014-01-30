

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