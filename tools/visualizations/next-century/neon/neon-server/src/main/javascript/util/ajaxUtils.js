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

neon.util.AjaxUtils = {};


/**
 * Utility methods for working with ajax calls
 * @namespace neon.util
 * @class AjaxUtils
 */

neon.util.AjaxUtils.overlayId_ = 'neon-overlay';
neon.util.AjaxUtils.logger_ = neon.util.LoggerUtils.getGlobalLogger();

// this logger is used specifically for error logging and should not be confused with the global logger which logs
// all ajax requests
neon.util.AjaxUtils.errorLogger_ = log4javascript.getLogger('neon.util.AjaxUtils.error');

/**
 * Asynchronously makes a post request to the specified URL
 * @method doPost
 * @param {String} url The URL to post the data to
 * @param {Object} opts See {{#crossLink "neon.util.AjaxUtils/doAjaxRequest"}}{{/crossLink}}
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.util.AjaxUtils.doPost = function (url, opts) {
    return this.doAjaxRequest('POST', url, opts);
};

/**
 * Asynchronously posts the object in its json form (it is converted to json in this method). This method
 * also assumes that if any data is returned it will be in json format
 * @method doPostJSON
 * @param {Object} object The object to post
 * @param {String} url The URL to post to
 * @param {Object} opts See {{#crossLink "neon.util.AjaxUtils/doAjaxRequest"}}{{/crossLink}}
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.util.AjaxUtils.doPostJSON = function (object, url, opts) {
    var data = JSON.stringify(object);
    var fullOpts = _.extend({}, opts, {data: data, contentType: 'application/json', responseType: 'json'});
    return this.doPost(url, fullOpts);
};

/**
 * Makes an ajax GET request
 * @method doGet
 * @param {String} url The url to get
 * @param {Object} opts See {{#crossLink "neon.util.AjaxUtils/doAjaxRequest"}}{{/crossLink}}
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.util.AjaxUtils.doGet = function (url, opts) {
    return this.doAjaxRequest('GET', url, opts);
};

/**
 * Sets the callbacks to be called when ajax requests start/stop. This is a good place to
 * setup the display/hiding of "working" indicators
 * @method setStartStopCallbacks
 * @param {Function} requestStart
 * @param {Function} requestEnd
 */
neon.util.AjaxUtils.setStartStopCallbacks = function (requestStart, requestEnd) {
    $(document).ajaxStart(requestStart);
    $(document).ajaxStop(requestEnd);
};

/**
 * Uses a default spinner when ajax queries are made. If this method is used, the neon.css file needs to be included.
 * @method useDefaultStartStopCallbacks
 */
neon.util.AjaxUtils.useDefaultStartStopCallbacks = function () {
    neon.util.AjaxUtils.setStartStopCallbacks(
        neon.util.AjaxUtils.showDefaultSpinner_,
        neon.util.AjaxUtils.hideDefaultSpinner_);
};


/**
 * Executes the request using an ajax call
 * @method doAjaxRequest
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
neon.util.AjaxUtils.doAjaxRequest = function (type, url, opts) {
    var params = {};
    params.type = type;
    params.url = url;

    // don't just do a blind copy of params here. we want to restrict what can be used to avoid any jquery specific options.
    params.data = opts.data;
    params.contentType = opts.contentType;
    params.dataType = opts.responseType;
    params.success = opts.success;
    params.error = opts.error;
    // set a default error behavior is none is specified
    if (!params.error) {
        params.error = function (xhr, status, error) {
            neon.util.AjaxUtils.errorLogger_.error(xhr, status, error);
        };
    }
    neon.util.AjaxUtils.logRequest_(params);
    var xhr = $.ajax(params);
    return new neon.util.AjaxRequest(xhr);
};

neon.util.AjaxUtils.logRequest_ = function (params) {
    neon.util.AjaxUtils.logger_.debug('Making', params.type, 'request to URL', params.url, 'with data', params.data);
};


neon.util.AjaxUtils.showDefaultSpinner_ = function () {
    $('body').append($('<div>').attr('id', neon.util.AjaxUtils.overlayId_).addClass('overlay-container'));
    $('#' + neon.util.AjaxUtils.overlayId_)
        .append($('<div>').addClass('overlay'));
    $('#' + neon.util.AjaxUtils.overlayId_).append($('<div>').addClass('spinner'));
};

neon.util.AjaxUtils.hideDefaultSpinner_ = function () {
    $('#' + neon.util.AjaxUtils.overlayId_).remove();
};

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

(function () {
    neon.util.LoggerUtils.useBrowserConsoleAppender(neon.util.AjaxUtils.errorLogger_, true);
})();