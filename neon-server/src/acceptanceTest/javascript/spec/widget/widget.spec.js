/*
 * Copyright 2014 Next Century Corporation
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
describe('widgets', function() {

    it('save and restore states', function () {
        // simulate state from two different widgets with different ids
        var instanceId1 = "id1";
        var state1 = {"s1": "val1"};
        var state1Saved = false;
        var state1Restored = false;
        var restoredState1;


        var instanceId2 = "id2";
        var state2 = {"s2": "val2"};
        var restoredState2;
        var state2Saved = false;
        var state2Restored = false;

        var empty = function () {
        };

        neon.widget.saveState(instanceId1, state1, function () {
            state1Saved = true;
        }, empty);
        waitsFor(function () {
            return  state1Saved;
        });
        runs(function () {
            neon.widget.saveState(instanceId2, state2, function () {
                state2Saved = true;
            }, empty);
            waitsFor(function () {
                return state2Saved;
            });
            runs(function () {
                neon.widget.getSavedState(instanceId1, function (state) {
                    restoredState1 = state;
                    state1Restored = true;
                });
                waitsFor(function () {
                    return state1Restored;
                });
                runs(function () {
                    neon.widget.getSavedState(instanceId2, function (state) {
                        restoredState2 = state;
                        state2Restored = true;
                    });
                    waitsFor(function () {
                        return state2Restored;
                    });
                    runs(function () {
                        expect(restoredState1).toEqual(state1);
                        expect(restoredState2).toEqual(state2);
                    });
                });

            });
        });
    });

    it('get an empty state if none exists', function() {
        var empty;
        neon.widget.getSavedState('invalidWidgetId',function(state) {
            empty = state;
        });
        waitsFor(function() {
            return 'undefined' !== typeof empty;
        });
        runs(function() {
           expect(empty).toEqual({});
        });
    });

    it('gets widget initialization data', function() {
        var expected = {"key1":"value1"};
        var actual;
        neon.widget.getWidgetInitializationData('widget1', function(initData) {
            actual = initData;
        });
        waitsFor(function() {
            return 'undefined' !== typeof actual;
        });
        runs(function() {
            expect(actual).toEqual(expected);
        });
    });

    it('get empty initialization data if none exists', function() {
        var empty;
        neon.widget.getWidgetInitializationData('invalidWidget', function(widgetData) {
            empty = widgetData;
        });
        waitsFor(function() {
            return 'undefined' !== typeof empty;
        });
        runs(function() {
            expect(empty).toBe('');
        });
    });


    it('gets widget dataset data', function() {
        var expected =
            [ {"elementId" :"aSelector", "value":"someValue"}];
        var actual;
        neon.widget.getWidgetDatasetMetadata('database1', 'table1', 'widget1', function(widgetData) {
            actual = widgetData;
        });
        waitsFor(function() {
            return 'undefined' !== typeof actual;
        });
        runs(function() {
            expect(actual).toEqual(expected);
        });
    });

    it('gets empty widget dataset data if none exists', function() {
        var empty;
        neon.widget.getWidgetDatasetMetadata('invalidDatabase', 'invalidWidget', 'invalidWidget', function(widgetData) {
            empty = widgetData;
        });
        waitsFor(function() {
            return 'undefined' !== typeof empty;
        });
        runs(function() {
            expect(empty.length).toEqual(0);
        });
    });


    it('gets a unique instance id', function () {
        // instanceId1a and 1b should be the same
        var instanceId1a = neon.widget.getInstanceId('qualifier1');
        var instanceId1b = neon.widget.getInstanceId('qualifier1');
        var instanceId2 = neon.widget.getInstanceId('qualifier2');
        // these are synchronous methods so they will block
        var globalInstanceId1a = neon.widget.getInstanceId();
        var globalInstanceId1b = neon.widget.getInstanceId();

        expect(instanceId1a).toEqual(instanceId1b);
        expect(instanceId1a).not.toEqual(instanceId2);
        expect(instanceId1a).not.toEqual(globalInstanceId1a);
        expect(instanceId2).not.toEqual(globalInstanceId1a);
        expect(globalInstanceId1a).toEqual(globalInstanceId1b);
    });

});