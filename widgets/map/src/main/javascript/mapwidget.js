$(function(){
    var map = new coreMap.Map("map");

    var data = [
        {latitude: 50, longitude: 0, count_: 5},
        {latitude: 37, longitude: -117, count_: 10},
        {latitude: 40, longitude: -96, count_: 15}
    ];

    map.setData(data);
    map.draw();


});