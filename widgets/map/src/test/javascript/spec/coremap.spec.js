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
 *
 * @author tbrooks
 */

describe('map', function () {
    var mapId = "map";

    beforeEach(function () {
        setFixtures("<div id='" + mapId +"'></div>");
    });

    it('has a default layer and can toggle layers', function () {
        var map = new coreMap.Map(mapId);
        expect(map.currentLayer.name).toEqual("Points Layer");
        map.toggleLayers();
        expect(map.currentLayer.name).toEqual("Heatmap Layer");
        map.toggleLayers();
        expect(map.currentLayer.name).toEqual("Points Layer");
    });

    it('has default width and height', function () {
        var map = new coreMap.Map(mapId);

        expect(map.width).toEqual(800);
        expect(map.height).toEqual(600);
    });

    it('default width and height can be overridden', function () {
        var opts = {
            width: 200,
            height: 100
        };

        var map = new coreMap.Map(mapId, opts);

        expect(map.width).toEqual(200);
        expect(map.height).toEqual(100);
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

        expect(map.currentLayer.features[0].style.pointRadius).toEqual(5.54);
        expect(map.currentLayer.features[1].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[2].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[3].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[4].style.pointRadius).toEqual(3);
    });

    it('has radius ranging between 3 and 21', function () {
        var data = [{latitude: 50, longitude: 20, count_: -5000},
            {longitude: 10, latitude: -30, count_: 5},
            {longitude: 20, latitude: -30, count_: 50 * 1000},
            {longitude: 50, latitude: -30, count_: 5 * 1000 * 1000}
        ];
        var map = new coreMap.Map(mapId);
        map.setData(data);
        map.draw();

        expect(map.currentLayer.features[0].style.pointRadius).toEqual(3);
        expect(map.currentLayer.features[1].style.pointRadius).toBeCloseTo(4.78);
        expect(map.currentLayer.features[2].style.pointRadius).toBeCloseTo(14.94);
        expect(map.currentLayer.features[3].style.pointRadius).toBeCloseTo(20.02);
    });

    it('gets correct minimum and maximum', function () {
        var data = [{latitude: 50, longitude: 20},
            {longitude: 10, latitude: -30},
            {longitude: 20, latitude: 0},
            {longitude: 50, latitude: 25}
        ];
        var map = new coreMap.Map(mapId);

        expect(map.minValue(data, 'latitude')).toEqual(-30);
        expect(map.maxValue(data, 'latitude')).toEqual(50);
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
