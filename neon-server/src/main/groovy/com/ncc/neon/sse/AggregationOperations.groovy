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

import com.ncc.neon.query.result.QueryResult
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Utility class for SSE queries.
 */
class AggregationOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationOperations)

    // Defines the number of records that make one "iteration" for calculating mean, variance, and error.
    //private static final int ITER_SIZE = 1000

    /**
     * Takes the results from one iteration of a <code>count</code> aggregation and data about the query it was
     * performed on and does statistical analysis to determine estimated mean, variance, and error for each
     * item in the results
     *
     * How this works for means:
     *    -- countThisIteration :  amount from this iteration
     *    -- sum : sum of all countThisIteration
     *    -- mean : sum / randMax as estimate;  when randMax=1, this is final count
     *
     *  How this works for vars:
     *    -- estimate =
     *
     * @param queryResult The QueryResult containing results from this iteration of the query.
     * @param sseQueryData The SseQueryData object containing information about the query that produced the results.
     * @return A list of the current estimated values for everything in the given SseQueryData's runningResults.
     */
    @SuppressWarnings(['MethodSize'])
    static void count(SseQueryData sseQueryData) {
        Map<Object, SinglePointStats> runningResults = sseQueryData.runningResults
        String fieldName = sseQueryData.query.aggregates[0].name
        QueryResult queryResult = sseQueryData.queryResult

        // Get the total number of points retrieved this time, and to date
        int numDataPoints = queryResult.data.size()
        long totalRecordsThisIteration = 0
        for (int x = 0; x < numDataPoints; x++) {
            Map dataPoint = queryResult.data.get(x)
            totalRecordsThisIteration += dataPoint[fieldName]
        }
        sseQueryData.recordsToDate += totalRecordsThisIteration
        sseQueryData.iterationsToDate++

        LOGGER.debug("iteration " + sseQueryData.iterationsToDate + " total to date " + sseQueryData.recordsToDate +
                " and this iteration " + totalRecordsThisIteration)

        for (int x = 0; x < numDataPoints; x++) {

            // dataPoint is a single result, for example, a single day count
            Map dataPoint = queryResult.data.get(x)
            String id = JsonOutput.toJson(dataPoint.get("_id"))

            long countThisIteration = dataPoint[fieldName]

            SinglePointStats sps = runningResults[id]
            if (!sps) {
                // estimate for the entire result, extrapolating from current data point
                long estimate = countThisIteration / sseQueryData.randMax
                sps = [var               : 0,
                       mean              : estimate,
                       confidenceInterval: 0] as SinglePointStats
                runningResults.put(id, sps)
            }

            // Treat the estimate as as sample from a population. Take the 'easy' way and do not weight them
            // individually, but rather sum them over all trials to date, and then divide by the rand max.
            // See the first formula of https://en.wikipedia.org/wiki/Weighted_arithmetic_mean
            sps.sumOfIterations += countThisIteration
            sps.mean = sps.sumOfIterations / sseQueryData.randMax
            dataPoint.put("mean", sps.mean)

            // Variance is E(est - mean)^2, but simplified it is E[est^2] - [E(est)]^2 = E(est^2)-mean^2
            // Note that this is not quite right when we have weighted (uneven) samples, but is close.  See
            // wikipedia article above
            double estimate = countThisIteration / sseQueryData.randStep
            double estSquared = (estimate * estimate)
            sps.sumOfSquaredEstimate += estSquared
            double partA = (sps.sumOfSquaredEstimate / sseQueryData.iterationsToDate)

            sps.sumOfEstimate += estimate
            double avgOfEstimate = sps.sumOfEstimate / sseQueryData.iterationsToDate
            double partB = avgOfEstimate * avgOfEstimate

            sps.var = partA - partB
            sps.confidenceInterval = Math.sqrt(sseQueryData.zp * sseQueryData.zp * sps.var / sseQueryData.iterationsToDate)
            dataPoint.put(fieldName, countThisIteration)
            dataPoint.put("error", sps.confidenceInterval)

            // When we reach the end, then the confidence interval should be zero
            if (sseQueryData.randMax >= 1.0) {
                dataPoint.put("error", 0)
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" id " + id + " thisIter " + countThisIteration + " sum " + sps.sumOfIterations +
                        " mean " + sps.mean + " EstThisIter " + estimate + " AvgOfEst " + avgOfEstimate +
                        " EstSquared " + estSquared + " sum of SqEst " + sps.sumOfSquaredEstimate + " partA " + partA +
                        " partB " + partB + " var " + sps.var +
                        " confidence interval " + sps.confidenceInterval + " zp " + sseQueryData.zp)
            }
            sseQueryData.grandTotal += countThisIteration
        }
        LOGGER.debug("Grand total " + sseQueryData.grandTotal)
    }
}
