package com.ncc.neon.sse

import com.ncc.neon.query.Query
import com.ncc.neon.query.clauses.GroupByClause
import com.ncc.neon.query.clauses.GroupByFieldClause
import com.ncc.neon.query.clauses.GroupByFunctionClause
import com.ncc.neon.query.result.QueryResult

import java.util.logging.Logger

class AggregationOperations {

    // Re-add totalMean and totalVar to SinglePointStats if going back to this, or just rename to mean and var where used.
    /*static void countOld(QueryResult result, SseQueryData queryData) {
        queryData.traversed = queryData.count * queryData.randMax
        Map results = queryData.runningResults
        String fieldName = queryData.query.aggregates[0].name
        for(int x = 0; x < result.data.size(); x++) {
            Map point = result.data.get(x)
            double count = point[fieldName]
            String id = makeId(point, queryData.query)

            if(results[id]) {
                results[id].totalMean = results[id].totalMean + count
                results[id].totalVar = results[id].totalVar + (count * count)
                double var = results[id].totalVar / queryData.traversed
                results[id].error = Math.sqrt(queryData.zp * var / queryData.traversed)
            }
            else {
                results[id] = [totalMean: count, totalVar: count * count] as SinglePointStats
                double var = results[id].totalVar / queryData.traversed
                results[id].error = Math.sqrt(queryData.zp * var / queryData.traversed)
            }
            point['mean'] = results[id].totalMean * (queryData.count / queryData.traversed)
            point['error'] = results[id].error * (queryData.count / queryData.traversed)
        }
    }*/

    static void count(QueryResult result, SseQueryData queryData) {
        Map results = queryData.runningResults
        String fieldName = queryData.query.aggregates[0].name
        for(int x = 0; x < result.data.size(); x++) {
            Map point = result.data.get(x)
            double count = point[fieldName]
            String id = makeId(point, queryData.query)
            long oldN = queryData.count * queryData.randMin
            long newN = queryData.count * queryData.randMax

            if(results[id]) {
                double oldEx = results[id].mean
                results[id].mean = ((results[id].mean * oldN) + count) / newN
                // Updating variance uses the fact that we know Var(X) = E(X*X) - E(X)*E(X) and know how to update E(X).
                results[id].var = ((((results[id].var + (oldEx * oldEx)) * oldN) + (count * count)) / newN) - (results[id].mean * results[id].mean)
                results[id].error = Math.sqrt(queryData.zp * results[id].var / newN)
            }
            else {
                results[id] = [mean: count / newN] as SinglePointStats
                results[id].var = (count * count / newN) - (results[id].mean * results[id].mean)
                results[id].error = Math.sqrt(queryData.zp * results[id].var / newN)
            }
            // if(x == 0) { Logger.getLogger("").info("\tMean: ${results[id].mean}\tVariance: ${results[id].var}\tError:${results[id].error}" as String) }
            point['mean'] = Math.round(results[id].mean * queryData.count)
            point['error'] = Math.round(results[id].error * queryData.count)
        }
    }

    static void sum(QueryResult result, SseQueryData queryData) {
        Map results = queryData.runningResults
        String fieldName = queryData.query.aggregates[0].name
        for(int x = 0; x < result.data.size(); x++) {
            Map point = result.data.get(x)
            double count = point[fieldName]
            String id = makeId(point, queryData.query)
            long oldN = queryData.count * queryData.randMin
            long newN = queryData.count * queryData.randMax

            if(results[id]) {
                double oldEx = results[id].mean
                results[id].mean = ((results[id].mean * oldN) + count) / newN
                // Updating variance uses the fact that we know Var(X) = E(X*X) - E(X)*E(X) and know how to update E(X).
                results[id].var = ((((results[id].var + (oldEx * oldEx)) * oldN) + (count * count)) / newN) - (results[id].mean * results[id].mean)
                results[id].error = Math.sqrt(queryData.zp * results[id].var / newN)
            }
            else {
                results[id] = [mean: count / newN] as SinglePointStats
                results[id].var = (count * count / newN) - (results[id].mean * results[id].mean)
                results[id].error = Math.sqrt(queryData.zp * results[id].var / newN)
            }
            // if(x == 0) { Logger.getLogger("").info("\tMean: ${results[id].mean}\tVariance: ${results[id].var}\tError:${results[id].error}" as String) }
            point['mean'] = Math.round(results[id].mean * queryData.count)
            point['error'] = Math.round(results[id].error * queryData.count)
        }
    }

    /**
     * Creates a unique ID for a single item in a QueryResult by aggregating every name and
     * value in its query's groupByClauses in a string.
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