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

describe('OWF utils', function() {
    // save off the OWF namespace since some tests will modify it
    var OWFNamespace = OWF;

    afterEach(function() {
        OWF = OWFNamespace;
    });

    it('returns false when OWF is not defined', function() {
        OWF = undefined;
        expect(neon.util.owfUtils.isRunningInOWF()).toBeFalsy();
    });

    it('returns false when OWF says it is not running in OWF', function() {
        OWF = {
            Util: {
                isRunningInOWF: function() {
                    return false;
                }
            }
        };
        // sanity check that we properly defined OWF.Util
        expect(OWF.Util).toBeDefined();

        expect(neon.util.owfUtils.isRunningInOWF()).toBeFalsy();
    });

    it('return true when running OWF', function() {
        OWF = {
            Util: {
                isRunningInOWF: function() {
                    return true;
                }
            }
        };
        expect(neon.util.owfUtils.isRunningInOWF()).toBeTruthy();
    });
});