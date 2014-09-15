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


angular.module('directedGraphDirective', [])
.directive('directedGraph', ['ConnectionService', function (connectionService) {
	return {
		templateUrl: 'app/partials/directives/directedGraph.html',
		restrict: 'EA',
		scope: {
			startingFields: '='
		},
		link: function ($scope, element, attr) {
			if($scope.startingFields) {
				$scope.groupFields = $scope.startingFields;
			} else {
				$scope.groupFields = [""];
			}

			$scope.initialize = function () {
				$scope.messenger = new neon.eventing.Messenger();

				$scope.messenger.events({
					activeDatasetChanged: onDatasetChanged,
					filtersChanged: onFiltersChanged
				});
			};

			var onFiltersChanged = function (message) {
				$scope.render();
			};

			var onDatasetChanged = function (message) {
				$scope.databaseName = message.database;
				$scope.tableName = message.table;
				$scope.data = [];

				// if there is no active connection, try to make one.
				connectionService.connectToDataset(message.datastore, message.hostname, message.database, message.table);

				var connection = connectionService.getActiveConnection();
				if (connection) {
					connectionService.loadMetadata($scope.render);
				}
			};

			$scope.render = function() {
				if($scope.groupFields.length > 1 || $scope.groupFields[0] !== "") {
					if($scope.groupFields[$scope.groupFields.length - 1] === "") {
						$scope.groupFields.splice($scope.groupFields.length - 1, 1);
					}
					$scope.queryForData();
				}
			};

			$scope.queryForData = function () {
				var query = new neon.query.Query()
					.selectFrom($scope.databaseName, $scope.tableName);

				query = query.groupBy.apply(query, $scope.groupFields);

				var connection = connectionService.getActiveConnection();

				if(connection) {
					d3.select("#node-click-name").text("");
					connection.executeQuery(query, $scope.calculateGraphData);
				} else {
					d3.select("#node-click-name").text("No database connection.");
				}

			};

			$scope.calculateGraphData = function(response) {
				var data = response.data;
				//var data = [{name: "foo"},{name: "bar"},{name: "foo"}];

				//build nodes array
				var nodesIndexes = {};
				var nodes = [];
				var linksIndexes = {};
				var links = [];
				var node1;
				var node2;
				for(var i = 0; i < data.length; i++) {
					for(var field = 0; field < $scope.groupFields.length; field++) {
						if(!nodesIndexes[data[i][$scope.groupFields[field]]] ||
							!nodesIndexes[data[i][$scope.groupFields[field]]][$scope.groupFields[field]]) {

							if(!nodesIndexes[data[i][$scope.groupFields[field]]]) {
								nodesIndexes[data[i][$scope.groupFields[field]]] = {};
							}


							nodesIndexes[data[i][$scope.groupFields[field]]][$scope.groupFields[field]] = nodes.length;
							nodes.push({name: data[i][$scope.groupFields[field]], group:field});
						}
					}

					for(var field = 0; field < $scope.groupFields.length -1; field++) {
						node1 = nodesIndexes[data[i][$scope.groupFields[field]]][$scope.groupFields[field]];
						node2 = nodesIndexes[data[i][$scope.groupFields[field + 1]]][$scope.groupFields[field + 1]];

						if(!linksIndexes[node1] || !linksIndexes[node1][node2]) {
							if(!linksIndexes[node1]) {
								linksIndexes[node1] = {};
							}
							linksIndexes[node1][node2] = links.length;

							links.push({source: node1, target: node2, value: 1});
						}
					}
				}

				$scope.updateGraph({nodes: nodes, links: links});
			}

			$scope.uniqueId = (Math.floor(Math.random()*10000));
			$scope.svgId = "directed-svg-" + $scope.uniqueId;

			$scope.updateGraph = function(data) {
				var svg = d3.select("#" + $scope.svgId)
				if(svg) {
					svg.remove();
				}

				var width = 600,
				    height = 300;

				var color = d3.scale.category10();

				var force = d3.layout.force()
				    .charge(-10)
				    .linkDistance(30)
				    .size([width, height]);

				var svg = d3.select("#directed-graph-div-"+$scope.uniqueId)
				    .append("svg")
						.attr("id", $scope.svgId)
				      .attr({
				        "width": "100%",
				        "height": "100%"
				      })
				      .attr("viewBox", "0 0 " + width + " " + height )
				      .attr("preserveAspectRatio", "xMidYMid meet")
				      .attr("pointer-events", "all")
				    .call(d3.behavior.zoom().on("zoom", redraw));

				var vis = svg
				    .append('svg:g');

				function redraw() {
				  vis.attr("transform",
				      "translate(" + d3.event.translate + ")"
				      + " scale(" + d3.event.scale + ")");
				}

				  force
				      .nodes(data.nodes)
				      .links(data.links)
				      .start();

				  var link = vis.selectAll(".link")
				      .data(data.links)
				    .enter().append("line")
				      .attr("class", "link")
				      .style("stroke-width", function(d) { return Math.sqrt(d.value); });

				  var node = vis.selectAll(".node")
				      .data(data.nodes)
				    .enter().append("circle")
				      .attr("class", "node")
				      .attr("r", 5)
				      .style("fill", function(d) { return color(d.group); })
				      .call(force.drag);

					node.on("click", function(d, i) {
						d3.select("#node-click-name").text(d.name);
					});

				  node.append("title")
				      .text(function(d) { return d.name; });

				  force.on("tick", function() {
				    link.attr("x1", function(d) { return d.source.x; })
				        .attr("y1", function(d) { return d.source.y; })
				        .attr("x2", function(d) { return d.target.x; })
				        .attr("y2", function(d) { return d.target.y; });

				    node.attr("cx", function(d) { return d.x; })
				        .attr("cy", function(d) { return d.y; });
				  });
			};



			// Wait for neon to be ready, the create our messenger and intialize the view and data.
			neon.ready(function () {
				$scope.initialize();
			});

		}
	};
}]);
