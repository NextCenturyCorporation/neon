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
 * As this is meant for demonstrative purposes, the meta data is simply hard-coded to 1 or 2 example
 * datasets meant to be loaded in a local Mongo instance. 
 *
 * @class neonDemo.services.ConnectionService
 * @constructor
 */
var services = angular.module('neonDemo.services',[]);
services.factory('ConnectionService', ['$filter',
	function($filter) {

		var activeConnection = undefined;
		var connectionInformation = [
			{
				name: "Sample Earthquake Data",
				type: "mongo",
				host: "localhost",
				mappings: [{
					database: "mydb",
					tables: [{
						name: "sample",
						fields: [{
							name: "date",
							mapping: "time"
						}, {
							name: "latitude",
							mapping: "latitude"
						}, {
							name: "longitude",
							mapping: "longitude"
						}, {
							name: "line-x-axis",
							mapping: "time"
						}, {
							name: "bar-x-axis",
							mapping: "time"
						}, {
							name: "y-axis",
							mapping: "magnitude"
						}, {
							name: "color-by",
							mapping: "magnitudeType"
						},{
							name: "size-by",
							mapping: "magnitude"
						},{
							name: "sort-by",
							mapping: "time"
						}]
					},{
						name: "gbDate",
						fields: [{
							name: "date",
							mapping: "created_at"
						}, {
							name: "latitude",
							mapping: "latitude"
						}, {
							name: "longitude",
							mapping: "longitude"
						}, {
							name: "line-x-axis",
							mapping: "yyyy-mm-dd"
						}, {
							name: "bar-x-axis",
							mapping: "yyyy-mm"
						}, {
							name: "y-axis",
							mapping: "sentiment"
						}, {
							name: "color-by",
							mapping: "sentimentType"
						},{
							name: "size-by",
							mapping: "sentiment"
						},{
							name: "sort-by",
							mapping: "created_at"
						}]
					}]
				}]
			}, {
				host: "fluffy"
			}
		];

		var service = {};

		/** 
		 * Establish a Neon connection to a particular datset.
		 * @param {String} databaseType 
		 * @param {String} host
		 * @param {String} database
		 */
		service.connectToDataset = function(databaseType, host, database) {
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
		 * Returns connection meta-data related to a particular host.
		 * @param {neon.query.Connection} name
		 * @return {Object} Meta data json object describing that host.
		 * @method getActiveConnectionInformation
		 */
		service.getActiveConnectionInformation = function(name) {
			return $filter('filter')(connectionInformation, { "host": activeConnection.host_ }); 
		}

		/**
		 * Returns connection meta-data related to a particular host.  Allows client code to ask if there is a mapping
		 * for common visualization fields to a particular table field.  For example, "latitude" values may stored in
		 * a "lat_val" column or visulizations may want to "size-by" a "magnitude" column in a table of earthquake data.
		 * @param {String} database
		 * @param {String} table
		 * @param {String} field
		 * @return {Object} retVal A valid mapping object if present in the meta data associated with a connection
		 * @return {String} retVal.name The requested field type
		 * @return {String} retVal.mapping The column in the table which contains data of the requested type
		 * @method getFieldMapping
		 */
		service.getFieldMapping = function(database, table, field) {
			var connectionInfo = (activeConnection) ? $filter('filter')(connectionInformation, { "host": activeConnection.host_ }) : [];
			if (connectionInfo.length > 0) {
				var database = $filter('filter')(connectionInfo[0].mappings, { "database":database });
				if (database.length > 0) {
					var table = $filter('filter')(database[0].tables, { "name": table });
					if (table.length > 0) {
						var field = $filter('filter')(table[0].fields, { "name": field });
						if (field.length > 0) {
							return field[0];
						}
					}
				}
			}
			return {};
		}

		return service;

	}]);
