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
 * Represents a connection to a data source.
 * @param {String} datastore A database type
 * @param {String} hostname The url where the connection is hosted.
 * @class neon.query.connection.Connection
 * @constructor
 */
neon.query.connection.Connection = function (datastore, hostname) {
    this.dataSource = datastore;
    this.connectionUrl = hostname;
};

/**
 * Connects a user to the given datastore.
 * @method connectToDatastore
 * @param {neon.query.connection.Connection} connection The connection information
 * @param {Function} successCallback Callback invoked on success. The function has one parameter, the connectionId.
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.connectToDatastore = function (connection, successCallback, errorCallback) {

    return neon.util.ajaxUtils.doPost(
        neon.query.SERVER_URL + '/services/connections',
        {
            data: JSON.stringify(connection),
            contentType: 'application/json',
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Get an existing connection by id.
 * @method getConnection
 * @param {String} connectionId The key that identifies the connection resource.
 * @param {Function} successCallback  Callback invoked on success
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.getConnection = function (connectionId, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('connections', connectionId),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Get all the existing connections.
 * @method getAllConnectionIds
 * @param {Function} successCallback  Callback invoked on success
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.getAllConnectionIds = function (successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('connections', 'ids'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Removes an existing connection from Neon.
 * @method removeById
 * @param {String} connectionId The key that identifies the connection resource.
 * @param {Function} successCallback  Callback invoked on success
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.removeById = function (connectionId, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doDelete(
        neon.query.serviceUrl('connections', connectionId),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Gets the available host names.
 * @method getHostnames
 * @param successCallback Callback invoked on success
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest}
 */

neon.query.connection.getHostnames = function (successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('connections', 'hostnames'),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};
