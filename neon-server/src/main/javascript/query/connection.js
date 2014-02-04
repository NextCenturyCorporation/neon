/**
 * Creates a connection to a data store such as mongo or shark.
 * @class neon.query.Connection
 * @constructor
 */
neon.query.Connection = function(datastore, hostname){
    this.dataSource = datastore;
    this.connectionUrl = hostname;
};

/**
 * Set a connection id on the Connection.
 * @method setConnectionId
 * @param {String} id The id to set.
 */

neon.query.Connection.prototype.setConnectionId = function(id){
    this.connectionId = id;
};


/**
 * Connects a user to the given datastore.
 * @method connectToDatastore
 * @param {neon.query.Connection} connection The connection information
 * @param {Function} successCallback Callback invoked on success. The function has one parameter, the connectionId.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connectToDatastore = function (connection, successCallback) {

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

neon.query.getConnection = function(connectionId, successCallback) {
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

neon.query.removeConnection = function(connectionId, successCallback) {
    return neon.util.ajaxUtils.doDelete(
        neon.query.serviceUrl('connections', connectionId),
        {
            success: successCallback
        }
    );
};