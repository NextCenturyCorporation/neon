/*
 * Copyright 2016 Next Century Corporation
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

package com.ncc.neon.sse

/**
 * Stores statistical information about a single item in the results of a single query,
 * e.g. for a temporal query there might be a SinglePointStats for "June 8, 2013".
 */
class SinglePointStats {

    // The mean value (or expected value, E(X)) of the object this SinglePointStats object corresponds to.
    double totalMean

    // The variance (Var(X)) of the object this SinglePointStats object corresponds to.
    double totalVar

    // The error of the object this SinglePointStats object corresponds to.
    double error

    // The estimated result value over the entire data set.
    double resultantMean

    // The estimated error value over the entire data set.
    double resultantError
}