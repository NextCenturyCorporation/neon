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



describe('map', function () {
    var mapId = "map";

    beforeEach(function () {
        setFixtures("<div id='" + mapId +"'></div>");
    });

    it('has a default layer and can toggle layers', function () {
        var map = new coreMap.Map(mapId);
        expect(map.currentLayer.name).toEqual("Points Layer");
        expect(map.heatmapLayer.heatmap.get("visible")).toEqual(false);
        map.toggleLayers();
        expect(map.currentLayer.name).toEqual("Heatmap Layer");
        expect(map.pointsLayer.getVisibility()).toEqual(false);
        expect(map.heatmapLayer.heatmap.get("visible")).toEqual(true);
        map.toggleLayers();
        expect(map.currentLayer.name).toEqual("Points Layer");
        expect(map.heatmapLayer.heatmap.get("visible")).toEqual(false);

    });

    it('has default data mapping of latitude and longitude', function () {
        var data = [{latitude: 50, longitude: 20}, {longitude: 40, latitude: -30}];
        var map = new coreMap.Map(mapId);
        map.setData(data);

        expect(map.getValueFromDataElement(map.latitudeMapping, data[0])).toEqual(50);
        expect(map.getValueFromDataElement(map.longitudeMapping, data[0])).toEqual(20);
        expect(map.getValueFromDataElement(map.latitudeMapping, data[1])).toEqual(-30);
        expect(map.getValueFromDataElement(map.longitudeMapping, data[1])).toEqual(40);
    });

    it('default latitude and longitude mapping can be overridden', function () {
        var data = [[50,20], [-30, 40]];

        var opts = {
            data: data,
            latitudeMapping: function(element){
                return element[0];
            },
            longitudeMapping: function(element){
                return element[1];
            }
        };

        var map = new coreMap.Map(mapId, opts);

        expect(map.getValueFromDataElement(map.latitudeMapping, data[0])).toEqual(50);
        expect(map.getValueFromDataElement(map.longitudeMapping, data[0])).toEqual(20);
        expect(map.getValueFromDataElement(map.latitudeMapping, data[1])).toEqual(-30);
        expect(map.getValueFromDataElement(map.longitudeMapping, data[1])).toEqual(40);
    });

    it('has default width and height', function () {
        var opts = {
            responsive: false
        }
        var map = new coreMap.Map(mapId, opts);

        expect(map.width).toEqual(1024);
        expect(map.height).toEqual(680);
    });

    it('default width and height can be overridden', function () {
        var opts = {
            responsive: false,
            width: 200,
            height: 100
        }
        var map = new coreMap.Map(mapId, opts);

        expect(map.width).toEqual(200);
        expect(map.height).toEqual(100);
    });

    it('has default radius of 3', function () {
        var data = [{latitude: 50, longitude: 20}, {longitude: 40, latitude: -30}];
        var map = new coreMap.Map(mapId);
        map.setData(data);
        map.draw();

        expect(map.currentLayer.features[0].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[1].style.pointRadius).toEqual(3);

    });

    it('has radius depending on count', function () {
        var data = [{latitude: 50, longitude: 20, count_: 10},
            {longitude: 40, latitude: -30, count_: 1},
            {longitude: 40, latitude: -30, count_: 0},
            {longitude: 40, latitude: -30, count_: -5},
            {longitude: 40, latitude: -30, count_: null}
        ];
        var map = new coreMap.Map(mapId);
        map.setData(data);
        map.draw();

        expect(map.currentLayer.features[0].style.pointRadius).toEqual(12);
        expect(map.currentLayer.features[1].style.pointRadius).toEqual(6);
        expect(map.currentLayer.features[2].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[3].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[4].style.pointRadius).toEqual(3);
    });

    it('has radius ranging between 3 and 13', function () {
        var data = [{latitude: 50, longitude: 20, count_: -5000},
            {longitude: 10, latitude: -30, count_: 5},
            {longitude: 20, latitude: -30, count_: 50 * 1000},
            {longitude: 50, latitude: -30, count_: 5 * 1000 * 1000}
        ];
        var map = new coreMap.Map(mapId);
        map.setData(data);
        map.draw();

        expect(map.currentLayer.features[0].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[1].style.pointRadius).toEqual(5);
        expect(map.currentLayer.features[2].style.pointRadius).toEqual(5);
        expect(map.currentLayer.features[3].style.pointRadius).toEqual(15);
    });

    it('gets correct minimum and maximum', function () {
        var data = [{longitude: 20, latitude: 45},
            {longitude: 10, latitude: -30},
            {longitude: 20, latitude: 0},
            {longitude: 50, latitude: null}
        ];
        var map = new coreMap.Map(mapId);

        expect(map.minValue(data, 'latitude')).toEqual(-30);
        expect(map.maxValue(data, 'latitude')).toEqual(45);
    });

    it('gets correct minimum and maximum when using function', function () {
        var data = [[50,20], [-30, 40]];

        var latMap = function(element){
            return element[0];
        };

        var map = new coreMap.Map(mapId);

        expect(map.minValue(data, latMap)).toEqual(-30);
        expect(map.maxValue(data, latMap)).toEqual(50);
    });
});
