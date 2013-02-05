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
 */
jasmine.Matchers.prototype.toBeEqualArray = function (expectedArray) {

    var err = '';

    this.message = function () {
        return err;
    };

    if ( !(this.actual instanceof Array)  ) {
        err = 'Actual value not an array, received a ' + this.actual.constructor.name + ' instead';
        return false;
    }

    if ( !(expectedArray instanceof Array)  ) {
        err = 'Test configuration error. Expected value not an array, received a ' + expectedArray.constructor.name + ' instead';
        return false;
    }

    if ( expectedArray.length !== this.actual.length ) {
        err = 'Expected array of length ' + expectedArray.length + ', but was ' + this.actual.length;
        return false;
    }


    var matchError = false;
    this.actual.forEach(function (element, index, actual) {
        if (expectedArray[index] !== actual[index]) {
            if (err) {
                err += '\r\n';
            }
            err += 'Element ' + index + ' does not match, expected ' + expectedArray[index] + ', but was ' + actual[index];
            matchError = true;
        }
    });
    if ( matchError ) {
        return false;
    }


    return true;
};

jasmine.Spy.prototype.wasInvoked = function() {
    return this.callCount > 0;
};