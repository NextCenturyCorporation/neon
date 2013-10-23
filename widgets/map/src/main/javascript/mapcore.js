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
    this.selector = $(elementId);
    this.data = opts.data;

    this.latitudeMapping = opts.latitudeMapping || coreMap.Map.DEFAULT_LATITUDE_MAPPING;
    this.longitudeMapping = opts.longitudeMapping || coreMap.Map.DEFAULT_LONGITUDE_MAPPING;
    this.sizeMapping = opts.sizeMapping || coreMap.Map.DEFAULT_SIZE_MAPPING;
    this.colorMapping = opts.colorMapping;

    this.colorScale = d3.scale.category20();
    this.responsive = true;

    if(opts.responsive === false){
        this.responsive = false;
    }

    if (this.responsive) {
        this.redrawOnResize();
        this.width = this.selector.width();
        this.height = this.selector.height();
    }
    else {
        this.width = opts.width || coreMap.Map.DEFAULT_WIDTH;
        this.userSetWidth = this.width;
        this.height = opts.height || coreMap.Map.DEFAULT_HEIGHT;
        this.userSetHeight = this.height;
    }

    this.initializeMap();
    this.setupLayers();
    this.map.zoomToMaxExtent();
    this.heatmapLayer.toggle();
};

coreMap.Map.DEFAULT_DATA_LIMIT = 8000;

coreMap.Map.DEFAULT_WIDTH = 1024;
coreMap.Map.DEFAULT_HEIGHT = 680;
coreMap.Map.DEFAULT_LATITUDE_MAPPING = "latitude";
coreMap.Map.DEFAULT_LONGITUDE_MAPPING = "longitude";
coreMap.Map.DEFAULT_SIZE_MAPPING = "count_";

coreMap.Map.DEFAULT_OPACITY = 0.8;
coreMap.Map.DEFAULT_STROKE_WIDTH = 1;
coreMap.Map.DEFAULT_COLOR = "#00ff00";
coreMap.Map.DEFAULT_STROKE_COLOR = "#ffffff";
coreMap.Map.MIN_RADIUS = 3;
coreMap.Map.MAX_RADIUS = 13;

/**
 * Draws the map data
 * @method draw
 */

coreMap.Map.prototype.draw = function(){
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
 * Resets the map. This clears all the data, zooms all the way out and centers the map.
 * @method reset
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
 * @method setData
 */

coreMap.Map.prototype.setData = function(mapData){
    if(mapData.length >= coreMap.Map.DEFAULT_DATA_LIMIT){
        console.log(mapData.length);
        console.error("Unable to set data. The map cannot handle more than " + DEFAULT_DATA_LIMIT + " points");
    }
    else{
        this.data = mapData;
    }
};

/**
 * Toggles visibility between the points layer and heatmap layer.
 * @method toggleLayers
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
 * @method getExtent
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
 * Sets the viewable extent of the map.
 * @param {Object} extent An object containing the bounds of the viewable extent.
 * @method zoomToExtent
 */

coreMap.Map.prototype.zoomToExtent = function(extent) {
    var llPoint = new OpenLayers.LonLat(extent['minimumLongitude'], extent['minimumLatitude']);
    var urPoint = new OpenLayers.LonLat(extent['maximumLongitude'], extent['maximumLatitude']);

    llPoint.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
    urPoint.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
    var minLon = Math.min(llPoint.lon, urPoint.lon);
    var maxLon = Math.max(llPoint.lon, urPoint.lon);
    var minLat = Math.min(llPoint.lat, urPoint.lat);
    var maxLat = Math.max(llPoint.lat, urPoint.lat);

    this.map.zoomToExtent([minLon, minLat, maxLon, maxLat]);
};

/**
 * Registers a listener for a particular map event.
 * @param {String} type A map event type.
 * @param {Object} obj An object that the listener should be registered on.
 * @param {Function} listener A function to be called when the event occurs.
 * @method register
 */

coreMap.Map.prototype.register = function(type, obj, listener) {
    this.map.events.register(type, obj, listener);
};


/**
 * Creates a point to be added to the heatmap layer.
 * @param {Object} element One data element of the map's data array.
 * @param {number} longitude The longitude value of the data element
 * @param {number} latitude The latitude value of the data element.
 * @return {Object} an object containing the location and count for the heatmap.
 * @method createHeatmapDataPoint
 */

coreMap.Map.prototype.createHeatmapDataPoint = function(element, longitude, latitude){
    var count = this.getValueFromDataElement(this.sizeMapping, element);
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
 * @method createPointsLayerDataPoint
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
 * @return {OpenLayers.Symbolizer.Point} The style object
 * @method stylePoint
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
 * @return {OpenLayers.Symbolizer.Point} The style object
 * @method createPointStyleObject
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
 * @return {number} The radius
 * @method calculateRadius
 */

coreMap.Map.prototype.calculateRadius = function(element){
    var maxValue = this.maxValue(this.data, this.sizeMapping);
    if(maxValue < 1){
        return coreMap.Map.MIN_RADIUS;
    }

    var size = this.getValueFromDataElement(this.sizeMapping, element);
    var radius = coreMap.Map.MIN_RADIUS;
    if(size > 1) {
        radius = (10/Math.log(maxValue) * Math.log(size)) + coreMap.Map.MIN_RADIUS;
    }
    return radius;
};

/**
 * Calculate the desired color of a point.
 * @param {Object} element One data element of the map's data array.
 * @return {String} The color
 * @method calculateColor
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
 * @return {number} The minimum value in the data
 * @method minValue
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
 * @return {number} The maximum value in the data
 * @method maxValue
 */

coreMap.Map.prototype.maxValue = function(data, mapping) {
    var me = this;
    return d3.max(data, function (el) {
        return me.getValueFromDataElement(mapping, el);
    });
};

/**
 * Gets a value from a data element using a mapping string or function.
 * @param {String | Function} mapping The mapping from data element object to value.
 * @param {Object} element An element of the data array.
 * @return The value in the data element.
 * @method getValueFromDataElement
 */

coreMap.Map.prototype.getValueFromDataElement = function(mapping, element){
    if (typeof mapping === 'function') {
        return mapping.call(this, element);
    }
    return element[mapping];
};

/**
 * Initializes the map.
 * @method initializeMap
 */

coreMap.Map.prototype.initializeMap = function(){
    $('#' + this.elementId).css({
        width: this.width,
        height: this.height
    });
    this.map = new OpenLayers.Map(this.elementId);
};

/**
 * Initializes the map layers and adds the base layer.
 * @method setupLayers
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


    this.map.addLayer(this.pointsLayer);
    this.map.addLayer(this.heatmapLayer);
    this.currentLayer = this.pointsLayer;
};

/**
 * Determine the width of the map.
 * @param {Object} selector The jquery selector of the map element
 * @method determineWidth
 */

coreMap.Map.prototype.determineWidth = function (selector) {
    if (this.userSetWidth) {
        return this.userSetWidth;
    }
    else if (selector.width() && selector.width() !== 0) {
        selector.css("width", "100%");
        return selector.width();
    }
    return coreMap.Map.DEFAULT_WIDTH;
};

/**
 * Determine the height of the map.
 * @param {Object} selector The jquery selector of the map element
 * @method determineHeight
 */

coreMap.Map.prototype.determineHeight = function (selector) {
    if (this.userSetHeight) {
        return this.userSetHeight;
    }
    else if (selector.height() && selector.height() !== 0) {
        selector.css("height", "90%");
        return selector.height();
    }
    return coreMap.Map.DEFAULT_HEIGHT;
};

/**
 * Add a resize listener on the window to redraw the map
 * @method redrawOnResize
 */

coreMap.Map.prototype.redrawOnResize = function () {
    var me = this;

    function drawChart() {
        me.draw();
    }

    //Debounce is needed because browser resizes fire this resize event multiple times.
    $(window).resize(function () {
        me.width = me.determineWidth(me.selector);
        me.height = me.determineHeight(me.selector);

        $("canvas").css("height", me.height);
        $("canvas").css("width", me. width);

        _.debounce(drawChart, 100);
    });

};