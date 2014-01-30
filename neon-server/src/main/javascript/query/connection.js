/**
 * Creates a connection to a data store such as mongo or shark.
 * @class neon.query.Connection
 * @constructor
 */
neon.query.Connection = function(datastore, hostname){
    this.dataSource = datastore;
    this.connectionUrl = hostname;
};

neon.query.Connection.prototype.setConnectionId = function(id){
    this.connectionId = id;
};


/**
 * Connects a user to the given datastore.
 * @method connectToDatastore
 * @param {String} datastore The name of the datastore (e.g. mongo or hive)
 * @param {String} hostname The connection url of the datastore
 * @param {Function} successCallback Callback invoked on success
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.connectToDatastore = function (datastore, hostname, successCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.query.serviceUrl('connections', ''),
        {
            data: { dataSource: datastore, connectionUrl: hostname },
            success: successCallback
        }
    );
};



neon.query.removeConnection = function() {

};