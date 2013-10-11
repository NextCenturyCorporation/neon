$(function(){
    var map = new coreMap.Map("map");

    var data = [
        {latitude: 50, longitude: 0, count_: 5, category: "blue"},
        {latitude: 37, longitude: -117, count_: 10, category: "blue"},
        {latitude: 50, longitude: 40, count_: 5, category: "green"},
        {latitude: 37, longitude: -147, count_: 10, category: "green"},
        {latitude: 50, longitude: 30, count_: 5, category: "yellow"},
        {latitude: 37, longitude: -137, count_: 10, category: "yellow"},
        {latitude: 50, longitude: 20, count_: 5, category: "white"},
        {latitude: 37, longitude: -127, count_: 10, category: "white"},
        {latitude: 40, longitude: -96, count_: 15, category: "red"}
    ];

    map.setData(data);
    map.setColorMapping(function(element){
        return element.latitude;
    });
    map.setSizeMapping(function(element){
        return element.latitude;
    });
    map.draw();


});