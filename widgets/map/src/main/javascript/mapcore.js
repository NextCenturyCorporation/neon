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

    this.setupMapAndLayers();
};

coreMap.Map.DEFAULT_WIDTH = 800;
coreMap.Map.DEFAULT_HEIGHT = 600;
coreMap.Map.DEFAULT_LATITUDE_MAPPING = "latitude";
coreMap.Map.DEFAULT_LONGITUDE_MAPPING = "longitude";

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
    });

    me.pointsLayer.removeAllFeatures();
    me.pointsLayer.addFeatures(mapData);
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

coreMap.Map.prototype.setupMapAndLayers = function(){
    this.map = new OpenLayers.Map();
    var baseLayer = new OpenLayers.Layer.OSM();
    this.map.addLayer(baseLayer);

    var style = {
        styleMap: new OpenLayers.StyleMap(OpenLayers.Util.applyDefaults(
            {fillColor: "#00FF00", fillOpacity: 0.8, strokeOpacity: 0.8, strokeWidth: 1, pointRadius: 4},
            OpenLayers.Feature.Vector.style["default"]
        ))
    };
    this.pointsLayer = new OpenLayers.Layer.Vector("Points Layer", style);

    var hmOptions = {visible: true, radius: 10};
    var options = {isBaseLayer: false, opacity: 0.3, projection: new OpenLayers.Projection("EPSG:4326")};
    this.heatmapLayer = new OpenLayers.Layer.Heatmap("Heatmap Layer", this.map, this.layer, hmOptions, options);

    this.currentLayer = this.pointsLayer;
    this.map.addLayer(this.currentLayer);
};
