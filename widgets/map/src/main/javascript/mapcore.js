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
    this.countMapping = opts.countMapping || coreMap.Map.DEFAULT_COUNT_MAPPING;

    this.map = new OpenLayers.Map();
    this.setupLayers();
};

coreMap.Map.DEFAULT_WIDTH = 800;
coreMap.Map.DEFAULT_HEIGHT = 600;
coreMap.Map.DEFAULT_LATITUDE_MAPPING = "latitude";
coreMap.Map.DEFAULT_LONGITUDE_MAPPING = "longitude";
coreMap.Map.DEFAULT_COUNT_MAPPING = "count_";

coreMap.Map.DEFAULT_OPACITY = 0.8;
coreMap.Map.DEFAULT_STROKE_WIDTH = 0.5;
coreMap.Map.DEFAULT_COLOR = "#00ff00";
coreMap.Map.DEFAULT_STROKE_COLOR = "#ffffff";
coreMap.Map.DEFAULT_RADIUS = 3;



coreMap.Map.prototype.setData = function(mapData){
    this.data = mapData;
};

coreMap.Map.prototype.draw = function(){
    this.map.render(this.elementId);
    this.map.zoomToMaxExtent();
    this.renderData();
};

coreMap.Map.prototype.renderData = function(){
    var me = this;

    var mapData = [];
    _.each(this.data, function (element) {
        var longitude = me.getValueFromDataElement(me.longitudeMapping, element);
        var latitude = me.getValueFromDataElement(me.latitudeMapping, element);

        var point = new OpenLayers.Geometry.Point(longitude, latitude);
        point.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));
        var feature = new OpenLayers.Feature.Vector(point);
        mapData.push(feature);

        feature.style = me.stylePoint(element);

    });

    me.pointsLayer.removeAllFeatures();
    me.pointsLayer.addFeatures(mapData);
};

coreMap.Map.prototype.stylePoint = function(element){
    var radius = this.calculateRadius(element);
    return this.createStyleObject(null, radius);
};

coreMap.Map.prototype.calculateRadius = function(element){
    var count = this.getValueFromDataElement(this.countMapping, element);
    var radius = coreMap.Map.DEFAULT_RADIUS;
    if(count > 1) {
        radius = (2.54 * this.log10(count)) + 3;
    }
    return radius;

};

coreMap.Map.prototype.log10 = function (num) {
    return (Math.log(num)/Math.log(10));
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


coreMap.Map.prototype.createStyleObject = function(color, radius){
    color = color || coreMap.Map.DEFAULT_COLOR;
    radius = radius || coreMap.Map.DEFAULT_RADIUS;

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

coreMap.Map.prototype.toggleLayers = function(){
    if(this.currentLayer === this.pointsLayer){
        this.map.addLayer(this.heatmapLayer);
        this.map.removeLayer(this.pointsLayer);
        this.currentLayer = this.heatmapLayer;
    }
    else{
        this.map.addLayer(this.pointsLayer);
        this.map.removeLayer(this.heatmapLayer);
        this.currentLayer = this.pointsLayer;
    }
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
    this.heatmapLayer = new OpenLayers.Layer.Heatmap("Heatmap Layer", this.map, this.layer, heatmapOptions, options);

    this.currentLayer = this.pointsLayer;
    this.map.addLayer(this.currentLayer);
};
