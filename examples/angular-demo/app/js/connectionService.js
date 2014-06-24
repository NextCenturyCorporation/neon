'use strict';
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
 * This provides an Angular service for managing simple meta data about Neon Connection objects.
 *
 * @class neonDemo.services.ConnectionService
 * @constructor
 */
var services = angular.module('neonDemo.services',[]);
services.factory('ConnectionService', ['$filter',
	function($filter) {

		var activeConnection = undefined;
		var connectionInformation = {fields: {}};

		var service = {};

		/** 
		 * Establish a Neon connection to a particular datset.
		 * @param {String} databaseType 
		 * @param {String} host
		 * @param {String} database
         * @param {String} table
		 */
		service.connectToDataset = function(databaseType, host, database, table) {
			if (!activeConnection) {
				activeConnection = new neon.query.Connection();
			} 
			
			// Connect to the specified server.
			if (databaseType && host) {
				activeConnection.connect(databaseType, host);
			}

			// Use the given database if present.  If datbase is undefined, this will
			// will be passed along, clearing out the table database field.
			activeConnection.use(database);

            // If this is different from the previous call, clear out the metadata
            if (connectionInformation === undefined || connectionInformation.type !== databaseType
                || connectionInformation.host !== host || connectionInformation.database !== database
                || connectionInformation.table !== table) {
                connectionInformation = {
                    type: databaseType,
                    host: host,
                    database: database,
                    table: table,
                    fields: {}
                }
            }
		};

        /**
         * Gets any metadata information from the service for this specific table. connectToDataset must be called
         * before this function.
         * @param callback
         */
        service.loadMetadata = function(callback) {
            var database = connectionInformation.database;
            var table = connectionInformation.table;
            // Only go to the server if the information hasn't already been loaded.
            if (_.keys(connectionInformation.fields).length === 0) {
                neon.widget.getWidgetDatasetMetadata(database, table, "angular_example", function (result) {
                    connectionInformation.fields = result;
                    callback();
                });
            } else {
                callback();
            }
        };

		/**
		 * Sets the active connection.  Any client code can ask for the active connection rather than creating a new one.
		 * @param {neon.query.Connection} connection
		 * @method setActiveConnection
		 */
		service.setActiveConnection = function(connection) {
			activeConnection = connection;
		};

		/**
		 * Returns the active connection. 
		 * @return {neon.query.Connection}
		 * @method getActiveConnection
		 */
		service.getActiveConnection = function() {
			return activeConnection;
		};

		/**
		 * Returns connection meta-data related to the current host.  Allows client code to ask if there is a mapping
		 * for common visualization fields to a particular table field.  For example, "latitude" values may stored in
		 * a "lat_val" column or visulizations may want to "size-by" a "magnitude" column in a table of earthquake data.
         * loadMetadata() must be called before this function, or this function will return undefined.
		 * @param {String} field the name of the field to get the mapping for the current table
		 * @return {String} retVal The column in the table which contains data of the requested type
		 * @method getFieldMapping
		 */
		service.getFieldMapping = function(field) {
            return connectionInformation.fields[field];
		};

        /**
         * Overrides the meta-data for a particular field.
         * @param {String} field
         * @param {String} mapping
         */
        service.setFieldMapping = function(field, mapping) {
            connectionInformation.fields[field] = mapping;
        };

        /**
         * Returns an object where the keys are the fields and the values are the mappings for the current host.
         * @returns {String}
         */
        service.getFieldMappings = function() {
            return connectionInformation.fields;
        };

		return service;

	}]);
