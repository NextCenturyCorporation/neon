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
 * Utility methods for working with ajax calls
 * @class neon.util.ajaxUtils
 * @static
 */

neon.util.ajaxUtils = (function() {
    var overlayId = 'neon-overlay';
    var logger = neon.util.loggerUtils.getGlobalLogger();
    var errorLogger = neon.util.loggerUtils.getLogger('neon.util.ajaxUtils.error');

    neon.util.loggerUtils.useBrowserConsoleAppender(errorLogger);
    $(function() {
        //document ready is used here so that this call is not overwritten by other jquery includes
        useDefaultStartStopCallbacks();
    });

    /**
     * Executes the request using an ajax call
     * @method doAjaxRequest
     * @private
     * @param {String} type The type of request, e.g. <code>GET</code> or <code>POST</code>
     * @param {String} url The url of the request
     * @param {Object} opts An associative array of options to configure the call
     * <ul>
     *  <li>data: Any data to include with the request (typically for POST requests)</li>
     *  <li>contentType: The mime type of data being sent, such as <code>application/json</code></li>
     *  <li>responseType: The type of data expected as a return value, such as <code>json</code> or <code>text</code></li>
     *  <li>success: The callback function to execute on success. It will be passed the return value of the call</li>
     *  <li>async: If the call should be asynchronous (defaults to true)</li>
     *  <li>global: If the call should trigger global ajax handlers</li>
     *  <li>error: The callback function to execute on error. It will have 3 parameters - the xhr, a short error status message and the error message</li>
     * </ul>
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function doAjaxRequest(type, url, opts) {
        var params = {};
        params.type = type;
        params.url = url;

        // don't just do a blind copy of params here. we want to restrict what can be used to avoid any jquery specific options.
        params.data = opts.data;
        params.contentType = opts.contentType;
        params.dataType = opts.responseType;
        params.success = opts.success;
        params.error = opts.error;
        params.global = opts.global;
        params.async = opts.async;
        logRequest(params);
        var xhr = $.ajax(params);
        return new neon.util.AjaxRequest(xhr);
    }

    function logRequest(params) {
        logger.debug('Making', params.type, 'request to URL', params.url, 'with data', params.data);
    }

    /**
     * Asynchronously makes a post request to the specified URL
     * @method doPost
     * @param {String} url The URL to post the data to
     * @param {Object} opts See {{#crossLink "neon.util.ajaxUtils/doAjaxRequest"}}{{/crossLink}}
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function doPost(url, opts) {
        return doAjaxRequest('POST', url, opts);
    }

    /**
     * Asynchronously posts the object in its json form (it is converted to json in this method). This method
     * also assumes that if any data is returned it will be in json format
     * @method doPostJSON
     * @param {Object} object The object to post
     * @param {String} url The URL to post to
     * @param {Object} opts See {{#crossLink "neon.util.ajaxUtils/doAjaxRequest"}}{{/crossLink}}
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function doPostJSON(object, url, opts) {
        var data = JSON.stringify(object);
        var fullOpts = _.extend({}, opts, {
            data: data,
            contentType: 'application/json',
            responseType: 'json'
        });
        return doPost(url, fullOpts);
    }

    /**
     * Asynchronously posts binary data to the specified URL.
     * @method doPostBinary
     * @param {Blob} binary The binary data to be uploaded.
     * @param {String} url The URL to post to.
     * @param {Function} successCallback The function to call when the post successfuly completes.
     * This function takes the server's response as its parameter.
     * @param {Function} errorCallback The function to call when an error occurs. This function
     * takes the server's response as its parameter.
     */
    function doPostBinary(binary, url, successCallback, errorCallback) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', url);
        xhr.onload = function() {
            if(xhr.status === 200) {
                successCallback(xhr.response);
            } else {
                errorCallback(xhr.response);
            }
            hideDefaultSpinner();
        };
        showDefaultSpinner();
        xhr.send(binary);
    }

    /**
     * Makes an ajax GET request
     * @method doGet
     * @param {String} url The url to get
     * @param {Object} opts See {{#crossLink "neon.util.ajaxUtils/doAjaxRequest"}}{{/crossLink}}
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function doGet(url, opts) {
        return doAjaxRequest('GET', url, opts);
    }

    /**
     * Makes an ajax DELETE request
     * @method doDelete
     * @param {String} url The url to get
     * @param {Object} opts See {{#crossLink "neon.util.ajaxUtils/doAjaxRequest"}}{{/crossLink}}
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function doDelete(url, opts) {
        return doAjaxRequest('DELETE', url, opts);
    }

    /**
     * Sets the callbacks to be called when ajax requests start/stop. This is a good place to
     * setup the display/hiding of "working" indicators
     * @method setStartStopCallbacks
     * @param {Function} requestStart
     * @param {Function} requestEnd
     */
    function setStartStopCallbacks(requestStart, requestEnd) {
        $(document).ajaxStart(requestStart);
        $(document).ajaxStop(requestEnd);
    }

    function showDefaultSpinner() {
        $('body').append($('<div>').attr('id', overlayId).addClass('overlay-container'));
        $('#' + overlayId).append($('<div>').addClass('loader'));
    }

    function hideDefaultSpinner() {
        $('#' + overlayId).remove();
    }

    /**
     * Uses a default spinner when ajax queries are made.
     * If this method is used, the neon.css file needs to be included.
     * @method useDefaultStartStopCallbacks
     */
    function useDefaultStartStopCallbacks() {
        neon.util.ajaxUtils.setStartStopCallbacks(showDefaultSpinner, hideDefaultSpinner);
    }

    return {
        doPost: doPost,
        doPostJSON: doPostJSON,
        doPostBinary: doPostBinary,
        doGet: doGet,
        doDelete: doDelete,
        setStartStopCallbacks: setStartStopCallbacks,
        useDefaultStartStopCallbacks: useDefaultStartStopCallbacks
    };
})();

/**
 * Stores an ajax request that is in progress (returned by any of the ajax method calls in this class)
 * @class neon.util.AjaxRequest
 * @param {Object} xhr The jquery xhr being wrapped
 * @constructor
 */
neon.util.AjaxRequest = function(xhr) {
    // this really just wraps a jquery xhr
    this.xhr = xhr;
};

/**
 * Aborts the request if it is in progress. This will call the error/fail handler with status set to
 * 0.
 * @method abort
 */
neon.util.AjaxRequest.prototype.abort = function() {
    this.xhr.abort();
    return this;
};

/**
 * Takes a function that will be called if/when the request is successful.
 *
 * @method done
 * @param {Function} callback
 */
neon.util.AjaxRequest.prototype.done = function(callback) {
    this.xhr.done(callback);
    return this;
};

/**
 * Takes a function that will be called if/when the request fails.
 *
 * @method fail
 * @param {Function} callback
 */
neon.util.AjaxRequest.prototype.fail = function(callback) {
    this.xhr.fail(callback);
    return this;
};

/**
 * Takes a function that will be called if/when the request completes, whether it succeeds or fails
 *
 * @method always
 * @param {Function} callback
 */
neon.util.AjaxRequest.prototype.always = function(callback) {
    this.xhr.always(callback);
    return this;
};
