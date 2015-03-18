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
     * Gets a unique id for a widget for a particular session. Repeated calls to this method in a single session with the
     * same parameters will result in the same id being returned. Note this method is executed synchronously.
     * @param {String} [qualifier] If a qualifier is specified, the id will be tied to that qualifier. This
     * allows multiple ids to be created for a single session. If a qualifier is not specified, the id returned
     * will be unique to the session.
     * If running within OWF, the OWF instanceId is appended to the identifier so the same widget can be reused in
     * multiple windows without conflict.
     * @method getInstanceId
     * @return {String} A unique identifier string
     */
    function getInstanceId(qualifier) {
        var instanceId;
        neon.util.ajaxUtils.doGet(
            neon.serviceUrl('widgetservice', 'instanceid', buildInstanceIdQueryString(qualifier)), {
                // callers expect the id to return synchronously so set async to false
                async: false,
                success: function(id) {
                    instanceId = id;
                }
            }
        );
        return instanceId;
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

    /**
     * Gets widget initialization metadata.
     * @method getWidgetInitialization
     * @param {String} widget an identifier of a widget, usually the widget name
     * @param {Function} successCallback The callback that contains the saved data.
     * @return {neon.util.AjaxRequest} The xhr request object
     */
    function getWidgetInitializationData(widget, successCallback) {
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('widgetservice', 'widgetinitialization/' + encodeURIComponent(widget)), {
                success: successCallback
            }
        );
    }

    /**
     * Gets the metadata for each of the columns in the table
     * @method getWidgetDatasetMetadata
     * @param {String} databaseName The name of the database whose field metadata is being returned
     * @param {String} tableName The name of the table whose field metadata is being returned
     * @param {String} widgetName The name of the widget whose initialization data is being returned
     * @param successCallback
     * @return {neon.util.AjaxRequest}
     */
    function getWidgetDatasetMetadata(databaseName, tableName, widgetName, successCallback) {
        return neon.util.ajaxUtils.doGet(
            neon.serviceUrl('widgetservice', 'widgetdataset/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName) + '/' + encodeURIComponent(widgetName)),
            {
                success: function(result) {
                    // The result is an array in the format:
                    // [{elementId: "name", value: "columnName"}, {elementId: "otherName", value: "column2"}]
                    // Turn it into the more useful object:
                    // {name: "columnName", otherName: "column2"}
                    var mappings = {};
                    for(var i = 0; i < result.length; ++i) {
                        mappings[result[i].elementId] = result[i].value;
                    }
                    successCallback(mappings);
                }
            }
        );
    }

    return {
        saveState: saveState,
        getSavedState: getSavedState,
        getInstanceId: getInstanceId,
        getWidgetInitializationData: getWidgetInitializationData,
        getWidgetDatasetMetadata: getWidgetDatasetMetadata
    };
})();
