/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

/**
 * Utility methods for working with ajax calls
 * @class neon.util.ajaxUtils
 * @static
 */

neon.util.ajaxUtils = (function(){
    var overlayId = 'neon-overlay';
    var logger = neon.util.loggerUtils.getGlobalLogger();
    var errorLogger = neon.util.loggerUtils.getLogger('neon.util.ajaxUtils.error');

    neon.util.loggerUtils.useBrowserConsoleAppender(errorLogger);
    //document ready is used here so that this call is not overwritten by other jquery includes
    $(function () {
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
        // set a default error behavior is none is specified
        if (!params.error) {
            params.error = function (xhr, status, error) {
                errorLogger.error(xhr, status, error);
            };
        }
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
    function doPost(url, opts){
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
        var fullOpts = _.extend({}, opts, {data: data, contentType: 'application/json', responseType: 'json'});
        return doPost(url, fullOpts);
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
        $('#' + overlayId).append($('<div>').addClass('overlay'));
        $('#' + overlayId).append($('<div>').addClass('spinner'));
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
        doGet: doGet,
        setStartStopCallbacks: setStartStopCallbacks,
        useDefaultStartStopCallbacks: useDefaultStartStopCallbacks
    };

})();

/**
 * Stores an ajax request that is in progress (returned by any of the ajax method calls in this class)
 * @class AjaxRequest
 * @param {Object} xhr The jquery xhr being wrapped
 * @constructor
 */
neon.util.AjaxRequest = function (xhr) {

    // this really just wraps a jquery xhr
    this.xhr = xhr;
};

/**
 * Cancels the request if it is in progress
 * @method cancel
 */
neon.util.AjaxRequest.prototype.cancel = function () {
    this.xhr.abort();
};
