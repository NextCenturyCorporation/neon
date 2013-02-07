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
 * @namespace neon.util
 * @class AjaxUtils
 */
neon.namespace('neon.util.AjaxUtils');

neon.util.AjaxUtils.overlayId_ = 'neon-overlay';

/*jshint expr: true */
// these are set in the anonymous function at the end of this file
neon.util.AjaxUtils.ajaxStartCallback_;
neon.util.AjaxUtils.ajaxStopCallback_;
/*jshint expr: false */

/**
 * Asynchronously posts the data to the specified URL
 * @method doPost
 * @param {Object} data The data to post
 * @param {String} url The URL to post the data to
 * @param {String} contentType The type of data being sent (mime-type)
 * @param {String} responseType The type of data expected back
 * @param {Function} successCallback The function to call when the post successfully completes
 */
neon.util.AjaxUtils.doPost = function (data, url, contentType, responseType, successCallback) {
    this.doAjaxRequest_('POST', data, url, contentType, responseType, successCallback);
};

/**
 * Asynchronously posts the object in its json form (it is converted to json in this method). This method
 * also assumes that if any data is returned it will be in json format
 * @method doPostJSON
 * @param {Object} object The object to post
 * @param {String} url The URL to post to
 * @param {Function} successCallback The function to call when the post successfully completes
 */
neon.util.AjaxUtils.doPostJSON = function (object, url, successCallback) {
    var data = JSON.stringify(object);
    this.doPost(data, url, 'application/json', 'json', successCallback);
};

/**
 * Makes an ajax GET request
 * @method doGet
 * @param {String} url The url to get
 * @param {Function} successCallback The function to call when the GET request completes
 */
neon.util.AjaxUtils.doGet = function (url, successCallback) {
    this.doAjaxRequest_('GET', null, url, null, null, successCallback);
};

/**
 * Sets the callbacks to be called when ajax requests start/stop. This is a good place to
 * setup the display/hiding of "working" indicators
 * @method setStartStopCallbacks
 * @param {Function} requestStart
 * @param {Function} requestEnd
 */
neon.util.AjaxUtils.setStartStopCallbacks = function (requestStart, requestEnd) {
    neon.util.AjaxUtils.ajaxStartCallback_ = requestStart;
    neon.util.AjaxUtils.ajaxStopCallback_ = requestEnd;
};

neon.util.AjaxUtils.doAjaxRequest_ = function (type, data, url, contentType, responseType, successCallback) {
    var params = {};
    params.type = type;
    params.url = url;
    if (data) {
        params.data = data;
    }
    if (contentType) {
        params.contentType = contentType;
    }
    if (responseType) {
        params.dataType = responseType;
    }
    if (successCallback) {
        params.success = successCallback;
    }
    params.error = function (xhr, status, error) {
        console.log("error:");
        console.log(JSON.stringify(xhr));
        console.log(status);
        console.log(error);
    };

    $.ajax(params);
};


/**
 * The default method for showing an ajax spinner when none is provided
 * @method showDefaultSpinner
 */
neon.util.AjaxUtils.showDefaultSpinner = function () {
    $('body').append($('<div>').attr('id', neon.util.AjaxUtils.overlayId_).addClass('overlay-container'));
    $('#' + neon.util.AjaxUtils.overlayId_)
        .append($('<div>').addClass('overlay'));
    $('#' + neon.util.AjaxUtils.overlayId_).append($('<div>').addClass('spinner'));
};

/**
 * Removes the default ajax spinner when an ajax request completes
 * @method hideDefaultSpinner
 */
neon.util.AjaxUtils.hideDefaultSpinner = function () {
    $('#' + neon.util.AjaxUtils.overlayId_).remove();
};

(function () {
    neon.util.AjaxUtils.setStartStopCallbacks(
        neon.util.AjaxUtils.showDefaultSpinner,
        neon.util.AjaxUtils.hideDefaultSpinner);

    // wrap the callbacks so the user can changed the underlying function that is invoked
    $(document).ajaxStart(function () {
        neon.util.AjaxUtils.ajaxStartCallback_();
    });
    $(document).ajaxStop(function () {
        neon.util.AjaxUtils.ajaxStopCallback_();
    });
})();