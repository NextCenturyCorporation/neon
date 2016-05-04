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
    var executeAndWait = function(name, asyncFunction, args, test) {
        // the target is null since there is no "this" context for these functions (they are "static")
        return neontest.executeAndWait(name, null, asyncFunction, args, test);
    };

    describe('save and restore states', function() {
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

        executeAndWait('saved state 1', neon.widget.saveState, [instanceId1, state1], function(result) {
            expect(true).toBe(true);
        });

        executeAndWait('saved state 2', neon.widget.saveState, [instanceId2, state2], function(result) {
            expect(true).toBe(true);
        });

        executeAndWait('restored state 1', neon.widget.getSavedState, instanceId1, function(result) {
            expect(result).toEqual(state1);
        });

        executeAndWait('restored state 2', neon.widget.getSavedState, instanceId2, function(result) {
            expect(result).toEqual(state2);
        });
    });

    describe('get an empty state if none exists', function() {
        executeAndWait('returned an empty state', neon.widget.getSavedState, 'invalidWidgetId', function(result) {
            expect(result).toEqual({});
        });
    });

    describe('gets a unique instance id', function() {
        // instanceId1a and 1b should be the same
        var instanceId1a;
        var instanceId1b;
        var instanceId2;
        var globalInstanceId1a;
        var globalInstanceId1b;

        executeAndWait('fetched a UUID with one qualifier', neon.widget.getInstanceId, 'qualifier1', function(result) {
            instanceId1a = result;
            expect(instanceId1a.length).toBeGreaterThan(0);
        });

        executeAndWait('fetched a UUID with the same qualifier', neon.widget.getInstanceId, 'qualifier1', function(result) {
            instanceId1b = result;
            expect(instanceId1b.length).toBeGreaterThan(0);
        });

        executeAndWait('fetched a UUID for a different qualifier', neon.widget.getInstanceId, 'qualifier2', function(result) {
            instanceId2 = result;
            expect(instanceId2.length).toBeGreaterThan(0);
        });

        executeAndWait('fetched a global UUID', neon.widget.getInstanceId, [], function(result) {
            globalInstanceId1a = result;
            expect(globalInstanceId1a.length).toBeGreaterThan(0);
        });

        executeAndWait('fetched a second global UUID', neon.widget.getInstanceId, [], function(result) {
            globalInstanceId1b = result;
            expect(globalInstanceId1b.length).toBeGreaterThan(0);
        });

        it('returned the same UUIDs given the same qualifier', function() {
            expect(instanceId1a).toEqual(instanceId1b);
        });

        it('returned different UUIDs given different qualifiers', function() {
            expect(instanceId1a).not.toEqual(instanceId2);
        });

        it('returned a global UUID repeatedly and that ID was not one of the qualified UUIDs', function() {
            expect(instanceId1a).not.toEqual(globalInstanceId1a);
            expect(instanceId2).not.toEqual(globalInstanceId1a);
            expect(globalInstanceId1a).toEqual(globalInstanceId1b);
        });
    });
});
