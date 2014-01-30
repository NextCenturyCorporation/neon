package com.ncc.neon.query

import groovy.transform.Canonical



/**
 * Provides a way to look up a Transformer. The transformName should be the
 * fully qualified class name of the Transformer implementation. Parameters can be any object
 * that configures the Transformer's convert() method.
 */

@Canonical
class Transform {
    String transformName
    def params
}
