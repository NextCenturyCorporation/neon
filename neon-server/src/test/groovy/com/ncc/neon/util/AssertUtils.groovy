package com.ncc.neon.util

import org.apache.commons.collections.CollectionUtils

import static org.junit.Assert.assertTrue



/**
 * Utility methods for helping with assertions
 */
class AssertUtils {

    static def assertEqualCollections(expected, actual) {
        assertTrue("expected ${expected}, actual: ${actual}",CollectionUtils.isEqualCollection(expected,actual))
    }
}
