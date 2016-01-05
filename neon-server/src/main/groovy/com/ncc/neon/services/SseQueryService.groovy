/*
 * Copyright 2015 Next Century Corporation
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

package com.ncc.neon.services

import com.ncc.neon.query.*
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.sse.RecordCounter
import com.ncc.neon.sse.RecordCounterFactory
import com.ncc.neon.sse.SinglePointStats
import com.ncc.neon.sse.SseQueryData

import groovy.json.JsonOutput

import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.*

import javax.ws.rs.core.MediaType

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.apache.commons.math3.distribution.NormalDistribution

import org.glassfish.jersey.media.sse.EventOutput
import org.glassfish.jersey.media.sse.OutboundEvent
import org.glassfish.jersey.media.sse.OutboundEvent.Builder
import org.glassfish.jersey.media.sse.SseFeature

@Component
@Path("/ssequeryservice")
class SseQueryService {

    @Autowired
    QueryService queryService

    @Autowired
    RecordCounterFactory recordCounterFactory

    private final int MAX_ITERATION_TIME = 1000 // Maximum time a single iteration of the aggregation should be allowed to take, in milliseconds.
    private final int INITIAL_RECORDS_PER_ITERATION = 10000
    private final String RANDOM_FIELD_NAME= 'random'
    private static final Map CURRENTLY_RUNNING_QUERIES_DATA = new ConcurrentHashMap<String, List<SseQueryData>>()

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("query/{host}/{databaseType}")
    EventOutput executeOnlineQuery(@PathParam("host") String host,
                             @PathParam("databaseType") String databaseType,
                             @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                             @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                             @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                             Query query) {
        SseQueryData queryData = initDataWithCollectionValues(host, databaseType, query.getDatabaseName(), query.getTableName())
        queryData.host = host
        queryData.databaseType = databaseType
        queryData.ignoreFilters = ignoreFilters
        queryData.selectionOnly = selectionOnly
        queryData.ignoredFilterIds = ignoredFilterIds
        queryData.query = query
        queryData.zp = 1.6500000000000012
        // queryData.zp = getZp(query.confidence)
        String uuid = UUID.randomUUID().toString()
        CURRENTLY_RUNNING_QUERIES_DATA.put(uuid, [queryData])
        return threadOnlineQuery(uuid, queryData)
    }

    private EventOutput threadOnlineQuery(String uuid, SseQueryData queryData) {
        final EventOutput OUTPUT = new EventOutput()
        Thread.start {
            try {
                // First, return the uuid of the query so it can be cancelled.
                OUTPUT.write(new OutboundEvent.Builder().data(String, uuid).build())
                while(!queryData.complete) {
                    // If a query is not active and also not complete, it was cancelled. Remove it from the list of running queries and quit.
                    if(!queryData.active) {
                        CURRENTLY_RUNNING_QUERIES_DATA.remove(uuid)
                        return
                    }
                    QueryResult result = runSingleQueryIteration(queryData)
                    updateResults(result, queryData)
                    OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(result)).build())
                }
            }
            catch(final InterruptedException | IOException e) {
                throw new Exception().setStackTrace(e.getStackTrace()) // TODO - this code does nothing useful. Either come up with something better or remove the catch block.
            }
            finally {
                OUTPUT.close()
            }
        }
        return OUTPUT
    }

    /**
     * Runs a single iteration of a query, recording the time it takes and accordingly
     * adjusting how large a segment of records should be looked through next time. (adjustment of record segments is currently commented out)
     * @param queryData An SseQueryData object containing all of the reuired data about the query to execute.
     * @return The result of this iteration of the query.
     */
    private QueryResult runSingleQueryIteration(SseQueryData queryData) {
        //long startTime = System.currentTimeMillis()
        applyRandomFilter(query, queryData.randMin, queryData.randMax)
        QueryResult result = queryService.executeQuery(queryData.host, queryData.databaseType, queryData.ignoreFilters, queryData.selectionOnly, queryData.ignoredFilterIds, queryData.query)
        //long endTime = System.currentTimeMillis()
        //long elapsedTime = endTime - startTime
        //queryData.randStep *= (MAX_ITERATION_TIME / elapsedTime)
        queryData.randMin += queryData.randStep
        queryData.randMax += queryData.randStep
        return result
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("querygroup/{host}/{databaseType}")
    EventOutput executeOnlineQueryGroup(@PathParam("host") String host,
                             @PathParam("databaseType") String databaseType,
                             @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                             @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                             @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                             QueryGroup queryGroup) {
        List<SseQueryData> queryList = []
        queryGroup.each { query ->
            SseQueryData queryData = initDataWithCollectionValues(host, databaseType, query.getDatabaseName(), query.getTableName())
            queryData.host = host
            queryData.databaseType = databaseType
            queryData.ignoreFilters = ignoreFilters
            queryData.selectionOnly = selectionOnly
            queryData.ignoredFilterIds = ignoredFilterIds
            queryData.query = query
            queryData.zp = 1.6500000000000012
            // queryData.zp = getZp(query.confidence)
            queryList << queryData
        }
        String uuid = UUID.randomUUID().toString()
        CURRENTLY_RUNNING_QUERIES_DATA.put(uuid, queryList)
    }

    /**
     * Adds the results from one iteration of a query to the overall statistical results for that query, and
     * alters the results object from that iteration to include the statistical results instead of just
     * the results from that iteration.
     * 
     * TODO - what does it even mean when a query has multiple aggregates clauses? How does that work and how should it be handled?
     * 
     * @param result The results from a single iteration of a query.
     * @param The query that returned the given results object.
     */
    private void updateResults(QueryResult result, SseQueryData queryData) {
        // Just so we don't have to preface every use of results with "queryData.".
        Map results = queryData.runningResults

        // Adds the number of records traversed this iteration to the number of records traversed overall.
        queryData.traversed += results.sum { point -> point.count }

        // Adds results of one iteration of a query to the overall results for that query. Currently assumes returned field is named "count".
        result.each { point ->
            String id = makeId(point, queryData.query)
            if(!point.count) { // Skip anything that doesn't have a "count" field.
                continue
            }
            if(results.id) {
                results.id.totalMean = results.id.totalMean + point.count
                results.id.totalVar = results.id.totalVar + (point.count * point.count)
                double var = results.id.totalVar / queryData.traversed
                results.id.error = Math.sqrt(queryData.zp * var / queryData.traversed)
            }
            else {
                results.id = [totalMean: point.count, totalVar: point.count * point.count, error: Math.sqrt(point.count * point.count * queryData.zp / queryData.traversed)] as SinglePointStats
            }
            point.mean = results.id.totalMean * (queryData.count / queryData.traversed)   
            point.error = results.id.error * (queryData.count / queryData.traversed)
        }
    }

    /**
     * Creates a unique ID for a single item in a QueryResult by aggregating every name and
     * value in its query's groupByClauses in a string.
     * @param singleResult The QueryResult item to create an ID for.
     * @param query The query from which to pull groupByClauses values from.
     * @return A string of the form {"clause1Name":"clause1Value","clause2Name":"clause2Value",etc}
     */
    private String makeId(Map singleResult, Query query) {
        List idPieces = []
        query.groupByClauses.each { clause ->
            if(clause instanceof GroupByFieldClause) {
                idPieces << '"' + clause.field + '":"' + singleResult[clause.field] + '"'
            }
            else if(clause instanceof GroupByFunctionClause) {
                idPieces << '"' + clause.name + '":"' + singleResult[clause.name] + '"'
            }
        }
        return '{' + idPieces.join(',') + '}'
    }

    /**
     * Initializes the step/iteration values for a given collection. Gets the number of records in that
     * collection and sets the intial step, min, and max values for the random value field accordingly.
     * @param host The host on which the collection is stored.
     * @param databaseType The type of database on which the collection is stored (converted to a <code>com.ncc.neon.connect.Datasources</code>).
     * @param databaseName The name of the database storing the collection to get values for.
     * @param tableName The name of the collection to get values for.
     * @return A map, of the form [active: true,
     *                             complete: false,
     *                             count: number of records in collection,
     *                             randStep: initial step value for random field,
     *                             randMin: initial lower bound for random field,
     *                             randMax: initial upper bound for random field
     *                             traversed: 0]
     * coerced to a com.ncc.neon.sse.SseQueryData object.
     */
    private SseQueryData initDataWithCollectionValues(String host, String databaseType, String databaseName, String tableName) {
        RecordCounter counter = recordCounterFactory.getRecordCounter(databaseType)
        long recordCount = counter.getCount(host, databaseName, tableName)
        double initialIterations = recordCount / INITIAL_RECORDS_PER_ITERATION
        double randStep = 1.0 / initialIterations
        SseQueryData queryData = [active: true, complete: false, count: recordCount, randStep: randStep, randMin: 0.0, randMax: 0.0 + randStep, traversed: 0] as SseQueryData
        return queryData
    }

    private double getZp(double confidence) {
        NormalDistribution norm = new NormalDistribution(0, 1)
        double requiredVal = (confidence + 1) / 2
        for(double x = 0; x < 10; x += 0.01) {
            if(norm.cumulativeProbability(x) > requiredVal) {
                return norm.cumulativeProbability(x)
            }
        }
        return 10
    }

    /**
     * Applies a filter to a query on the designated random field, adding the filter if it does not exist or modifying it if it does.
     * @param query The query to add the filter to.
     * @param randMin The minimum random value to find (exclusive).
     * @param randMax the maximum random value tofind (inclusive).
     */
    private void applyRandomFilter(Query query, double randMin, double randMax) { // TODO Would it be worth it to make this method store references to the random WhereClauses
        WhereClause currentClause = query.filter.whereClause                      // alongside query IDs for faster processing once they're created, once loop is iterating?
        if(!(currentClause instanceof AndWhereClause)) {
            AndWhereClause newClause = new AndWhereClause()
            newClause.whereClauses << currentClause
            newClause.whereClauses << [lhs: RANDOM_FIELD_NAME, operator: ">", rhs: "$randMin"] as SingularWhereClause
            newClause.whereClauses << [lhs: RANDOM_FIELD_NAME, operator: "<=", rhs: "$randMax"] as SingularWhereClause
            query.filter.whereClause = newClause
        }
        else {
            boolean minClause = false, maxClause = false
            currentClause.whereClauses.each { clause ->
                if(clause instanceof SingularWhereClause && clause.lhs == RANDOM_FIELD_NAME && clause.operator == ">") {
                    clause.rhs = "$randMin"
                    minClause = true
                }
                else if(clause instanceof SingularWhereClause && clause.lhs == RANDOM_FIELD_NAME && clause.operator == "<=") {
                    clause.rhs = "$randMax"
                    maxClause = true
                }
            }
            if(!minClause) {
                currentClause.whereClauses << [lhs: RANDOM_FIELD_NAME, operator: ">", rhs: "$randMin"] as SingularWhereClause
            }
            if(!maxClause) {
                currentClause.whereClauses << [lhs: RANDOM_FIELD_NAME, operator: "<=", rhs: "$randMax"] as SingularWhereClause
            }
        }
    }

    /**
     * Simple testing method, just used to make sure server-sent events are working. Returns an EventOutput and
     * uses a new thread to send updates to that EventOutput every two seconds for ten seconds before closing it.
     * @return An EventOutput which will send updates.
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("test")
    EventOutput testSse() {
        final EventOutput OUTPUT = new EventOutput()
        Thread.start {
            try {
                for(int x = 0; x < 5; x++) {
                    Map m = [key: "value " + x]
                    OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(m)).build())
                    Thread.sleep(2000)
                }
                OUTPUT.close()
            }
            catch (final InterruptedException | IOException e) {
                Exception ex = new Exception()
                ex.setStackTrace(e.getStackTrace())
                throw ex
            }
        }
        return OUTPUT
    }
}