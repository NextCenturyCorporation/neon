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
 *     // use database mydb
 *     connection.use("mydb");
 *
 *     // queries through this connection will use the parameters specified above
 *     connection.executeQuery(query1, callback);
 *     connection.executeQuery(query2, callback);
 * @constructor
 */
neon.query.Connection = function() {

    this.host_ = undefined;
    this.databaseType_ = undefined;
    this.database_ = undefined;
};

/**
 * Indicates the database type is mongo
 * @property MONGO
 * @type {String}
 */
neon.query.Connection.MONGO = 'mongo';

/**
 * Indicates the database type is hive
 * @property HIVE
 * @type {String}
 */
neon.query.Connection.HIVE = 'hive';


/**
 * Specifies what database type and host the queries will be executed against.
 * @method connect
 * @param {String} databaseType What type of database is being connected to. The constants in this class specify the
 * valid database types.
 * @param {String} host The host the database is running on
 */
neon.query.Connection.prototype.connect = function(databaseType, host) {
    this.host_ = host;
    this.databaseType_ = databaseType;
};

/**
 * Specifies what database to use for queries.
 * @param {String} database The name of the database to use for queries
 * @method use
 */
neon.query.Connection.prototype.use = function(database) {
    this.database_ = database;
};

/**
 * Executes the specified query and fires the callback when complete. This query object may be modified to have the
 * database name set if none was specified in the query.
 * @method executeQuery
 * @param {neon.query.Query} query the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeQuery = function (query, successCallback, errorCallback) {
    this.populateQueryDatabaseField_(query);
    return this.executeQueryService_(query, successCallback, errorCallback, 'query');
};

/**
 * Executes a text based query.
 * Currently this does not use the value of the "use" method and a use statement must be specified as part of the query.
 * @method executeTextQuery
 * @param {String} queryText The query text
 * @param {Function} successCallback The callback to execute when the query is parsed, which contains the query result
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that
 * contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeTextQuery = function (queryText, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doPost(
        neon.serviceUrl('languageservice', 'query/' + this.host_ + '/' + this.databaseType_),
        {
            data: { text: queryText },
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            responseType: 'json',
            success: successCallback,
            error: errorCallback
        }
    );
};


/**
 * Gets the metadata for each of the columns in the table
 * @method getColumnMetadata
 * @param {String} tableName The table whose metadata is being returned
 * @param {Function} successCallback
 * @return {neon.util.AjaxRequest}
 */
neon.query.Connection.prototype.getColumnMetadata = function(tableName, successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'columnmetadata/' + this.database_ + '/' + tableName),
        {
            success: successCallback
        }
    );
};

neon.query.Connection.prototype.populateQueryDatabaseField_ = function(query) {
    if (!query.filter.databaseName) {
        query.filter.databaseName = this.database_;
    }
};

/**
 * Executes the specified query group (a series of queries whose results are aggregate),
 * and fires the callback when complete. This query objects may be modified to have the
 * database name set if none was specified in the query.
 * @method executeQueryGroup
 * @param {neon.query.QueryGroup} queryGroup the query to execute
 * @param {Function} successCallback The callback to fire when the query successfully completes
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.executeQueryGroup = function (queryGroup, successCallback, errorCallback) {
    this.populateQueryGroupDatabaseField_(queryGroup);
    return this.executeQueryService_(queryGroup, successCallback, errorCallback, 'querygroup');
};

neon.query.Connection.prototype.populateQueryGroupDatabaseField_ = function(queryGroup) {
    var me = this;
    _.each(queryGroup.queries, function(query) {
        me.populateQueryDatabaseField_(query);
    });
};

neon.query.Connection.prototype.executeQueryService_ = function (query, successCallback, errorCallback, serviceName) {
    if (query.selectionOnly_) {
        serviceName += "withselectiononly";
    }
    else if (query.ignoreFilters_) {
        serviceName += "disregardfilters";
    }
    return neon.util.ajaxUtils.doPostJSON(
        query,
        neon.serviceUrl('queryservice', serviceName + '/' + this.host_ + '/' + this.databaseType_),
        {
            success: successCallback,
            error: errorCallback
        }
    );
};


/**
 * Gets a list of database names
 * @method getDatabaseNames
 * @param {Function} successCallback The callback that contains the database names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.Connection.prototype.getDatabaseNames = function (successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice','databasenames/' +  this.host_ + '/' + this.databaseType_),
        {
            success: successCallback
        }
    );
};

/**
 * Gets the tables names available for the current database
 * @method getTableNames
 * @param {Function} successCallback The callback that contains the table names in an array.
 * @return {neon.util.AjaxRequest} The xhr request object
 */

neon.query.Connection.prototype.getTableNames = function (successCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'tablenames/' + this.host_ + '/' + this.databaseType_ + '/' + this.database_),
        {
            success: successCallback
        }
    );
};

/**
 * Executes a query that returns the field names from table
 * @method getFieldNames
 * @param {String} tableName The table name whose fields are being returned
 * @param {Function} successCallback The callback to call when the field names are successfully retrieved
 * @param {Function} [errorCallback] The optional callback when an error occurs. This is a 3 parameter
 * function that contains the xhr, a short error status and the full error message.
 * @return {neon.util.AjaxRequest} The xhr request object
 */
neon.query.Connection.prototype.getFieldNames = function (tableName, successCallback, errorCallback) {
    return neon.util.ajaxUtils.doGet(
        neon.serviceUrl('queryservice', 'fields/' + this.host_ + '/' + this.databaseType_ + '/' + this.database_ + '/' + tableName),
        {
            success: successCallback,
            error: errorCallback,
        }
    );
};
