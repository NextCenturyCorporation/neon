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

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.GroupByClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.result.QueryResult

/**
 * Utility class for SSE queries.
 */
class AggregationOperations {

    // Defines the number of records that make one "iteration" for calculating mean, variance, and error.
    private static final int ITER_SIZE = 1000

    /**
     * Takes the results from one iteration of a <code>count</code> aggregation and data about the query it was performed on
     * and does some statistical analysis to determine estimated mean, variance, and error for each item in the results
     * before returning a list of the current estimates.
     * @param result The QueryResult containing results from this iteration of the query.
     * @param queryData The SseQueryData object containing information about the query that produced the results.
     * @return A list of the current estimated values for everything in the given SseQueryData's runningResults.
     */
    static List count(QueryResult result, SseQueryData queryData) {
        Map results = queryData.runningResults
        String fieldName = queryData.query.aggregates[0].name
        for(int x = 0; x < result.data.size(); x++) {
            Map point = result.data.get(x)
            long count = point[fieldName]
            String id = makeId(point, queryData.query)
            double totalSeen = queryData.count * queryData.randMax
            double seenThisTime = totalSeen - (queryData.count * queryData.randMin)
            // Separates the seen records into "iterations" for calculating variance and error.
            double iterationsTotal = totalSeen / ITER_SIZE
            double iterationsThisChunk = seenThisTime / ITER_SIZE
            double singleIterationCount = count / iterationsThisChunk
            double mean
            double error
            if(results[id]) {
                results[id].totalMean += count
                // Split total count into what it would be for each iteration of ITER_SIZE and square that, then multiply result by number of iterations.
                results[id].totalVar += (singleIterationCount * singleIterationCount) * iterationsThisChunk
                mean = results[id].totalMean / iterationsTotal
                double variance = results[id].totalVar / iterationsTotal - (mean * mean)
                error = Math.sqrt(queryData.zp * queryData.zp * variance / iterationsTotal)
            }
            else {
                results[id] = [totalMean: count, totalVar: (singleIterationCount * singleIterationCount) * iterationsThisChunk] as SinglePointStats
            }
            results[id].resultantMean = mean * iterationsTotal * (queryData.count / totalSeen)
            results[id].resultantError = error * iterationsTotal * (queryData.count / totalSeen)
        }
        return makeResults(queryData)
    }

    /**
     * Takes the results from one iteration of a <code>sum</code> aggregation and data about the query it was performed on
     * and does some statistical analysis to determine estimated mean, variance, and error for each item in the results
     * before returning a list of the current estimates.
     * @param result The QueryResult containing results from this iteration of the query.
     * @param queryData The SseQueryData object containing information about the query that produced the results.
     * @return A list of the current estimated values for everything in the given SseQueryData's runningResults.
     */
    static List sum(QueryResult result, SseQueryData queryData) {
        Map results = queryData.runningResults
        String fieldName = queryData.query.aggregates[0].name
        for(int x = 0; x < result.data.size(); x++) {
            Map point = result.data.get(x)
            long count = point[fieldName]
            String id = makeId(point, queryData.query)
            double totalSeen = queryData.count * queryData.randMax
            double seenThisTime = totalSeen - (queryData.count * queryData.randMin)
            // Separates the seen records into "iterations" for the purposes of calculating variance and error.
            long iterationsTotal = totalSeen / ITER_SIZE
            long iterationsThisChunk = seenThisTime / ITER_SIZE
            double singleIterationCount = count / iterationsThisChunk
            double mean
            double error
            if(results[id]) {
                results[id].totalMean += count
                // Split total count into what it would be for each iteration of ITER_SIZE and square that, then multiply result by number of iterations.
                results[id].totalVar += (singleIterationCount * singleIterationCount) * iterationsThisChunk
                mean = results[id].totalMean / iterationsTotal
                double variance = results[id].totalVar / iterationsTotal - (mean * mean)
                error = Math.sqrt(queryData.zp * queryData.zp * variance / iterationsTotal)
            }
            else {
                results[id] = [totalMean: count, totalVar: (singleIterationCount * singleIterationCount) * iterationsThisChunk] as SinglePointStats
            }
            results[id].resultantMean = mean * iterationsTotal * (queryData.count / totalSeen)
            results[id].resultantError = error * iterationsTotal * (queryData.count / totalSeen)
        }
        return makeResults(queryData)
    }

    /**
     * Makes the results list for a given SseQueryData object, to be returned to the client.
     * @param queryData The SseQueryData object to make the results list for.
     * @return The results list for the given SseQueryData object.
     */
    static List makeResults(SseQueryData queryData) {
        List results = []
        queryData.runningResults.each {key, value ->
            results << [_id: key, mean: value.resultantMean, error: value.resultantError]
        }
        return results
    }

    /**
     * Creates a unique ID for a single item in a QueryResult by aggregating the name ofevery
     * field in the item's query's groupByClauses and the item's value for that field in a string.
     * @param singleResult The QueryResult item to create an ID for.
     * @param query The query from which to pull groupByClauses values from.
     * @return A string of the form {"clause1Name":"clause1Value","clause2Name":"clause2Value",etc}
     */
    private static String makeId(Map singleResult, Query query) {
        List idPieces = []
        for(int x = 0; x < query.groupByClauses.size(); x ++) {
            GroupByClause clause = query.groupByClauses.get(x)
            if(clause instanceof GroupByFieldClause) {
                idPieces << '"' + clause.field + '":"' + singleResult[clause.field] + '"'
            }
            else if(clause instanceof GroupByFunctionClause) {
                idPieces << '"' + clause.name + '":"' + singleResult[clause.name] + '"'
            }
        }
        return '{' + idPieces.join(',') + '}'
    }
}