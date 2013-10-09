$(function(){
//    var map = new coreMap.Map("map");
//
//    var data = [
//        {latitude: 50, longitude: 0},
//        {latitude: 37, longitude: -117},
//        {latitude: 40, longitude: -96}
//    ];
//
//    map.setData(data);
//    map.draw();

    var data = [[55,0], [45,-112], [37,-86]];

    var opts = {
        data: data,
        latitudeMapping: function(element){
            return element[0];
        },
        longitudeMapping: function(element){
            return element[1];
        }
    };

    var map = new coreMap.Map("map",opts);
    map.draw();

});