$(function(){
    var map = new coreMap.Map("map");

    var data = [
        {latitude: 50, longitude: 0},
        {latitude: 37, longitude: -117},
        {latitude: 40, longitude: -96}
    ];

    map.setData(data);
    map.draw();

});