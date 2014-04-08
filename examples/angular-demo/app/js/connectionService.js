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
angular.module('neonDemo.services', []).factory('ConnectionService',
	function() {

		var activeConnection = undefined;
		var connectionInformation = [
			{
				name: "Sample Earthquake Data",
				type: "mongo",
				host: "localhost",
				database: "mydb",
				metaData: [{
					table: "sample",
					mappings: {
						"time": "time",
						"latitude": "latitude",
						"longitude": "longitude"
					}
				}]
			}
		];

		var service = {};

		service.setActiveConnection = function(connection) {
			activeConnection = connection;
		};

		service.getActiveConnection = function() {
			return activeConnection;
		};

		service.getConnectionInformation = function(name) {
			if (typeof(name) === 'undefined') {
				return connectionInformation;
			}
			else {
				return angular.filter(connectionInformation, { "name": name }); 
			}
		}

		return service;

	});
