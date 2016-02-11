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
/*global neontest*/

describe('widgets', function() {
    // helper to execute async functions
    var executeAndWait = function(asyncFunction, args) {
        // the target is null since there is no "this" context for these functions (they are "static")
        return neontest.executeAndWait(null, asyncFunction, args);
    };

    it('save and restore states', function() {
        // simulate state from two different widgets with different ids
        var instanceId1 = "id1";
        var state1 = {
            s1: "val1"
        };
        var restoredState1;
        var instanceId2 = "id2";
        var state2 = {
            s2: "val2"
        };
        var restoredState2;

        executeAndWait(neon.widget.saveState, [instanceId1, state1]);
        runs(function() {
            executeAndWait(neon.widget.saveState, [instanceId2, state2]);
            runs(function() {
                restoredState1 = executeAndWait(neon.widget.getSavedState, instanceId1);
                runs(function() {
                    restoredState2 = executeAndWait(neon.widget.getSavedState, instanceId2);
                    runs(function() {
                        expect(restoredState1.get()).toEqual(state1);
                        expect(restoredState2.get()).toEqual(state2);
                    });
                });
            });
        });
    });

    it('get an empty state if none exists', function() {
        var empty = executeAndWait(neon.widget.getSavedState, 'invalidWidgetId');
        runs(function() {
            expect(empty.get()).toEqual({});
        });
    });

    it('gets a unique instance id', function() {
        // instanceId1a and 1b should be the same
        var instanceId1a = executeAndWait(neon.widget.getInstanceId, 'qualifier1');
        runs(function() {
            var instanceId1b = executeAndWait(neon.widget.getInstanceId, 'qualifier1');
            runs(function() {
                var instanceId2 = executeAndWait(neon.widget.getInstanceId, 'qualifier2');
                runs(function() {
                    var globalInstanceId1a = executeAndWait(neon.widget.getInstanceId);
                    runs(function() {
                        var globalInstanceId1b = executeAndWait(neon.widget.getInstanceId);
                        runs(function() {
                            expect(instanceId1a.get()).toEqual(instanceId1b.get());
                            expect(instanceId1a.get()).not.toEqual(instanceId2.get());
                            expect(instanceId1a.get()).not.toEqual(globalInstanceId1a.get());
                            expect(instanceId2.get()).not.toEqual(globalInstanceId1a.get());
                            expect(globalInstanceId1a.get()).toEqual(globalInstanceId1b.get());
                        });
                    });
                });
            });
        });
    });
});
