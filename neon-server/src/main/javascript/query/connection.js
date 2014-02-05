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
 * Set a connection id on the Connection.
 * @method setConnectionId
 * @param {String} id The id to set.
 */

neon.query.connection.Connection.prototype.setConnectionId = function (id) {
    this.connectionId = id;
};


/**
 * Connects a user to the given datastore.
 * @method connectToDatastore
 * @param {neon.query.Connection} connection The connection information
 * @param {Function} successCallback Callback invoked on success. The function has one parameter, the connectionId.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.connectToDatastore = function (connection, successCallback) {

    return neon.util.ajaxUtils.doPostJSON(connection,
        neon.query.SERVER_URL + '/services/connections',
        {
            success: successCallback
        }
    );
};

/**
 * Get the existing connection from Neon.
 * @param {String} connectionId The key that identifies the connection resource.
 * @param {Function} successCallback  Callback invoked on success
 * @returns {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.getConnection = function (connectionId, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('connections', connectionId),
        {
            success: successCallback
        }
    );
};

/**
 * Removes an existing connection from Neon.
 * @param {String} connectionId The key that identifies the connection resource.
 * @param {Function} successCallback  Callback invoked on success
 * @returns {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connection.removeById = function (connectionId, successCallback) {
    return neon.util.ajaxUtils.doDelete(
        neon.query.serviceUrl('connections', connectionId),
        {
            success: successCallback
        }
    );
};

/**
 * @method getHostnames
 * @param successCallback
 * @returns {neon.util.AjaxRequest}
 */

neon.query.connection.getHostnames = function (successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.query.serviceUrl('connections', 'hostnames'),
        {
            success: successCallback
        }
    );
}
