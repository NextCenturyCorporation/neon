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
import com.ncc.neon.sse.SseQueryData
import com.ncc.neon.sse.RecordCounter
import com.ncc.neon.sse.RecordCounterFactory

import groovy.json.JsonOutput

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

import java.util.concurrent.ConcurrentHashMap

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

    private final int MAX_ITERATION_TIME = 10000 // Maximum time a single iteration of the aggregation should be allowed to take, in milliseconds.
    private final String RANDOM_FIELD_NAME= 'random'
    private static final Map CURRENTLY_RUNNING_QUERIES_DATA = new ConcurrentHashMap<String, SseQueryData>()

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
        SseQueryData queryData = initDataWithCollectionValues(host, databaseType, query.filter.databaseName, query.filter.tableName)
        queryData.host = host
        queryData.databaseType = databaseType
        queryData.ignoreFilters = ignoreFilters
        queryData.selectionOnly = selectionOnly
        queryData.ignoredFilterIds = ignoredFilterIds
        queryData.query = query
        String uuid = UUID.randomUUID().toString()
        CURRENTLY_RUNNING_QUERIES_DATA.put(uuid, queryData)
        return threadOnlineQuery(uuid, queryData)
    }

// TODO - figure out how to do the statistics stuff, preferably generically. Look into how groupbyclauses work?
    private EventOutput threadOnlineQuery(String uuid, SseQueryData queryData) {
        final EventOutput OUTPUT = new EventOutput()
        new Thread() {
            public void run() {
                try {
                    // First, return the uuid of the query so it can be cancelled.
                    OUTPUT.write(new OutboundEvent.Builder().data(String, uuid).build())

                    while(!queryData.complete) {
                        // If a query is not active and also not complete, it was cancelled. Remove it from the list of running queries.
                        if(!queryData.active) {
                            CURRENTLY_RUNNING_QUERIES_DATA.remove
                        }
                        QueryResult result = runSingleQueryIteration(queryData)
                        updateResults(result, queryData)
                        OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(queryData.results)).build())
                    }
                    OUTPUT.close()
                }
                catch(final InterruptedException | IOException e) {
                    Exception ex = new Exception().setStackTrace(e.getStackTrace())
                    throw ex
                }
            }
        }.start()
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
        queryData.randMin += queryData.randStep
        queryData.randMax += queryData.randStep
        applyRandomFilter(query, queryData.randMin, queryData.randMax)
        QueryResult result = queryService.executeQuery(queryData.host, queryData.databaseType, queryData.ignoreFilters, queryData.selectionOnly, queryData.ignoredFilterIds, queryData.query)
        //long endTime = System.currentTimeMillis()
        //long elapsedTime = endTime - startTime
        //queryData.randStep *= (MAX_ITERATION_TIME / elapsedTime)
        return result
    }

    private void updateResults(QueryResult result, SseQueryData queryData) {
        // Adds results of one iteration of a query to the overall results for that query.
        result.each { point ->
            String strId = JsonOutput.toJson(point._id)


        }

        
        // TODO what does it even mean when a query has multiple aggregates clauses? How does that work and how should it be handled?
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
     *                             randMax: initial upper bound for random field]
     */
    private SseQueryData initDataWithCollectionValues(String host, String databaseType, String databaseName, String tableName) {
        RecordCounter counter = recordCounterFactory.getRecordCounter(databaseType)
        long recordCount = counter.getCount(host, databaseName, tableName)
        double initialIterations = recordCount / 1000
        double randStep = 1.0 / initialIterations
        SseQueryData queryData = [active: true, complete: false, count: recordCount, randStep: randStep, randMin: 0.0, randMax: 0.0 + randStep] as SseQueryData
        return queryData
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

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("test")
    public EventOutput testSse() {
        final EventOutput OUTPUT = new EventOutput()
        new Thread() {
            public void run() {
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
        }.start()
        return OUTPUT
    }
}