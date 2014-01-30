

var neontest = neontest || {};
neontest.matchers = {};
neontest.matchers.matcher = new jasmine.Matchers();

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

    return expectedArray.length == actual.length && lodash.difference(expectedArray, actual).length == 0;
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
