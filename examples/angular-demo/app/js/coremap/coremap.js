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


var coreMap = coreMap || {};

/**
 * Creates a new map component.
 * @class Map
 * @namespace coreMap

 * @param {String} elementId id of a div or span which the map component will replace.
 * @param {Object} opts A collection of optional key/value pairs used for configuration parameters:
 * <ul>
 *     <li>data - An array of data to display in the map</li>
 *     <li>width - The width of the map in pixels.</li>
 *     <li>height - The height of the map in pixels.</li>
 *     <li>latitudeMapping {function | String} - A way to map the data element to latitude.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>longitudeMapping {function | String} - A way to map the data element to longitude.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>sizeMapping {function | String} - A way to map the data element to size.
 *     This could be a string name for a simple mapping or a function for a more complex one.</li>
 *     <li>categoryMapping {function | String} - A way to map the data element to color.
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

coreMap.Map = function (elementId, opts) {
    opts = opts || {};

    this.elementId = elementId;
    this.selector = $(elementId);

    // mapping of categories to colors
    this.colors = {};

    this.latitudeMapping = opts.latitudeMapping || coreMap.Map.DEFAULT_LATITUDE_MAPPING;
    this.longitudeMapping = opts.longitudeMapping || coreMap.Map.DEFAULT_LONGITUDE_MAPPING;
    this.sizeMapping = opts.sizeMapping || coreMap.Map.DEFAULT_SIZE_MAPPING;

    this.categoryMapping = opts.categoryMapping;
    this.onZoomRect = opts.onZoomRect;

    //this.colorScale = d3.scale.category20();
    this.colorRange = [
        '#39b54a',
        '#C23333',
        '#3662CC',
        "#ff7f0e",
        "#9467bd",
        "#8c564b",
        "#e377c2",
        "#7f7f7f",
        "#bcbd22",
        "#17becf",
        "#98df8a",
        "#ff9896",
        "#aec7e8",
        "#ffbb78",
        "#c5b0d5",
        "#c49c94",
        "#f7b6d2",
        "#c7c7c7",
        "#dbdb8d",
        "#9edae5"
    ];
    this.colorScale = d3.scale.ordinal().range(this.colorRange);
    this.responsive = true;

    if (opts.responsive === false) {
        this.responsive = false;
    }

    if (this.responsive) {
        this.redrawOnResize();
        this.width = $(window).width();
        this.height = $(window).height() - 40;
    }
    else {
        this.width = opts.width || coreMap.Map.DEFAULT_WIDTH;
        this.height = opts.height || coreMap.Map.DEFAULT_HEIGHT;
    }

    this.initializeMap();
    this.setupLayers();
    this.resetZoom();
    this.setData(opts.data || []);
    this.heatmapLayer.toggle();
};

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
coreMap.Map.BOX_COLOR = "#39b54a";
coreMap.Map.BOX_WIDTH = 2;
coreMap.Map.BOX_OPACITY = 1;

coreMap.Map.SOURCE_PROJECTION = new OpenLayers.Projection("EPSG:4326");
coreMap.Map.DESTINATION_PROJECTION = new OpenLayers.Projection("EPSG:900913");

/**
 * Simple close handler to be called if a popup is closed.
 * @param Object evet A close event.
 * @private
 * @method onPopupClose
 */

var onPopupClose = function (evt) {
    this.map.selectControl.unselect(this.feature);
}

/**
 * Feature select handler used to display popups on point layers.
 * @param Object feature An Open Layers feature object.  Should be an object with geometry.
 * @private
 * @method onFeatureSelect
 */

var onFeatureSelect = function(feature) {
    var text = '<div><table class="table table-striped table-condensed">';
    for (key in feature.attributes) {
        text += '<tr><th>' + _.escape(key) + '</th><td>' + _.escape(feature.attributes[key]) + '</td>';
    }
    text += '</table></div>';

    var popup = new OpenLayers.Popup.FramedCloud("Data",
        feature.geometry.getBounds().getCenterLonLat(),
        null,
        text,
        null,
        false,
        onPopupClose);

    feature.popup = popup;
    this.map.addPopup(popup);
}

/**
 * Feature unselect handler used to remove popups from point layers.
 * @param Object feature An Open Layers feature object.  Should be an object with geometry.
 * @private
 * @method onFeatureUnelect
 */

var onFeatureUnselect = function(feature) {
    if (feature.popup) {
        this.map.removePopup(feature.popup);
        feature.popup.destroy();
        feature.popup = null;
    }
}

/**
 * Draws the map data
 * @method draw
 */

coreMap.Map.prototype.draw = function () {
    var me = this;

    var heatmapData = [];
    var mapData = [];
    me.colors = {};
    _.each(this.data, function (element) {
        var longitude = me.getValueFromDataElement(me.longitudeMapping, element);
        var latitude = me.getValueFromDataElement(me.latitudeMapping, element);

        if ($.isNumeric(latitude) && $.isNumeric(longitude)) {
            heatmapData.push(me.createHeatmapDataPoint(element, longitude, latitude));
            mapData.push(me.createPointsLayerDataPoint(element, longitude, latitude));
        }
    });

    // Remove any popups before resetting data so they do not become orphaned.
    me.map.selectControl.unselectAll();
    me.heatmapLayer.setDataSet({ max: 1, data: heatmapData});
    me.pointsLayer.removeAllFeatures();
    me.pointsLayer.addFeatures(mapData);
};

/**
 * Resets the map. This clears all the data, zooms all the way out and centers the map.
 * @method reset
 */

coreMap.Map.prototype.reset = function () {
    this.map.selectControl.unSelectAll();
    this.setData([]);
    this.draw();
    this.resetZoom();
};

/**
 * Resets the map to zoom level 1 centered on latitude/longitude 0.0/0.0.
 * @method resetZoom
 */

coreMap.Map.prototype.resetZoom = function () {
    this.map.zoomToMaxExtent();
    this.map.setCenter(new OpenLayers.LonLat(0, 0), 1);
};

/**
 * Sets the map's data.
 * @param mapData the data to be set. This should be an array of points. The points may be specified
 * in any way, This component uses the mapping objects to map each array element to latitude, longitude, size and color.
 * @param {Array} An array of data objects to plot
 * @method setData
 */

coreMap.Map.prototype.setData = function (mapData) {
    this.data = mapData;
    this.updateRadii();
};

/**
 * Updates the internal min/max radii values for the point layer.  These values are simply
 * the minimum and maximum values of the sizeMapping in the current data set.  They will be
 * mapped linearly to the range of allowed sizes between coreMap.Map.MIN_RADIUS and
 * coreMap.Map.MAX_RADIUS.  This function should be called after new data is set to ensure
 * correct display.
 * @method updateRadii
 */

coreMap.Map.prototype.updateRadii = function () {
    this.minRadius = this.calculateMinRadius();
    this.maxRadius = this.calculateMaxRadius();
    this._baseRadiusDiff = coreMap.Map.MAX_RADIUS - coreMap.Map.MIN_RADIUS;
    this._dataRadiusDiff = this.maxRadius - this.minRadius;
};


coreMap.Map.prototype.getColorMappings = function () {
    var me = this;

    // convert to an array that is in alphabetical order for consistent iteration order
    var sortedColors = [];
    for (key in this.colors) {
        var color = me.colors[key];
        sortedColors.push({ 'color': color, 'category': key});
    }

    return sortedColors;
};

/**
 * Resets all assigned color mappings.
 * @method resetColorMappings
 */

coreMap.Map.prototype.resetColorMappings = function () {
    this.colorScale = d3.scale.ordinal().range(this.colorRange);
};

/**
 * Toggles visibility between the points layer and heatmap layer.
 * @method toggleLayers
 */

coreMap.Map.prototype.toggleLayers = function () {
    if (this.currentLayer === this.pointsLayer) {
        this.pointsLayer.setVisibility(false);
        this.heatmapLayer.toggle();
        this.currentLayer = this.heatmapLayer;
    }
    else {
        this.heatmapLayer.toggle();
        this.pointsLayer.setVisibility(true);
        this.currentLayer = this.pointsLayer;
    }
};


/**
 * Registers a listener for a particular map event.
 * @param {String} type A map event type.
 * @param {Object} obj An object that the listener should be registered on.
 * @param {Function} listener A function to be called when the event occurs.
 * @method register
 */

coreMap.Map.prototype.register = function (type, obj, listener) {
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

coreMap.Map.prototype.createHeatmapDataPoint = function (element, longitude, latitude) {
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

coreMap.Map.prototype.createPointsLayerDataPoint = function (element, longitude, latitude) {
    var point = new OpenLayers.Geometry.Point(longitude, latitude);
    point.data = element;
    point.transform(coreMap.Map.SOURCE_PROJECTION, coreMap.Map.DESTINATION_PROJECTION);
    var feature = new OpenLayers.Feature.Vector(point);
    feature.style = this.stylePoint(element);
    feature.attributes = element;
    return feature;
};

/**
 * Styles the data element based on the size and color.
 * @param {Object} element One data element of the map's data array.
 * @return {OpenLayers.Symbolizer.Point} The style object
 * @method stylePoint
 */

coreMap.Map.prototype.stylePoint = function (element) {
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

coreMap.Map.prototype.createPointStyleObject = function (color, radius) {
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
 * Calculate the desired radius of a point.  This will be a proporation of the
 * allowed coreMap.Map.MIN_RADIUS and coreMap.Map.MAX_RADIUS values.
 * @param {Object} element One data element of the map's data array.
 * @return {number} The radius
 * @method calculateRadius
 */
coreMap.Map.prototype.calculateRadius = function (element) {
    var dataVal = this.getValueFromDataElement(this.sizeMapping, element);
    var percentOfDataRange = (dataVal - this.minRadius) / this._dataRadiusDiff;
    return coreMap.Map.MIN_RADIUS + (percentOfDataRange * this._baseRadiusDiff);
};

/**
 * Calculate the desired color of a point.
 * @param {Object} element One data element of the map's data array.
 * @return {String} The color
 * @method calculateColor
 */

coreMap.Map.prototype.calculateColor = function (element) {
    var category = this.getValueFromDataElement(this.categoryMapping, element);
    var color;

    if (category) {
        color = this.colorScale(category);
    }
    else {
        category = '(Uncategorized)';
        color = coreMap.Map.DEFAULT_COLOR;
    }

    // store the color in the registry so we know the color/category mappings
    if (!(this.colors.hasOwnProperty(category))) {
        this.colors[category] = color;
    }

    return color
};

/**
 * Calculate the radius of the smallest element in the data
 * @return {number} The minimum value in the data
 * @method calculateMinRadius
 */

coreMap.Map.prototype.calculateMinRadius = function () {
    var me = this;
    return d3.min(me.data, function (el) {
        return me.getValueFromDataElement(me.sizeMapping, el);
    });
};

/**
 * Calculate the radius of the largest element in the data
 * @return {number} The maximum value in the data
 * @method calculateMaxRadius
 */

coreMap.Map.prototype.calculateMaxRadius = function () {
    var me = this;
    return d3.max(me.data, function (el) {
        return me.getValueFromDataElement(me.sizeMapping, el);
    });
};

/**
 * Gets a value from a data element using a mapping string or function.
 * @param {String | Function} mapping The mapping from data element object to value.
 * @param {Object} element An element of the data array.
 * @return The value in the data element.
 * @method getValueFromDataElement
 */

coreMap.Map.prototype.getValueFromDataElement = function (mapping, element) {
    if (typeof mapping === 'function') {
        return mapping.call(this, element);
    }
    return element[mapping];
};

coreMap.Map.prototype.toggleCaching = function () {
    this.caching = !this.caching;
    if (this.caching) {
        this.cacheReader.deactivate();
        this.cacheWriter.activate();
    }
    else {
        this.cacheReader.activate();
        this.cacheWriter.deactivate();
    }
}

// clear the LocaleStorage used by the browser to store data for this.
coreMap.Map.prototype.clearCache = function () {
    OpenLayers.Control.CacheWrite.clearCache();
    console.log("Cleared the map cache.");
}

/**
 * Initializes the map.
 * @method initializeMap
 */

coreMap.Map.prototype.initializeMap = function () {
    OpenLayers.ProxyHost = "proxy.cgi?url=";
    $('#' + this.elementId).css({
        width: this.width,
        height: this.height
    });
    this.map = new OpenLayers.Map(this.elementId);
    this.configureFilterOnZoomRectangle();
};

coreMap.Map.prototype.configureFilterOnZoomRectangle = function () {
    var me = this;
    var control = new OpenLayers.Control();
    // this is copied from the OpenLayers.Control.ZoomBox, but that doesn't provide a way to hook in, so we had to copy
    // it here to provide a callback after zooming
    OpenLayers.Util.extend(control, {
        draw: function () {
            // this Key Handler is works in conjunctions with the Box handler below.  It detects when the user
            // has depressed the shift key and tells the map to update its sizing.  This is a work around for 
            // zoomboxes being drawn in incorrect locations.  If any dom element higher in the page than a
            // map changes height to reposition the map, the next time a user tries to draw a rectangle, it does
            // not appear under the mouse cursor.  Rather, it is incorrectly drawn in proportion to the
            // height change in other dom elements.  This forces a the map to recalculate its size on the key event
            // that occurs just prior to the zoombox being drawn.  This may also trigger on other random shift-clicks
            // but does not appears performant enough in a map that displays a few hundred thousand points.
            this.keyHandler = new OpenLayers.Handler.Keyboard(control,
               {
                    "keydown": function(event) {
                        if (event.keyCode === 16 && !this.waitingForShiftUp) {
                            this.map.updateSize();
                            this.waitingForShiftUp = true;
                        }
                    },
                    "keyup": function(event) {
                        if (event.keyCode === 16 && this.waitingForShiftUp) {
                            this.waitingForShiftUp = false;
                        }
                    }
               });
            this.keyHandler.activate();

            // this Handler.Box will intercept the shift-mousedown
            // before Control.MouseDefault gets to see it
            this.box = new OpenLayers.Handler.Box(control,
                { "done": this.notice },
                {keyMask: OpenLayers.Handler.MOD_SHIFT});
            this.box.activate();
        },

        notice: function (position) {
            if (position instanceof OpenLayers.Bounds) {
                var bounds,
                    targetCenterPx = position.getCenterPixel();
                if (!this.out) {
                    var minXY = this.map.getLonLatFromPixel({
                        x: position.left,
                        y: position.bottom
                    });
                    var maxXY = this.map.getLonLatFromPixel({
                        x: position.right,
                        y: position.top
                    });
                    bounds = new OpenLayers.Bounds(minXY.lon, minXY.lat,
                        maxXY.lon, maxXY.lat);
                } else {
                    var pixWidth = position.right - position.left;
                    var pixHeight = position.bottom - position.top;
                    var zoomFactor = Math.min((this.map.size.h / pixHeight),
                        (this.map.size.w / pixWidth));
                    var extent = this.map.getExtent();
                    var center = this.map.getLonLatFromPixel(targetCenterPx);
                    var xmin = center.lon - (extent.getWidth() / 2) * zoomFactor;
                    var xmax = center.lon + (extent.getWidth() / 2) * zoomFactor;
                    var ymin = center.lat - (extent.getHeight() / 2) * zoomFactor;
                    var ymax = center.lat + (extent.getHeight() / 2) * zoomFactor;
                    bounds = new OpenLayers.Bounds(xmin, ymin, xmax, ymax);
                }
                // always zoom in/out
                var lastZoom = this.map.getZoom(),
                    size = this.map.getSize(),
                    centerPx = {x: size.w / 2, y: size.h / 2},
                    zoom = this.map.getZoomForExtent(bounds),
                    oldRes = this.map.getResolution(),
                    newRes = this.map.getResolutionForZoom(zoom);
                if (oldRes == newRes) {
                    this.map.setCenter(this.map.getLonLatFromPixel(targetCenterPx));
                } else {
                    var zoomOriginPx = {
                        x: (oldRes * targetCenterPx.x - newRes * centerPx.x) /
                            (oldRes - newRes),
                        y: (oldRes * targetCenterPx.y - newRes * centerPx.y) /
                            (oldRes - newRes)
                    };
                    this.map.zoomTo(zoom, zoomOriginPx);
                }
                if (lastZoom == this.map.getZoom() && this.alwaysZoom == true) {
                    this.map.zoomTo(lastZoom + (this.out ? -1 : 1));
                }
                if (me.onZoomRect) {
                    // switch destination and source here since we're projecting back into lat/lon
                    me.onZoomRect.call(me, bounds.transform(coreMap.Map.DESTINATION_PROJECTION, coreMap.Map.SOURCE_PROJECTION));
                }
            }
        }
    });
    this.map.addControl(control);
}

/**
 * Initializes the map layers and adds the base layer.
 * @method setupLayers
 */

coreMap.Map.prototype.setupLayers = function () {
    var baseLayer = new OpenLayers.Layer.OSM("OSM", null, {wrapDateLine: false});
    this.map.addLayer(baseLayer);

    var style = {
        styleMap: new OpenLayers.StyleMap(OpenLayers.Util.applyDefaults(
            {fillColor: "#00FF00", fillOpacity: 0.8, strokeOpacity: 0.8, strokeWidth: 1, pointRadius: 4},
            OpenLayers.Feature.Vector.style["default"]
        ))
    };
    // lets clients draw boxes on the map
    this.boxLayer = new OpenLayers.Layer.Boxes();
    this.pointsLayer = new OpenLayers.Layer.Vector("Points Layer", style);

    var heatmapOptions = {visible: true, radius: 10};
    var options = {isBaseLayer: false, opacity: 0.3, projection: coreMap.Map.SOURCE_PROJECTION};
    this.heatmapLayer = new OpenLayers.Layer.Heatmap("Heatmap Layer", this.map, baseLayer, heatmapOptions, options);

    this.map.addLayer(this.heatmapLayer);
    this.map.addLayer(this.pointsLayer);
    this.map.addLayer(this.boxLayer);

    // Add popup handlers to the points layer.
    // this.pointsLayer.events.on({
    //     'featureselected': onFeatureSelect,
    //     'featureunselected': onFeatureUnselect
    // });
    this.map.selectControl = new OpenLayers.Control.SelectFeature(this.pointsLayer, {
        'onSelect': onFeatureSelect,
        'onUnselect': onFeatureUnselect
    });
    this.map.addControl(this.map.selectControl);
    this.map.selectControl.activate();

    // Default the heatmap to be visible.
    this.heatmapLayer.toggle();
    this.pointsLayer.setVisibility(false);
    this.currentLayer = this.heatmapLayer;

    // Create a cache reader and writer.  Use default reader
    // settings to read from cache first.
    this.cacheReader = new OpenLayers.Control.CacheRead();

    this.cacheWriter = new OpenLayers.Control.CacheWrite({
        imageFormat: "image/png",
        eventListeners: {
            cachefull: function () {
                console.log("Map cache is full.  Will not cache again until the cache is cleared.");
                alert("Cache Full.  Re-enable caching to clear the cache and start building a new set");
                this.toggleCaching();
            }
        }
    });

    //this.cacheWriter.addLayer(baseLayer);
    this.map.addControl(this.cacheReader);
    this.map.addControl(this.cacheWriter);
};

/**
 * Draws a box with the specified bounds
 * @param {Object} bounds An object with 4 parameters, left, bottom, right and top
 * @return {Object} The object representing the box so it can be removed
 */
coreMap.Map.prototype.drawBox = function (bounds) {
    var box = new OpenLayers.Marker.Box(
        new OpenLayers.Bounds(bounds.left, bounds.bottom, bounds.right, bounds.top).transform(coreMap.Map.SOURCE_PROJECTION,coreMap.Map.DESTINATION_PROJECTION),
        coreMap.Map.BOX_COLOR, coreMap.Map.BOX_WIDTH);
    box.div.style.opacity = coreMap.Map.BOX_OPACITY;
    this.boxLayer.addMarker(box);
    return box;
};

/**
 * Removes the box that was added with drawBox
 * @param box
 */
coreMap.Map.prototype.removeBox = function (box) {
    this.boxLayer.removeMarker(box);
};

/**
 * Zooms to the specified bounding rectangle
 * @param {Object} bounds An object with 4 parameters, left, bottom, right and top
 */
coreMap.Map.prototype.zoomToBounds = function(bounds) {
    this.map.zoomToExtent(new OpenLayers.Bounds(bounds.left, bounds.bottom, bounds.right, bounds.top).transform(coreMap.Map.SOURCE_PROJECTION,coreMap.Map.DESTINATION_PROJECTION));
};

/**
 * Add a resize listener on the window to redraw the map
 * @method redrawOnResize
 */

coreMap.Map.prototype.redrawOnResize = function () {

    var me = this;
    $(window).resize(function () {
        me.width = $(window).width();
        me.height = $(window).height() - 40;

        $('#' + me.elementId).css({
            width: me.width,
            height: me.height
        });
        me.map.updateSize();
    });

};