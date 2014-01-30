/*
 * Copyright 2013 Next Century Corporation
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



var graphs = graphs || {};


/**
 *
 * Creates a new graph/network display component
 * @namespace graphs
 * @class Graph
 * @param {String} graphSelector The selector for the component in which the graph will be drawn. This should
 * be a container element (e.g. a div), not the drawing element (e.g. svg/canvas).
 * @param {Object} opts A collection of key/value pairs used for configuration parameters.
 * <ul>
 *     <li>id (optional) - The attribute in the data that contains a unique id for the node. This is used
 *     by the edge list to indicate what nodes are linked. Defaults to `id`</li>
 *     <li>x (optional) - The attribute in the data that contains the x position of the node - defaults to `x`</li>
 *     <li>y (optional) - The attribute in the data that contains the y position of the node - defaults to `y`</li>
 *     <li>radius (optional) - The default radius of a node in pixels. If not defined, a pre-configured default
 *     will be used</li>
 * </ul>
 *
 *
 * @constructor
 * @example
 *    var nodes = [
 *                  {"id": 0, "xPosition": 960, "yPosition": 320},
 *                  {"id": 1, "xPosition": 513, "yPosition": 335},
 *                  {"id": 2, "xPosition": 557, "yPosition": 454},
 *                  {"id": 3, "xPosition": 506, "yPosition": 642},
 *                  {"id": 4, "xPosition": 547, "yPosition": 660},
 *                  {"id": 5, "xPosition": 695, "yPosition": 705}
 *                ];
 *
 *     var edges = [
 *                  {"source": 1, "target": 0},
 *                  {"source": 2, "target": 0},
 *                  {"source": 3, "target": 4}
 *                 ];
 *
 *     var opts = {"xPos":"xPosition","yPos":"yPosition"};
 *
 *     var graph = new graphs.Graph("#graph", opts);
 *     graph.draw(nodes, edges);
 */
graphs.Graph = function (graphSelector, opts) {

    this.graphSelector_ = graphSelector;
    this.xAttr_ = opts.xPos || "xPos";
    this.yAttr_ = opts.yPos || "yPos";
    this.id_ = opts.id || "id";
    this.nodeRadius_ = opts.radius || 6;
    this.svg_ = undefined;
    this.nodes_ = undefined;
    this.edges_ = undefined;
    this.xPos_ = undefined;
    this.yPos_ = undefined;
    this.height_ = 0;
    this.width_ = 0;

    // maps a node id to the node and information about its current translated coordinates
    this.nodeInfo_ = {};
    this.scale_ = 1;
};

/**
 * Draws a graph with the specified nodes and edges. The nodes are an array of objects with a list of x/y coordinates
 * and a node id. The edge list is just a list of node id's as a source/target json pair. See the example documentation
 * at the top of this class for how to create and draw a graph
 * @param {Array} nodes An array of node objects. The nodes just need attributes for x/y/id
 * @param {Array} edgeList
 * @method
 */
graphs.Graph.prototype.draw = function (nodes, edgeList) {
    this.nodeInfo_ = this.buildNodeInfoMap_(nodes);

    var graph = $(this.graphSelector_);
    this.width_ = graph.width();
    this.height_ = graph.height();

    this.xPos_ = d3.scale.linear().domain([0, this.width_]).range([0, this.width_]);
    this.yPos_ = d3.scale.linear().domain([0, this.height_]).range([0, this.height_]);

    this.initSvg_();

    // note the drawing of edges/nodes all use transforms to move the objects, not absolute positions. this approach
    // works better in coordinating zoom/pan transforms with node positions

    // draw the edges first so they show up below the nodes
    this.drawEdges_(edgeList);
    this.drawNodes_(nodes);
};

graphs.Graph.prototype.buildNodeInfoMap_ = function (nodes) {
    var me = this;
    var map = {};
    nodes.forEach(function (node) {
        var nodeInfo = {};
        nodeInfo.node = node;
        nodeInfo.translate = [0, 0];
        map[node[me.id_]] = nodeInfo;
    });
    return map;
};

graphs.Graph.prototype.initSvg_ = function () {
    // reset svg so that draw can be called multiple times with new edges and nodes.
    // only the last draw operation remains.  In other words, the effect of calling
    // graph.draw() multiple times is not additive.
    d3.select("svg").remove();
    this.svg_ = undefined;

    this.svg_ = d3.select(this.graphSelector_).append("svg")
        .attr("width", this.width_).attr("height", this.height_)
        .append("g")
        // add semantic zooming behavior - adapted from http://bl.ocks.org/mbostock/3680957
        .call(d3.behavior.zoom()
            .x(this.xPos_).y(this.yPos_).scaleExtent([1, 8])
            .on("zoom",
                $.proxy(this.zoom_, this)
            ));

    // intercepts mouse events for zooming
    this.svg_.append("rect").attr("class", "overlay").attr("width", this.width_).attr("height", this.height_);
};

graphs.Graph.prototype.drawEdges_ = function (edgeList) {
    var edgeNodes = this.mapEdgeListIdsToNodes_(edgeList);
    this.drawSVGEdges_(edgeNodes);
};

graphs.Graph.prototype.mapEdgeListIdsToNodes_ = function (edgeList) {
    var me = this;
    var edgeNodes = [];
    edgeList.forEach(function (edge) {
        var sourceNode = me.nodeInfo_[edge.source].node;
        var targetNode = me.nodeInfo_[edge.target].node;
        edgeNodes.push({ source: sourceNode, target: targetNode});
    });
    return edgeNodes;
};

graphs.Graph.prototype.drawSVGEdges_ = function (edgeNodes) {
    var me = this;
    this.edges_ = this.svg_.selectAll(".edge").data(edgeNodes);
    this.edges_.enter().append("line")
        .attr("class", "edge")
        .attr("x1", function (edge) {
            return edge.source[me.xAttr_];
        })
        .attr("y1", function (edge) {
            return edge.source[me.yAttr_];
        })
        .attr("x2", function (edge) {
            return edge.target[me.xAttr_];
        })
        .attr("y2", function (edge) {
            return edge.target[me.yAttr_];
        });
    this.edges_.exit().remove();

};

graphs.Graph.prototype.drawNodes_ = function (nodes) {
    var me = this;
    this.nodes_ = this.svg_.selectAll(".node").data(nodes);
    this.nodes_.enter().append("circle")
        .attr("transform", function (node) {
            return me.transform_.call(me, node);
        })
        .call(
            d3.behavior.drag()
                .on("drag", function (node) {
                    me.dragNode_.call(me, node, d3.select(this));
                    me.dragNodeEdges_.call(me, node);
                })
        )
        .attr("class", "node")
        .attr("r", this.nodeRadius_);

    this.nodes_.exit().remove();

};

graphs.Graph.prototype.dragNode_ = function (node, svgNode) {
    var me = this;
    node[this.xAttr_] += d3.event.dx / this.scale_;
    node[this.yAttr_] += d3.event.dy / this.scale_;
    svgNode.attr("transform", function (node) {
        return me.transform_.call(me, node);
    });
};

graphs.Graph.prototype.dragNodeEdges_ = function (node) {
    var me = this;

    me.edges_.filter(function (edge) {
        return edge.source === node;
    })
        .attr("x1", function (edge) {
            return me.getTranslation_(edge.source)[0];
        })
        .attr("y1", function (edge) {
            return me.getTranslation_(edge.source)[1];
        });

    me.edges_.filter(function (edge) {
        return edge.target === node;
    })
        .attr("x2", function (edge) {
            return me.getTranslation_(edge.target)[0];
        })
        .attr("y2", function (edge) {
            return me.getTranslation_(edge.target)[1];
        });
};

graphs.Graph.prototype.getTranslation_ = function (node) {
    return this.nodeInfo_[node[this.id_]].translate;
};

graphs.Graph.prototype.zoom_ = function () {
    var me = this;
    me.nodes_.attr("transform",
        function (node) {
            return me.transform_.call(me, node);
        }
    );

    me.edges_
        .attr("x1", function (edge) {
            return me.getTranslation_(edge.source)[0];
        })
        .attr("y1", function (edge) {
            return me.getTranslation_(edge.source)[1];
        })
        .attr("x2", function (edge) {
            return me.getTranslation_(edge.target)[0];
        })
        .attr("y2", function (edge) {
            return me.getTranslation_(edge.target)[1];
        });

};

graphs.Graph.prototype.transform_ = function (node) {
    if (d3.event && d3.event.type === "zoom") {
        this.scale_ = d3.event.scale;
    }
    var translate = [this.xPos_(node[this.xAttr_]), this.yPos_(node[this.yAttr_])];
    var nodeId = node[this.id_];
    var nodeTranslate = this.nodeInfo_[nodeId].translate;
    nodeTranslate[0] = translate[0];
    nodeTranslate[1] = translate[1];
    return "translate(" + nodeTranslate + ")";
};