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
 * Utility methods for working with widgets
 * @class neon.widget
 * @static
 */
neon.widget = (function() {
    /**
     * Save the current state of a widget.
     * @method saveState
     * @param {String} instanceId a unique identifier of an instance of a widget
     * @param {Object} stateObject an object that is to be saved.
     * @param {Function} successCallback The callback to execute when the state is saved. The callback will have no data.
     * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function saveState(instanceId, stateObject, successCallback, errorCallback) {
        var strObject = JSON.stringify(stateObject);
        return neon.util.ajaxUtils.doPostJSON({
            instanceId: instanceId,
            state: strObject
        }, neon.serviceUrl('widgetservice', 'savestate'), {
            success: successCallback,
            error: errorCallback,
            global: false
        });
    }

    /**
     * Gets the current state that has been saved.
     * @method getSavedState
     * @param {String} id an unique identifier of a client widget
     * @param {Function} successCallback The callback that contains the saved data.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function getSavedState(id, successCallback) {
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('widgetservice', 'restorestate/' + encodeURIComponent(id)), {
                success: function(data) {
                    if(!data) {
                        return;
                    }
                    if(successCallback && typeof successCallback === 'function') {
                        successCallback(data);
                    }
                },
                error: function() {
                    //Do nothing, the state does not exist.
                },
                responseType: 'json'
            }
        );
    }

    /**
     * Gets the list of property keys
     * @method getPropertyKeys
     * @param {Function} successCallback The callback that contains the array of key names
     * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function getPropertyKeys(successCallback, errorCallback) {
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('propertyservice', '*'), {
                success: successCallback,
                error: errorCallback,
                responseType: 'json'
            }
        );
    }

    /**
     * Gets a property
     * @method getProperty
     * @param {String} the property key to look up
     * @param {Function} successCallback The callback which takes the property as its argument, in an object of the form {key: "...", value: "..."}. If the property doesn't exist, the value field will be null
     * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function getProperty(key, successCallback, errorCallback) {
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('propertyservice', key), {
                success: successCallback,
                error: errorCallback,
                responseType: 'json'
            }
        );
    }

    /**
     * Remove a property
     * @method removeProperty
     * @param {String} the property key to remove
     * @param {Function} successCallback The callback
     * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function removeProperty(key, successCallback, errorCallback) {
        return neon.util.ajaxUtils.doDelete(
            neon.serviceUrl('propertyservice', key), {
                success: successCallback,
                error: errorCallback,
                responseType: 'json'
            }
        );
    }

    /**
     * Sets a property
     * @method setProperty
     * @param {String} the property key to set
     * @param {String} the new value for the property
     * @param {Function} successCallback The callback
     * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function setProperty(key, value, successCallback, errorCallback) {
        return neon.util.ajaxUtils.doPost(
            neon.serviceUrl('propertyservice', key), {
                data: value,
                contentType: 'text/plain',
                success: successCallback,
                error: errorCallback,
                responseType: 'json'
            }
        );
    }

    /**
     * Gets a unique id for a widget for a particular session. Repeated calls to this method in a single session with the
     * same parameters will result in the same id being returned.
     * @param {String} [qualifier] If a qualifier is specified, the id will be tied to that qualifier. This
     * allows multiple ids to be created for a single session. If a qualifier is not specified, the id returned
     * will be unique to the session.
     * If running within OWF, the OWF instanceId is appended to the identifier so the same widget can be reused in
     * multiple windows without conflict.
     * @param {Function} successCallback The callback that contains the unique id string
     * @method getInstanceId
     * @return {String} The xhr request object
     */
    function getInstanceId(qualifier, successCallback) {
        // If the first argument is a function, assume that is the callback and that the caller
        // wants the session id
        if(typeof qualifier === 'function') {
            successCallback = qualifier;
            qualifier = null;
        }
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('widgetservice', 'instanceid', buildInstanceIdQueryString(qualifier)), {
                success: function(instanceId) {
                    if(!instanceId) {
                        return;
                    }
                    if(successCallback && typeof successCallback === 'function') {
                        successCallback(instanceId);
                    }
                },
                error: function() {
                }
            }
        );
    }

    /**
     * Given the text qualifier value, if running in OWF, the OWF instance id is appended to it. Otherwise, the original
     * value is returned.
     * @method buildQualifierString
     * @param {String} [qualifier]
     * @return {string} The full qualifier, which may include the OWF instance id if running in OWF
     */
    function buildQualifierString(qualifier) {
        var fullQualifier = qualifier || '';
        // when running in OWF, it is possible to have the same widget running multiple times so append
        // the owf widget instanceid to the qualifier
        if(neon.util.owfUtils.isRunningInOWF()) {
            fullQualifier += OWF.getInstanceId();
        }
        return fullQualifier;
    }

    function buildInstanceIdQueryString(qualifier) {
        var queryString = '';
        if(qualifier) {
            queryString = 'qualifier=' + encodeURIComponent(buildQualifierString(qualifier));
        }
        return queryString;
    }

    return {
        saveState: saveState,
        getSavedState: getSavedState,
        getPropertyKeys: getPropertyKeys,
        getProperty: getProperty,
        setProperty: setProperty,
        removeProperty: removeProperty,
        getInstanceId: getInstanceId
    };
})();
