/**
 * Represents a connection to a data source.
 * @param {String} datastore A database type
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
 * @returns {neon.util.AjaxRequest} The xhr request object
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
 * @returns {neon.util.AjaxRequest} The xhr request object
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
 * @param {String} connectionId The key that identifies the connection resource.
 * @param {Function} successCallback  Callback invoked on success
 * @param {Function} errorCallback The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @returns {neon.util.AjaxRequest} The xhr request object
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
 * @returns {neon.util.AjaxRequest}
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
