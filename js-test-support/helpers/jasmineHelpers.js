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

/**
 * Executes the asynchronous function synchronously and returns the result of the call
 * @param {Object} The target object to execute the function on
 * @param {Function} asyncFunction The function to execute
 * @param {Array} args The arguments to pass to the function. This may be either a single argument or an array of arguments
 * @param {Object} A future whose "get" method will return the value of the async function when it is completed.
 * Typically this will be used inside of a jasmine "runs" block.
  */
neontest.executeAndWait = function(name, target, asyncFunction, args, testFunc) {
    describe('', function() {
        var result;

        beforeEach(function(done){
            var argsArray = [];

            if(args) {
                argsArray = argsArray.concat(args);
            }
            // push the success callback to store the current result
            var successHandler = function(res) {
                result = res;
                done();
            };
            var failHandler = function(res) {
                result = res;
                done();
            };
            argsArray.push(successHandler);
            argsArray.push(failHandler);
            asyncFunction.apply(target, argsArray);
        });
        it(name, function() {
            testFunc(result);
        });
    });
};

/**
 * Checks if the expected array is equal to the actual array. This comparison
 * uses a deeper equality check (checks actual keys and values).
 * @param expectedArray
 * @returns {boolean}
 */
neontest.matchers.toBeEqualArray =  function(util, customEqualityTesters) {
    return {
        compare: function(actual, expectedArray) {
            var err = '';
            var result = {
                pass: false
            };

            if(!(actual instanceof Array)) {
                result.message = 'Actual value not an array, received a ' +
                    ((actual && actual.constructor) ? actual.constructor.name : typeof actual) + ' instead';
                return result;
            }

            if(!(expectedArray instanceof Array)) {
                result.message = 'Test configuration error. Expected value not an array, received a ' + expectedArray.constructor.name + ' instead';
                return result;
            }

            if(expectedArray.length !== actual.length) {
                result.message = 'Expected array of length ' + expectedArray.length + ', but was ' + actual.length;
                return result;
            }

            var matchError = false;
            actual.forEach(function(element, index, actual) {
                if(!lodash.isEqual(actual[index], expectedArray[index])) {
                    if(err) {
                        err += '\r\n';
                    }
                    result.message += 'Element ' + index + ' does not match, expected ' + JSON.stringify(expectedArray[index]) + ', but was ' + JSON.stringify(actual[index]);
                    matchError = true;
                }
            });

            if(matchError) {
                return result;
            }

            result.pass = true;
            return result;
        }
    };
};

neontest.matchers.toBeInstanceOf = function(util, customEqualityTesters) {
    return {
        compare: function(actual, expectedType) {
            var result = {
                pass: false
            };

            result.pass = util.equals(actual, jasmine.any(expectedType), customEqualityTesters);
            if (!result.pass) {
                // constructor.name is not defined in all browsers.
                var typeName = (Function.prototype.name !== undefined) ? actual.constructor.name : actual.constructor;
                result.message = 'Expected ' + actual + ' to be instance of ' + expectedType +
                    ', but was ' + typeName;
            }
            return result;
        }
    };
};


/**
 *
 * Checks if the expected array is the same as the actual, independent of order. This does not
 * compare individual keys/values of objects, it just uses equality checks
 * @param expectedArray
 * @methods
 * @returns {boolean}
 */
neontest.matchers.toBeArrayWithSameElements = function(util, customEqualityTesters) {
    return {
        compare: function(actual, expectedArray) {
            var result = {
                pass: false
            };
            result.message = 'expected: ' + expectedArray + ', actual: ' + actual;
            result.pass = (expectedArray.length === actual.length && lodash.difference(expectedArray, actual).length === 0);

            return result;
        }
    };
};

// this adds the matchers globally for all tests
beforeEach(function() {
    var matchers = {
        toBeEqualArray: neontest.matchers.toBeEqualArray,
        toBeInstanceOf: neontest.matchers.toBeInstanceOf,
        toBeArrayWithSameElements: neontest.matchers.toBeArrayWithSameElements
    };

    jasmine.addMatchers(matchers);
});
