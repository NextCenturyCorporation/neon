package com.ncc.neon.sse

import com.ncc.neon.query.result.QueryResult
import groovy.transform.ToString

@ToString(includeNames = true)
class SinglePointStats {

    // The mean value (or expected value, E(X)) of the object this SinglePointStats object corresponds to.
    double mean

    // The variance (Var(X)) of the object this SinglePointStats object corresponds to.
    double var

    double confidenceInterval

    double sumOfIterations

    double sumOfEstimate
    double sumOfSquaredEstimate

}