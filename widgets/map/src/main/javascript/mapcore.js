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

var coreMap = coreMap || {};

/**
 * Creates a new map component.
 * @class Map
 * @namespace coreMap

 * @param {String} elementId id of a div or span which the map component will replace.
 * @param {Object} opts A collection of optional key/value pairs used for configuration parameters:
 * <ul>
 *     <li>data - An array of data to display in the map</li>
 *     <li>width - The width of the map.</li>
 *     <li>height - The height of the map.</li>
 *     <li>latitudeMapping {function | String} - A way to map the data element to latitude.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>longitudeMapping {function | String} - A way to map the data element to longitude.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>sizeMapping {function | String} - A way to map the data element to size.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>colorMapping {function | String} - A way to map the data element to color.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *
 * </ul>
 *
 * @constructor
 *
 * @example
 *     var data = [
 *                 {"latitude": "50", "longitude": -100},
 *                 {"latitude": "-20", "longitude": 130
 *                ];
 *     var map = new coreMap.Map('map');
 *     map.setData(data);
 *     map.draw();
 *
 * @example
 *     var opts = {
 *            data: {[50,30,5], [20,-120,10]},
 *            latitudeMapping: function(element){ return element[0]; },
 *            longitudeMapping: function(element){ return element[1]; },
 *            sizeMapping: function(element){ return element[2]; }
 *                };
 *     var map = new coreMap.Map('map', opts);
 *     map.draw();
 *
**/

coreMap.Map = function(elementId, opts){
    opts = opts || {};

    this.elementId = elementId;
    this.width = opts.width || coreMap.Map.DEFAULT_WIDTH;
    this.height = opts.height || coreMap.Map.DEFAULT_HEIGHT;
    this.data = opts.data;
    this.latitudeMapping = opts.latitudeMapping || coreMap.Map.DEFAULT_LATITUDE_MAPPING;
    this.longitudeMapping = opts.longitudeMapping || coreMap.Map.DEFAULT_LONGITUDE_MAPPING;
    this.sizeMapping = opts.sizeMapping || coreMap.Map.DEFAULT_SIZE_MAPPING;
    this.colorMapping = opts.colorMapping;

    this.colorScale = d3.scale.category20();

    this.rendered = false;

    this.initializeMap();
    this.setupLayers();
};

coreMap.Map.DEFAULT_WIDTH = 1024;
coreMap.Map.DEFAULT_HEIGHT = 680;
coreMap.Map.DEFAULT_LATITUDE_MAPPING = "latitude";
coreMap.Map.DEFAULT_LONGITUDE_MAPPING = "longitude";
coreMap.Map.DEFAULT_SIZE_MAPPING = "count_";

coreMap.Map.DEFAULT_OPACITY = 0.8;
coreMap.Map.DEFAULT_STROKE_WIDTH = 0.5;
coreMap.Map.DEFAULT_COLOR = "#00ff00";
coreMap.Map.DEFAULT_STROKE_COLOR = "#ffffff";
coreMap.Map.MIN_RADIUS = 3;
coreMap.Map.MAX_RADIUS = 20;

/**
 * Draws the map. The first time this is called, it will render the map.
 * Subsequent times it is called it will redraw the map's data.
 */

coreMap.Map.prototype.draw = function(){
    if(!this.rendered){
        this.sizeMapContainer();
        this.map.render(this.elementId);
        this.map.zoomToMaxExtent();
        this.rendered = true;
        this.map.addLayer(this.pointsLayer);
        this.map.addLayer(this.heatmapLayer);
        this.currentLayer = this.pointsLayer;
        this.heatmapLayer.toggle();
    }
    this.addDataToLayers();
};

/**
 * Resets the map. This clears all the data, zooms all the way out and centers the map.
 */

coreMap.Map.prototype.reset = function(){
    this.setData([]);
    this.draw();
    this.map.setCenter(new OpenLayers.LonLat(0, 0));
    this.map.zoomToMaxExtent();

};

/**
 * Sets the map's data.
 * @param mapData the data to be set. This should be an array of points. The points may be specified
 * in any way, This component uses the mapping objects to map each array element to latitude, longitude, size and color.
 */

coreMap.Map.prototype.setData = function(mapData){
    this.data = mapData;
};


/**
 * Sets the mapping between a data element and latitude.
 * @param {String | function} mapping can be a function or string. Use a string if the latitude has a name in the data element object.
 * Use a function for any other mapping between a data element and latitude.
 */

coreMap.Map.prototype.setLatitudeMapping = function(mapping){
    this.latitudeMapping = mapping;
};

/**
 * Sets the mapping between a data element and longitude.
 * @param {String | function} mapping can be a function or string. Use a string if the longitude has a name in the data element object.
 * Use a function for any other mapping between a data element and longitude.
 */

coreMap.Map.prototype.setLongitudeMapping = function(mapping){
    this.longitudeMapping = mapping;
};

/**
 * Sets the mapping between a data element and size.
 * @param {String | function} mapping can be a function or string. Use a string if the size has a name in the data element object.
 * Use a function for any other mapping between a data element and size.
 */

coreMap.Map.prototype.setSizeMapping = function(mapping){
    this.sizeMapping = mapping;
};

/**
 * Sets the mapping between a data element and color.
 * @param {String | function} mapping can be a function or string. Use a string if the category for color
 * has a name in the data element object. Use a function for any other mapping between a data element and color category.
 */

coreMap.Map.prototype.setColorMapping = function(mapping){
    this.colorMapping = mapping;
};

/**
 * Toggles visibility between the points layer and heatmap layer.
 */

coreMap.Map.prototype.toggleLayers = function(){
    if(this.currentLayer === this.pointsLayer){
        this.pointsLayer.setVisibility(false);
        this.heatmapLayer.toggle();
        this.currentLayer = this.heatmapLayer;
    }
    else{
        this.heatmapLayer.toggle();
        this.pointsLayer.setVisibility(true);
        this.currentLayer = this.pointsLayer;
    }
};

/**
 * Get the current viewable extent.
 * @return {Object} An object that contains the minimum and maximum latitudes and longitudes currently shown.
 */

coreMap.Map.prototype.getExtent = function(){
    var extent = this.map.getExtent();
    var llPoint = new OpenLayers.LonLat(extent.left, extent.bottom);
    var urPoint = new OpenLayers.LonLat(extent.right, extent.top);
    var proj1 = new OpenLayers.Projection("EPSG:4326");
    var proj2 = new OpenLayers.Projection("EPSG:900913");
    llPoint.transform(proj2, proj1);
    urPoint.transform(proj2, proj1);
    var minLon = Math.min(llPoint.lon, urPoint.lon);
    var maxLon = Math.max(llPoint.lon, urPoint.lon);

    var minLat = Math.min(llPoint.lat, urPoint.lat);
    var maxLat = Math.max(llPoint.lat, urPoint.lat);

    return {
        minimumLatitude: minLat,
        minimumLongitude: minLon,
        maximumLatitude: maxLat,
        maximumLongitude: maxLon
    };
};

/**
 * Takes the map's data and plots it on each layer.
 */

coreMap.Map.prototype.addDataToLayers = function(){
    var me = this;

    var heatmapData = [];
    var mapData = [];
    _.each(this.data, function (element) {
        var longitude = me.getValueFromDataElement(me.longitudeMapping, element);
        var latitude = me.getValueFromDataElement(me.latitudeMapping, element);

        heatmapData.push(me.createHeatmapDataPoint(element, longitude, latitude));
        mapData.push(me.createPointsLayerDataPoint(element, longitude, latitude));
    });


    me.heatmapLayer.setDataSet({ max: 1, data: heatmapData});
    me.pointsLayer.removeAllFeatures();
    me.pointsLayer.addFeatures(mapData);
};


/**
 * Creates a point to be added to the heatmap layer.
 * @return {Object} an object containing the location and count for the heatmap.
 */

coreMap.Map.prototype.createHeatmapDataPoint = function(element, longitude, latitude){
    var count = this.getValueFromDataElement(coreMap.Map.DEFAULT_SIZE_MAPPING, element);
    var point = new OpenLayers.LonLat(longitude, latitude);

    return {
        lonlat: point,
        count: count
    };
};

/**
 * Creates a point to be added to the points layer, styled appropriately.
 * @param {Object} element One data element of the map's data array.
 * @param {number} longitude The longitude value of the data element
 * @param {number} latitude The latitude value of the data element.
 * @return {OpenLayers.Feature.Vector} the point to be added.
 */

coreMap.Map.prototype.createPointsLayerDataPoint = function(element, longitude, latitude){
    var point = new OpenLayers.Geometry.Point(longitude, latitude);
    point.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
    var feature = new OpenLayers.Feature.Vector(point);
    feature.style = this.stylePoint(element);
    return feature;
};

/**
 * Styles the data element based on the size and color.
 * @param {Object} element One data element of the map's data array.
 * @returns {OpenLayers.Symbolizer.Point} The style object
 */

coreMap.Map.prototype.stylePoint = function(element){
    var radius = this.calculateRadius(element);
    var color = this.calculateColor(element);

    return this.createPointStyleObject(color, radius);
};

/**
 * Creates the style object for a point
 * @param {String} color The color of the point
 * @param {number} radius The radius of the point
 * @returns {OpenLayers.Symbolizer.Point} The style object
 */

coreMap.Map.prototype.createPointStyleObject = function(color, radius){
    color = color || coreMap.Map.DEFAULT_COLOR;
    radius = radius || coreMap.Map.MIN_RADIUS;

    return new OpenLayers.Symbolizer.Point({
        fillColor: color,
        fillOpacity: coreMap.Map.DEFAULT_OPACITY,
        strokeOpacity: coreMap.Map.DEFAULT_OPACITY,
        strokeWidth: coreMap.Map.DEFAULT_STROKE_WIDTH,
        stroke: coreMap.Map.DEFAULT_STROKE_COLOR,
        pointRadius: radius
    });
};

/**
 * Calculate the desired radius of a point.
 * @param {Object} element One data element of the map's data array.
 * @returns {number} The radius
 */

coreMap.Map.prototype.calculateRadius = function(element){
    var minValue = this.minValue(this.data, this.sizeMapping);
    if(minValue < 1){
        minValue = 1;
    }

    var size = this.getValueFromDataElement(this.sizeMapping, element);
    var radius = coreMap.Map.MIN_RADIUS;
    if(size > 1) {
        radius = Math.log(size - minValue + 1) + coreMap.Map.MIN_RADIUS;
    }
    return radius;
};

/**
 * Calculate the desired color of a point.
 * @param {Object} element One data element of the map's data array.
 * @returns {String} The color
 */

coreMap.Map.prototype.calculateColor = function(element){
    var category = this.getValueFromDataElement(this.colorMapping, element);

    if(!category){
        return coreMap.Map.DEFAULT_COLOR;
    }
    return this.colorScale(category);
};

/**
 * Calculate the minimum value of the data, using one of the mapping functions.
 * @param {Object} data The array of data elements.
 * @param {String | Function} mapping The mapping from data element object to value.
 * @returns {number} The minimum value in the data
 */

coreMap.Map.prototype.minValue = function(data, mapping) {
    var me = this;
    return d3.min(data, function (el) {
        return me.getValueFromDataElement(mapping, el);
    });
};

/**
 * Calculate the maximum value of the data, using one of the mapping functions.
 * @param {Object} data The array of data elements.
 * @param {String | Function} mapping The mapping from data element object to value.
 * @returns {number} The maximum value in the data
 */

coreMap.Map.prototype.maxValue = function(data, mapping) {
    var me = this;
    return d3.max(data, function (el) {
        return me.getValueFromDataElement(mapping, el);
    });
};

/**
 * Calculate the maximum value of the data, using one of the mapping functions.
 * @param {Object} data The array of data elements.
 * @param {String | Function} mapping The mapping from data element object to value.
 * @returns {number} The maximum value in the data
 */

coreMap.Map.prototype.getValueFromDataElement = function(mapping, element){
    if (typeof mapping === 'function') {
        return mapping.call(this, element);
    }
    return element[mapping];
};

/**
 * Sets the size of the parent div or span of the map using the map's width and height.
 */

coreMap.Map.prototype.sizeMapContainer = function(){
   $('#' + this.elementId).css({
       width: this.width,
       height: this.height
   });
};

/**
 * Initializes the map.
 */

coreMap.Map.prototype.initializeMap = function(){
    this.map = new OpenLayers.Map();
    //We need to set this size object before initializing the heatmap.
    this.map.size = new OpenLayers.Size(this.width, this.height);
};

/**
 * Initializes the map layers and adds the base layer.
 */

coreMap.Map.prototype.setupLayers = function(){
    var baseLayer = new OpenLayers.Layer.OSM();
    this.map.addLayer(baseLayer);

    var style = {
        styleMap: new OpenLayers.StyleMap(OpenLayers.Util.applyDefaults(
            {fillColor: "#00FF00", fillOpacity: 0.8, strokeOpacity: 0.8, strokeWidth: 1, pointRadius: 4},
            OpenLayers.Feature.Vector.style["default"]
        ))
    };
    this.pointsLayer = new OpenLayers.Layer.Vector("Points Layer", style);

    var heatmapOptions = {visible: true, radius: 10};
    var options = {isBaseLayer: false, opacity: 0.3, projection: new OpenLayers.Projection("EPSG:4326")};
    this.heatmapLayer = new OpenLayers.Layer.Heatmap("Heatmap Layer", this.map, baseLayer, heatmapOptions, options);
};
