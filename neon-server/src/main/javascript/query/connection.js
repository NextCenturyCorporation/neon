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
 * Indicates the database type is elasticsearch
 * @property ELASTICSEARCH
 * @type {String}
 */
neon.query.Connection.ELASTICSEARCH = 'elasticsearch';

/**
 * Specifies what database type and host the queries will be executed against and publishes a CONNECT_TO_HOST event.
 * @method connect
 * @param {String} databaseType What type of database is being connected to. The constants in this class specify the
 * valid database types.
 * @param {String} host The host the database is running on.  This can be an address (e.g., localhost) or an
 * address:port pair (e.g., localhost:9300).  If no port is provided, the Neon server will assume the default port
 * for the databaseType:  27017 for Mongo, 10000 for Spark via a Hive2 Thrift connection, and 9300 for an
 * Elasticsearch transport client.
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

/**
 * Executes a query that returns a sorted list of key, count pairs for an array field in the database.
 * @method executeArrayCountQuery
 * @param {String} databaseName The name of the database
 * @param {String} tableName The name of the collection or table
 * @param {String} fieldName The name of the array field to count
 * @param {Number} limit The number of pairs to return (default:  50)
 * @param {Object} whereClause The where clause to apply to the array counts query, or null to apply no where clause
 * @param {Function} successCallback The callback to call when the list of key,count pairs is returned
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeArrayCountQuery = function(databaseName, tableName, fieldName, limit, whereClause, successCallback, errorCallback) {
    var serviceName = encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) + '/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName) + '/' + encodeURIComponent(fieldName);
    var serviceUrl = neon.serviceUrl('queryservice/arraycounts', serviceName, (limit ? 'limit=' + limit : ''));
    var options = {
        success: successCallback,
        error: errorCallback,
        responseType: 'json'
    };
    return neon.util.ajaxUtils.doPostJSON(whereClause, serviceUrl, options);
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
 * @param {Function} [errorCallback] The optional callback when an error occurs. This function takes the server's
 * response as a parameter.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeExport = function(query, successCallback, errorCallback, fileType) {
    query.fileType = fileType;
    return neon.util.ajaxUtils.doPostJSON(
        query,
        neon.serviceUrl('exportservice', 'export/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_), ''),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Sends a file to be imported into a database and fires the callback when complete.
 * @method executeUploadFile
 * @param {FormData} data A FormData object containing the file to upload, as well as its type and a username and name of the database to upload it to.
 * @param {Function} successCallback The function to call when the request successfully completes. This function takes the server's response as a parameter.
 * @param {Function} errorCallback The function to call when an error occurs. This function takes the server's response as a parameter.
 * @param {String} [host] The host to upload a file to when you don't want to upload to the default.
 * @param {String} [databaseType] The type of database to upload a file to when you don't want the default.
 */
neon.query.Connection.prototype.executeUploadFile = function(data, successCallback, errorCallback, host, databaseType) {
    neon.util.ajaxUtils.doPostBinary(data, neon.serviceUrl('importservice', 'upload/' + encodeURIComponent(host || this.host_) + '/' + encodeURIComponent(databaseType || this.databaseType_), ''),
        successCallback, errorCallback);
};

/**
 * Checks on the status of a type-guessing operation with the given uuid and fires the callback when complete.
 * @method executeCheckTypeGuesses
 * @param {String} uuid The uuid associated with the type-guessing operation to check on.
 * @param {Function} successCallback The function to call when the request successfully completes. This function takes the server's response as a parameter.
 * @param {String} [host] The host to upload a file to when you don't want to upload to the default.
 * @param {String} [databaseType] The type of database to upload a file to when you don't want the default.
 */
neon.query.Connection.prototype.executeCheckTypeGuesses = function(uuid, successCallback, host, databaseType) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('importservice', 'guesses/' + encodeURIComponent(host || this.host_) + '/' + encodeURIComponent(databaseType || this.databaseType_) + '/' + encodeURIComponent(uuid), ''), {
            success: successCallback
        }
    );
};

/**
 * Initiates the process of importing records from a file into a database and converting them from strings to more appropriate types, and fires the callback when complete.
 * @method executeLoadFileIntoDB
 * @param {Object} data An object that contains a date formatting string and a list of objects that contain field names and type names to go with them.
 * @param {string} uuid The job ID to associate with the data to be parsed and converted.
 * @param {Function} successCallback The function to call when the request successfully completes. This function takes the server's response as a parameter.
 * @param {Function} errorCallback The function to call when an error occurs. This function takes the server's response as a parameter.
 * @param {String} [host] The host to upload a file to when you don't want to upload to the default.
 * @param {String} [databaseType] The type of database to upload a file to when you don't want the default.
 */
neon.query.Connection.prototype.executeLoadFileIntoDB = function(data, uuid, successCallback, errorCallback, host, databaseType) {
    neon.util.ajaxUtils.doPostJSON(data, neon.serviceUrl('importservice', 'convert/' + encodeURIComponent(host || this.host_) + '/' + encodeURIComponent(databaseType || this.databaseType_) +
        '/' + encodeURIComponent(uuid), ''), {
            success: successCallback,
            error: errorCallback
        }
    );
};

/**
 * Checks on the status of an import and conversion operation with the given uuid and fires the callback when complete.
 * @method executeCheckImportProgress
 * @param {String} uuid The uuid associated with the import and conversion operation to check on.
 * @param {Function} successCallback The function to call when the request successfully completes. This function takes the server's response as a parameter.
 * @param {String} [host] The host to upload a file to when you don't want to upload to the default.
 * @param {String} [databaseType] The type of database to upload a file to when you don't want the default.
 */
neon.query.Connection.prototype.executeCheckImportProgress = function(uuid, successCallback, host, databaseType) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('importservice', 'progress/' + encodeURIComponent(host || this.host_) + '/' + encodeURIComponent(databaseType || this.databaseType_) + '/' + encodeURIComponent(uuid), ''), {
            success: successCallback
        }
    );
};

/**
 * Sends a request to remove a dataset associated with the given username and satabase name, and fires the callback when complete.
 * @method executeRemoveDataset
 * @param {String} user The username associated with the database to drop.
 * @param {String} data The database name associated with the database to drop.
 * @param {Function} successCallback The function to call when the request successfully completes. This function takes the server's response as a parameter.
 * @param {Function} errorCallback The function to call when an error occurs. This function takes the server's response as a parameter.
 * @param {String} [host] The host to upload a file to when you don't want to upload to the default.
 * @param {String} [databaseType] The type of database to upload a file to when you don't want the default.
 */
neon.query.Connection.prototype.executeRemoveDataset = function(user, data, successCallback, errorCallback, host, databaseType) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('importservice', 'drop/' + encodeURIComponent(host || this.host_) + '/' + encodeURIComponent(databaseType || this.databaseType_) + '?user=' + encodeURIComponent(user) +
            '&data=' + encodeURIComponent(data), ''), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};

/**
 * Gets a list of database names
 * @method getDatabaseNames
 * @param {Function} successCallback The callback that contains the database names in an array.
 * @param {Function} errorCallback The function to call when an error occurs. This function takes the server's response as a parameter.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getDatabaseNames = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'databasenames/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_)), {
            success: successCallback,
            error: errorCallback,
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

/**
 * Requests and returns the translation cache.
 * @method getTranslationCache
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getTranslationCache = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl("translationservice", "getcache"), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to save the given translation cache.
 * @method setTranslationCache
 * @param {Object} cache
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.setTranslationCache = function(cache, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPostJSON(
        cache,
        neon.serviceUrl("translationservice", "setcache"), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to save the state with the given parameters.
 * @method saveState
 * @param {Object} stateParams
 * @param {Array} stateParams.dashboard
 * @param {Object} stateParams.dataset
 * @param {String} [stateParams.stateName]
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.saveState = function(stateParams, successCallback, errorCallback) {
    var opts = "";

    if(stateParams.stateName) {
        opts = "stateName=" + stateParams.stateName;
        delete stateParams.stateName;
    }

    return neon.util.ajaxUtils.doPostJSON(
        stateParams,
        neon.serviceUrl("stateservice", "savestate", opts), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to load the state with the given name, or dashboard and/or filter state IDs.
 * @method loadState
 * @param {Object} stateParams
 * @param {String} stateParams.dashboardStateId
 * @param {String} stateParams.filterStateId
 * @param {String} stateParams.stateName
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.loadState = function(stateParams, successCallback, errorCallback) {
    var opts = [];
    if(stateParams.stateName) {
        opts.push("stateName=" + stateParams.stateName);
    } else {
        if(stateParams.dashboardStateId) {
            opts.push("dashboardStateId=" + stateParams.dashboardStateId);
        }
        if(stateParams.filterStateId) {
            opts.push("filterStateId=" + stateParams.filterStateId);
        }
    }
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl("stateservice", "loadstate", opts.join('&')), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to delete the states with the given name.
 * @method deleteState
 * @param {String} stateName
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.deleteState = function(stateName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doDelete(
        neon.serviceUrl("stateservice", "deletestate/" + stateName), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to retrieve all the states names.
 * @method getAllStateNames
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getAllStateNames = function(successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl("stateservice", "allstatesnames"), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests to retrieve the state name for the given state IDs.
 * @method getStateName
 * @param {Object} stateParams
 * @param {String} stateParams.dashboardStateId
 * @param {String} stateParams.filterStateId
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getStateName = function(stateParams, successCallback, errorCallback) {
    var opts = [];
    opts.push("dashboardStateId=" + stateParams.dashboardStateId);
    opts.push("filterStateId=" + stateParams.filterStateId);
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl("stateservice", "statename", opts.join('&')), {
            success: successCallback,
            error: errorCallback,
            responseType: "json"
        }
    );
};

/**
 * Requests for field types from the table
 * @method getFieldTypes
 * @param {String} databaseName
 * @param {String} tableName The table name whose fields are being returned
 * @param {Function} successCallback The callback to call when the field types are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getFieldTypes = function(databaseName, tableName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'fields/types/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_) +
          '/' + encodeURIComponent(databaseName) + '/' + encodeURIComponent(tableName)), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};

/**
 * Requests for field types from the tables
 * @method getFieldTypesForGroup
 * @param {Object} databaseToTableNames A mapping of database names to a list of table names to get field
 * types for.
 * @param {Function} successCallback The callback to call when the field types are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getFieldTypesForGroup = function(databaseToTableNames, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPostJSON(
        databaseToTableNames,
        neon.serviceUrl('queryservice', 'fields/types/' + encodeURIComponent(this.host_) + '/' + encodeURIComponent(this.databaseType_)), {
            success: successCallback,
            error: errorCallback,
            responseType: 'json'
        }
    );
};
