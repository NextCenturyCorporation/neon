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



var neontest = neontest || {};
neontest.matchers = {};
neontest.matchers.matcher = new jasmine.Matchers();

/**
 * Executes the asynchronous function synchronously and returns the result of the call
 * @param {Object} The target object to execute the function on
 * @param {Function} asyncFunction The function to execute
 * @param {Array} args The arguments to pass to the function. This may be either a single argument or an array of arguments
 * @param {Object} A future whose "get" method will return the value of the async function when it is completed.
 * Typically this will be used inside of a jasmine "runs" block.
  */
neontest.executeAndWait = function(target, asyncFunction, args) {
    var done = false;
    var argsArray = [];
    var result;
    if ( args ) {
        argsArray = argsArray.concat(args);
    }

    // push the success callback to store the current result
    argsArray.push(function (res) {
        result = res;
        done = true;
    });
    asyncFunction.apply(target, argsArray);
    waitsFor(function () {
        return done;
    });
    return {
        get: function() { return result; }
    };

};

/**
 * Checks if the expected array is equal to the actual array. This comparison
 * uses a deeper equality check (checks actual keys and values).
 * @param expectedArray
 * @returns {boolean}
 */
neontest.matchers.toBeEqualArray = function (expectedArray) {

    var err = '';

    this.message = function () {
        return err;
    };


    if (!(this.actual instanceof Array)) {
        err = 'Actual value not an array, received a ' + this.actual.constructor.name + ' instead';
        return false;
    }

    if (!(expectedArray instanceof Array)) {
        err = 'Test configuration error. Expected value not an array, received a ' + expectedArray.constructor.name + ' instead';
        return false;
    }

    if (expectedArray.length !== this.actual.length) {
        err = 'Expected array of length ' + expectedArray.length + ', but was ' + this.actual.length;
        return false;
    }

    var matchError = false;
    this.actual.forEach(function (element, index, actual) {
        if (!lodash.isEqual(actual[index], expectedArray[index])) {
            if (err) {
                err += '\r\n';
            }
            err += 'Element ' + index + ' does not match, expected ' + JSON.stringify(expectedArray[index]) + ', but was ' + JSON.stringify(actual[index]);
            matchError = true;
        }
    });

    if (matchError) {
        return false;
    }


    return true;
};

neontest.matchers.toBeInstanceOf = function (expectedType) {

    var actual = this.actual;
    this.message = function () {
        // constructor.name is not defined in all browsers
        var typeName = (Function.prototype.name !== undefined) ? actual.constructor.name : actual.constructor;
        return 'expected ' + actual + ' to be of type ' + expectedType + ', but was ' + typeName;
    };

    return actual instanceof expectedType;
};

/**
 *
 * Checks if the expected array is the same as the actual, independent of order. This does not
 * compare individual keys/values of objects, it just uses equality checks
 * @param expectedArray
 * @methods
 * @returns {boolean}
 */
neontest.matchers.toBeArrayWithSameElements = function (expectedArray) {

    var actual = this.actual;
    this.message = function () {
        return 'expected: ' + expectedArray + ', actual: ' + actual;
    };

    return expectedArray.length === actual.length && lodash.difference(expectedArray, actual).length === 0;
};

jasmine.Spy.prototype.wasInvoked = function () {
    return this.callCount > 0;
};

// this adds the matchers globally for all tests
beforeEach(function () {

    var matchers = {
        toBeEqualArray: neontest.matchers.toBeEqualArray,
        toBeInstanceOf: neontest.matchers.toBeInstanceOf,
        toBeArrayWithSameElements: neontest.matchers.toBeArrayWithSameElements
    };

    this.addMatchers(matchers);
});
