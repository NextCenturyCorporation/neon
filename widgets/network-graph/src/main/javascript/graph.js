/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

var graphs = graphs || {};


/**
 *
 * Creates a new graph/network display component
 * @namespace graphs
 * @class Graph
 * @param {String} graphSelector The selector for the component in which the graph will be drawn
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
 *                  {"id": 0, "xPos": 960, "yPos": 320},
 *                  {"id": 1, "xPos": 513, "yPos": 335},
 *                  {"id": 2, "xPos": 557, "yPos": 454},
 *                  {"id": 3, "xPos": 506, "yPos": 642},
 *                  {"id": 4, "xPos": 547, "yPos": 660},
 *                  {"id": 5, "xPos": 695, "yPos": 705}
 *                ];
 *
 *     var edges = [
 *                  {"source": 1, "target": 0},
 *                  {"source": 2, "target": 0},
 *                  {"source": 3, "target": 4}
 *                 ];
 *
 *     var opts = {"x":"xPos","y":"yPos"};
 *
 *     var graph = new graphs.Graph("#graph", opts);
 *     graph.draw(nodes, edges);
 */
graphs.Graph = function (graphSelector, opts) {

    this.graphSelector_ = graphSelector;
    this.xAttr_ = opts.x || "x";
    this.yAttr_ = opts.y || "y";
    this.id_ = opts.id || "id";
    this.nodeRadius_ = opts.radius || 6;
    this.svg_ = undefined;
    this.nodes_ = undefined;
    this.edges_ = undefined;
    this.x_ = undefined;
    this.y_ = undefined;
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
 * @param {Array} nodes An array of node obecets. The nodes just need attributes for x/y/id
 * @param {Array} edgeList
 * @method
 */
graphs.Graph.prototype.draw = function (nodes, edgeList) {
    this.nodeInfo_ = this.buildNodeInfoMap_(nodes);

    var graph = $(this.graphSelector_);
    this.width_ = graph.width();
    this.height_ = graph.height();

    this.x_ = d3.scale.linear().domain([0, this.width_]).range([0, this.width_]);
    this.y_ = d3.scale.linear().domain([0, this.height_]).range([0, this.height_]);

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
    nodes.forEach(function (d) {
        var nodeInfo = {};
        nodeInfo.node = d;
        nodeInfo.translate = [0, 0];
        map[d[me.id_]] = nodeInfo;
    });
    return map;
};

graphs.Graph.prototype.initSvg_ = function () {
    if (!this.svg_) {
        var me = this;
        this.svg_ = d3.select(this.graphSelector_).append("svg")
            .attr("width", this.width_).attr("height", this.height_)
            .append("g")
            // add semantic zooming behavior - adapted from http://bl.ocks.org/mbostock/3680957
            .call(d3.behavior.zoom()
                .x(this.x_).y(this.y_).scaleExtent([1, 8])
                .on("zoom", function () {
                    me.zoom_.call(me);
                }));

        // intercepts mouse events for zooming
        this.svg_.append("rect").attr("class", "overlay").attr("width", this.width_).attr("height", this.height_);
    }
};

graphs.Graph.prototype.drawEdges_ = function (edgeList) {
    var edgeNodes = this.mapEdgeListIdsToNodes_(edgeList);
    this.drawSVGEdges_(edgeNodes);
};

graphs.Graph.prototype.mapEdgeListIdsToNodes_ = function (edgeList) {
    var me = this;
    var edgeNodes = [];
    edgeList.forEach(function (d) {
        var sourceNode = me.nodeInfo_[d.source].node;
        var targetNode = me.nodeInfo_[d.target].node;
        edgeNodes.push({ source: sourceNode, target: targetNode});
    });
    return edgeNodes;
};

graphs.Graph.prototype.drawSVGEdges_ = function (edgeNodes) {
    var me = this;
    this.edges_ = this.svg_.append("g")
        .selectAll(".edge")
        .data(edgeNodes);
    this.edges_.enter().append("line")
        .attr("class", "edge")
        .attr("x1", function (d) {
            return d.source[me.xAttr_];
        })
        .attr("y1", function (d) {
            return d.source[me.yAttr_];
        })
        .attr("x2", function (d) {
            return d.target[me.xAttr_];
        })
        .attr("y2", function (d) {
            return d.target[me.yAttr_];
        });
    this.edges_.exit().remove();

};

graphs.Graph.prototype.drawNodes_ = function (nodes) {
    var me = this;
    this.nodes_ = this.svg_.selectAll("circle").data(nodes);
    // put the nodes in a group so we can just drag the whole group
    var g = this.nodes_.enter().append("g")
        .attr("transform", function (d) {
            return me.transform_.call(me, d);
        })
        .call(
            d3.behavior.drag()
                .on('drag', function (node) {
                    me.dragNode_.call(me, node, d3.select(this));
                    me.dragNodeEdges_.call(me, node);
                })
        );
    g.append("circle")
        .attr("class", "node")
        .attr("r", this.nodeRadius_);

    this.nodes_.exit().remove();

};

graphs.Graph.prototype.dragNode_ = function (node, nodeGroup) {
    var me = this;
    node[this.xAttr_] += d3.event.dx / this.scale_;
    node[this.yAttr_] += d3.event.dy / this.scale_;
    nodeGroup.attr("transform", function (d) {
        return me.transform_.call(me, d);
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
    this.nodes_.attr("transform",
        function (d) {
            return me.transform_.call(me, d);
        }
    );

    this.edges_
        .attr("x1", function (d) {
            return me.getTranslation_(d.source)[0];
        })
        .attr("y1", function (d) {
            return me.getTranslation_(d.source)[1];
        })
        .attr("x2", function (d) {
            return me.getTranslation_(d.target)[0];
        })
        .attr("y2", function (d) {
            return me.getTranslation_(d.target)[1];
        });

};

graphs.Graph.prototype.transform_ = function (d) {
    if (d3.event && d3.event.type === "zoom") {
        this.scale_ = d3.event.scale;
    }
    var translate = [this.x_(d[this.xAttr_]), this.y_(d[this.yAttr_])];
    var nodeId = d[this.id_];
    var nodeTranslate = this.nodeInfo_[nodeId].translate;
    nodeTranslate[0] = translate[0];
    nodeTranslate[1] = translate[1];
    return "translate(" + nodeTranslate + ")";
};

