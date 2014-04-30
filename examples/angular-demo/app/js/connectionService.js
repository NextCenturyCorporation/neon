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
							name: "x-axis",
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
						name: "gbSmall",
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
							name: "x-axis",
							mapping: "yyyy-mm"
						}, {
							name: "y-axis",
							mapping: "sentiment"
						}, {
							name: "color-by",
							mapping: "sentiment"
						},{
							name: "size-by",
							mapping: "retweet_count"
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

		service.setActiveConnection = function(connection) {
			activeConnection = connection;
		};

		service.getActiveConnection = function() {
			return activeConnection;
		};

		service.getActiveConnectionInformation = function(name) {
			return $filter('filter')(connectionInformation, { "host": activeConnection.host_ }); 
		}

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
