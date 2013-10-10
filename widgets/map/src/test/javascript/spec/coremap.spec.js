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

});
