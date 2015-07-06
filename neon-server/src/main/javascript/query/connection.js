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
 * Stores the parameters a user is using to connect to the database. Queries will be executed through this connection.
 * @class neon.query.Connection
 * @example
 *     var connection = new neon.query.Connection();
 *     // use a mongo database on localhost
 *     connection.connect(neon.query.Connection.MONGO, "localhost");
 *
 *     // queries through this connection will use the parameters specified above
 *     connection.executeQuery(query1, callback);
 *     connection.executeQuery(query2, callback);
 * @constructor
 */
neon.query.Connection = function() {
    this.host_ = undefined;
    this.databaseType_ = undefined;
    this.messenger = new neon.eventing.Messenger();
};

/**
 * Indicates the database type is mongo
 * @property MONGO
 * @type {String}
 */
neon.query.Connection.MONGO = 'mongo';

/**
 * Indicates the database type is spark sql
 * @property SPARK
 * @type {String}
 */
neon.query.Connection.SPARK = 'sparksql';

/**
 * Specifies what database type and host the queries will be executed against and publishes a CONNECT_TO_HOST event.
 * @method connect
 * @param {String} databaseType What type of database is being connected to. The constants in this class specify the
 * valid database types.
 * @param {String} host The host the database is running on
 */
neon.query.Connection.prototype.connect = function(databaseType, host) {
    this.host_ = host;
    this.databaseType_ = databaseType;
    this.messenger.publish(neon.eventing.channels.CONNECT_TO_HOST, {
        host: host,
        type: databaseType
    });
};

/**
 * Executes the specified query and fires the callback when complete.
 * @method executeQuery
 * @param {neon.query.Query} query the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeQuery = function(query, successCallback, errorCallback) {
    return this.executeQueryService_(query, successCallback, errorCallback, 'query');
};

/**
 * Gets the metadata for each of the columns in the table
 * @method getColumnMetadata
 * @param {String} databaseName
 * @param {String} tableName The table whose metadata is being returned
 * @param {Function} successCallback
 * @return {neon.util.AjaxRequest}
 */
neon.query.Connection.prototype.getColumnMetadata = function(databaseName, tableName, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'columnmetadata/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName)), {
            success: successCallback,
            responseType: 'json'
        }
    );
};

/**
 * Executes the specified query group (a series of queries whose results are aggregate),
 * and fires the callback when complete.
 * @method executeQueryGroup
 * @param {neon.query.QueryGroup} queryGroup the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that
 * contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeQueryGroup = function(queryGroup, successCallback, errorCallback) {
    return this.executeQueryService_(queryGroup, successCallback, errorCallback, 'querygroup');
};

neon.query.Connection.prototype.executeQueryService_ = function(query, successCallback, errorCallback, serviceName) {
    var opts = [];
    if(query.ignoreFilters_) {
        opts.push("ignoreFilters=true");
    } else if(query.ignoredFilterIds_) {
        var filterIds = [];
        query.ignoredFilterIds_.forEach(function(id) {
            filterIds.push("ignoredFilterIds=" + encodeURIComponent(id));
        });
        if(filterIds.length) {
            opts.push(filterIds.join("&"));
        }
    }
    if(query.selectionOnly_) {
        opts.push("selectionOnly=true");
    }
    return neon.util.ajaxUtils.doPostJSON(
        query,
        neon.serviceUrl('queryservice', serviceName + '/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_), opts.join('&')),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Executes the specified export request and fires the callback when complete.
 * @method executeExport
 * @param {neon.query.Query} query the query to export data for
 * @param {Function} successCallback The callback to fire when the export request successfully completes. Takes 
 * a JSON object with the export URL stored in it's data field as a parameter.
 * @param {Function} [errorCallBack] The optional callback when an error occurs. This function takes the server's
 * response as a parameter.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeExport = function(data, successCallback, errorCallback, fileType) {
    data.fileType = fileType;
    return neon.util.ajaxUtils.doPostJSON(
        data,
        neon.serviceUrl('exportservice', 'export' +'/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_), ''),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * 
 */
neon.query.Connection.prototype.executeUploadFile = function(file, successCallback, errorCallback) {
    // Hopefully I don't have to manually make an XMLHttpRequest in the end - for now, though, it's the only way I've found to get to the server at all.
    var xhr = new XMLHttpRequest();
    xhr.open('POST', neon.serviceUrl('importservice', 'upload' + '/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_), ''));
    xhr.onload = function() {
        if(xhr.status === 200) {
            successCallback(xhr.response);
        } else {
            errorCallback(xhr.response);
        }
    }
    xhr.send(file);
    return;


    window.alert(JSON.stringify(file))
    var opts = [];
    opts.push("")
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('importservice', 'upload' + '/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_), ''),
        {
            success: successCallback,
            error: errorCallback,
            data: file,
            responseType: 'json',
            processData: false,
            contentType: 'multipart/form-data'
        }
    );

}

/**
 * Gets a list of database names
 * @method getDatabaseNames
 * @param {Function} successCallback The callback that contains the database names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getDatabaseNames = function(successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'databasenames/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_)), {
            success: successCallback,
            responseType: 'json'
        }
    );
};

/**
 * Gets the tables names available for the current database
 * @method getTableNames
 * @param {String} databaseName
 * @param {Function} successCallback The callback that contains the table names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getTableNames = function(databaseName, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'tablenames/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) + '/' + encodeURIComponent(databaseName)), {
            success: successCallback,
            responseType: 'json'
        }
    );
};

/**
 * Executes a query that returns the field names from table
 * @method getFieldNames
 * @param {String} databaseName
 * @param {String} tableName The table name whose fields are being returned
 * @param {Function} successCallback The callback to call when the field names are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getFieldNames = function(databaseName, tableName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'fields/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) +
          '/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName)), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};

/**
 * Executes a query that returns a map of the table names available in the database and the field names in each table
 * @method getTableNamesAndFieldNames
 * @param {String} databaseName
 * @param {Function} successCallback The callback to call when the field names are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getTableNamesAndFieldNames = function(databaseName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'tablesandfields/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) + '/' + encodeURIComponent(databaseName)), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};

neon.query.Connection.prototype.executeArrayCountQuery = function(databaseName, tableName, field, limit, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(neon.serviceUrl(
        'queryservice/arraycounts', encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) + '/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName),
        'field=' + field + (limit ? '&limit=' + limit : '')), {
        success: successCallback,
        error: errorCallback,
        responseType: 'json'
    });
};