//TODO: Documentation
//TODO: The numeric color by scale for numeric color column mapping.

var coreMap = coreMap || {};

coreMap.Map = function(elementId, opts){
    opts = opts || {};

    this.elementId = elementId;
    this.selectorText = "#" + elementId;
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

coreMap.Map.prototype.reset = function(){
    this.setData([]);
    this.draw();
    this.map.setCenter(new OpenLayers.LonLat(0, 0));
    this.map.zoomToMaxExtent();

};

coreMap.Map.prototype.setData = function(mapData){
    this.data = mapData;
};

coreMap.Map.prototype.setLatitudeMapping = function(mapping){
    this.latitudeMapping = mapping;
};

coreMap.Map.prototype.setLongitudeMapping = function(mapping){
    this.longitudeMapping = mapping;
};

coreMap.Map.prototype.setSizeMapping = function(mapping){
    this.sizeMapping = mapping;
};

coreMap.Map.prototype.setColorMapping = function(mapping){
    this.colorMapping = mapping;
};

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

coreMap.Map.prototype.createPointsLayerDataPoint = function(element, longitude, latitude){
    var point = new OpenLayers.Geometry.Point(longitude, latitude);
    point.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
    var feature = new OpenLayers.Feature.Vector(point);
    feature.style = this.stylePoint(element);
    return feature;
};

coreMap.Map.prototype.createHeatmapDataPoint = function(element, longitude, latitude){
    var count = this.getValueFromDataElement(coreMap.Map.DEFAULT_SIZE_MAPPING, element);
    var point = new OpenLayers.LonLat(longitude, latitude);

    return {
        lonlat: point,
        count: count
    };
};

coreMap.Map.prototype.stylePoint = function(element){
    var radius = this.calculateRadius(element);
    var color = this.calculateColor(element);

    return this.createPointStyleObject(color, radius);
};

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

coreMap.Map.prototype.calculateColor = function(element){
    var category = this.getValueFromDataElement(this.colorMapping, element);

    if(!category){
        return coreMap.Map.DEFAULT_COLOR;
    }
    return this.colorScale(category);
};


coreMap.Map.prototype.minValue = function(data, mapping) {
    var me = this;
    return d3.min(data, function (el) {
        return me.getValueFromDataElement(mapping, el);
    });
};

coreMap.Map.prototype.maxValue = function(data, mapping) {
    var me = this;
    return d3.max(data, function (el) {
        return me.getValueFromDataElement(mapping, el);
    });
};

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

coreMap.Map.prototype.getValueFromDataElement = function(mapping, element){
    if (typeof mapping === 'function') {
        return mapping.call(this, element);
    }
    return element[mapping];
};

coreMap.Map.prototype.sizeMapContainer = function(){
   $(this.selectorText).css({
       width: this.width,
       height: this.height
   });
};

coreMap.Map.prototype.initializeMap = function(){
    this.map = new OpenLayers.Map();
    //We need to set this size object before initializing the heatmap.
    this.map.size = new OpenLayers.Size(this.width, this.height);
};

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
