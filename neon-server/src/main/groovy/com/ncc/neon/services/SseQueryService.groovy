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

package com.ncc.neon.services

import com.ncc.neon.connect.ConnectionInfo
import com.ncc.neon.connect.DataSources
import com.ncc.neon.query.*
import com.ncc.neon.query.clauses.*
import com.ncc.neon.query.executor.QueryExecutor
import com.ncc.neon.query.filter.GlobalFilterState
import com.ncc.neon.query.filter.CopiedFilterState
import com.ncc.neon.query.result.QueryResult
import com.ncc.neon.query.result.TabularQueryResult
import com.ncc.neon.sse.*

import groovy.json.JsonOutput

import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.*

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.apache.commons.math3.distribution.NormalDistribution

import org.glassfish.jersey.media.sse.EventOutput
import org.glassfish.jersey.media.sse.OutboundEvent
import org.glassfish.jersey.media.sse.OutboundEvent.Builder
import org.glassfish.jersey.media.sse.SseFeature

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
@Path("/ssequeryservice")
class SseQueryService {

    @Autowired
    RecordCounterFactory recordCounterFactory

    @Autowired
    GlobalFilterState filterState

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    //private static final int MAX_ITERATION_TIME = 1000 // Maximum time a single iteration of the aggregation should be allowed to take, in milliseconds.
    private static final int INITIAL_RECORDS_PER_ITERATION = 100000
    private static final String RANDOM_FIELD_NAME= 'rand'
    private static final Map CURRENTLY_RUNNING_QUERIES_DATA = new ConcurrentHashMap<String, List<SseQueryData>>()

    private static final Logger LOGGER = LoggerFactory.getLogger(SseQueryService)

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("query/{host}/{databaseType}")
    Response storeQueryData(@PathParam("host") String host,
                            @PathParam("databaseType") String databaseType,
                            @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                            @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                            @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                            Query query) {
        SseQueryData queryData = initDataWithCollectionValues(host, databaseType, query.filter.databaseName, query.filter.tableName,
                                                                  ignoreFilters, selectionOnly, ignoredFilterIds, query)
        Map response = [uuid: addToRunningQueries([queryData])]
        return Response.ok().entity(JsonOutput.toJson(response)).build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("querygroup/{host}/{databaseType}")
    Response storeQueryGroupData(@PathParam("host") String host,
                                 @PathParam("databaseType") String databaseType,
                                 @DefaultValue("false") @QueryParam("ignoreFilters") boolean ignoreFilters,
                                 @DefaultValue("false") @QueryParam("selectionOnly") boolean selectionOnly,
                                 @QueryParam("ignoredFilterIds") Set<String> ignoredFilterIds,
                                 QueryGroup queryGroup) {
        List queryList = []
        for(int x = 0; x < queryGroup.queries.size(); x++) {
            Query query = queryGroup.queries.get(x)
            SseQueryData queryData = initDataWithCollectionValues(host, databaseType, query.filter.databaseName, query.filter.tableName,
                                                                  ignoreFilters, selectionOnly, ignoredFilterIds, query)
            queryList << queryData
        }
        Map response = [uuid: addToRunningQueries(queryList)]
        return Response.ok().entity(JsonOutput.toJson(response)).build()
    }

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("getupdates/{uuid}")
    EventOutput executeByUuid(@PathParam("uuid") String uuid) {
        return threadOnlineQueryOrGroup(uuid)
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("cancel/{uuid}")
    EventOutput cancelUuid(@PathParam("uuid") String uuid) {
        CURRENTLY_RUNNING_QUERIES_DATA.remove(uuid)
        return Response.ok().build()
    }

    /**
     * Creates a new thread to run a query or group of queries, given the UUID associated with that query or group of queries.
     * Returns an SSE event output, through which updates as to the progress of the query or group will be sent.
     * @param uuid The UUID associated with the query or group of queries to be run, used to find the query or group in the map of stored queries.
     * @return An SSE event stream that will be used to pass updates to the client.
     */
    private EventOutput threadOnlineQueryOrGroup(String uuid) {
        final EventOutput OUTPUT = new EventOutput()
        CopiedFilterState copiedFilterState = new CopiedFilterState(filterState)
        LOGGER.debug("Starting SSE Thread")
        Thread.start {
            try {
                List<SseQueryData> queries = CURRENTLY_RUNNING_QUERIES_DATA[uuid]
                boolean allComplete = false
                while(!allComplete) {
                    if(!CURRENTLY_RUNNING_QUERIES_DATA[uuid]) { // TODO - If this is kept as-is, I don't think that SseQueryData needs an "active" field.
                        OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson([])).id("cancel").build()) // False indicates the query was cancelled, not finished.
                        return
                    }
                    QueryResult result = runSingleIteration(queries, copiedFilterState)
                    OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(result)).id("update").build())
                    allComplete = true
                    for(SseQueryData queryData : queries) {
                        allComplete = allComplete && queryData.complete
                    }
                }
                OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson([])).id("done").build())
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
     * Runs a single iteration of a query or group of queries, recording the time it takes and accordingly adjusting
     * how large a segment of records should be looked through next time. Before returning results, edits them to include
     * statistical data.
     * @param queryData An SseQueryData object containing all of the reuired data about the query to execute.
     * @return The result of this iteration of the query.
     */
    private QueryResult runSingleIteration(List<SseQueryData> queries, CopiedFilterState copiedFilterState) {
        TabularQueryResult finalResult = new TabularQueryResult()
        for(int x = 0; x < queries.size(); x++) {
            SseQueryData queryData = queries.get(x)
                //long startTime = System.currentTimeMillis()
            applyRandomFilter(queryData.query, queryData.randMin, queryData.randMax)

            QueryOptions queryOptions = new QueryOptions(ignoreFilters: queryData.ignoreFilters, selectionOnly: queryData.selectionOnly, ignoredFilterIds: (queryData.ignoredFilterIds ?: ([] as Set)))
            QueryExecutor queryExecutor = queryExecutorFactory.getExecutor(new ConnectionInfo(host: queryData.host, dataSource: queryData.databaseType as DataSources))
            QueryResult result = queryExecutor.execute(queryData.query, queryOptions, copiedFilterState)
            updateResults(result, queryData)
                //long endTime = System.currentTimeMillis()
                //long elapsedTime = endTime - startTime
            queryData.randMin += queryData.randStep
                //queryData.randStep *= (MAX_ITERATION_TIME / elapsedTime)
            queryData.randMax = (queryData.randMax + queryData.randStep > 1) ? 1 : queryData.randMax + queryData.randStep
            if(queryData.randMin > 1) {
                queryData.complete = true
            }
            finalResult.data.addAll(result.data)
        }
        return finalResult
    }

    /**
     * Adds the results from one iteration of a query to the overall statistical results for that query, and
     * alters the results object from that iteration to include the statistical results instead of just
     * the results from that iteration.
     *
     * TODO - what does it even mean when a query has multiple aggregates clauses? How does that work and how should it be handled?
     *
     * TODO also - This needs some re-vamping in the future - on very large data sets it's possible that keeping a running sum of square of results would overflow or something.
     *
     * @param result The results from a single iteration of a query.
     * @param The query that returned the given results object.
     */
    private void updateResults(QueryResult result, SseQueryData queryData) {
        switch(queryData.query.aggregates[0].operation) {
            case 'count':
                AggregationOperations.count(result, queryData)
                break
            case 'sum':
                AggregationOperations.sum(result, queryData)
                break
            case 'avg':
                break
            case 'min':
                break
            case 'max':
                break
            deault:
                throw new UnsupportedOperationException("Don't know how to aggregate on operation ${queryData.query.aggregates[0].operation}.")
        }
    }

    private String addToRunningQueries(List queryData) {
        String uuid  = UUID.randomUUID().toString()
        while(CURRENTLY_RUNNING_QUERIES_DATA[uuid]) { // If the uuid is already in use, make a new one.
            uuid = UUID.randomUUID().toString()
        }
        CURRENTLY_RUNNING_QUERIES_DATA.put(uuid, queryData)
        return uuid
    }

    /**
     * Initializes the step/iteration values for a given collection. Gets the number of records in that
     * collection and sets the intial step, min, and max values for the random value field accordingly.
     * @param host The host on which the collection is stored.
     * @param databaseType The type of database on which the collection is stored (converted to a <code>com.ncc.neon.connect.Datasources</code>).
     * @param databaseName The name of the database storing the collection to get values for.
     * @param tableName The name of the collection to get values for.
     * @param ignoreFilters Whether or not to ignore filters.
     * @param selectionOnly Whether or not to only return for ma selection.
     * @param ignoredFilterIds The IDs of the filters to ignore.
     * @param query The query this data is associated with.
     * @return A map, of the form [active: true,
     *                             complete: false,
     *                             count: number of records in collection,
     *                             randStep: initial step value for random field,
     *                             randMin: initial lower bound for random field,
     *                             randMax: initial upper bound for random field
     *                             traversed: 0,
     *                             host: host parameter,
     *                             databaseType: databaseType parameter,
     *                             ignoreFilters: ignoreFilters parameter,
     *                             selectionOnly: selectionOnly parameter,
     *                             ignoredFilterIds: ignoredFilterIds parameter,
     *                             query: query parameter]
     * coerced to a com.ncc.neon.sse.SseQueryData object.
     */
    private SseQueryData initDataWithCollectionValues(String host,
                                                      String databaseType,
                                                      String databaseName,
                                                      String tableName,
                                                      boolean ignoreFilters,
                                                      boolean selectionOnly,
                                                      Set ignoredFilterIds,
                                                      Query query) {
        RecordCounter counter = recordCounterFactory.getRecordCounter(databaseType)
        long recordCount = counter.getCount(host, databaseName, tableName)
        double initialIterations = recordCount / (INITIAL_RECORDS_PER_ITERATION as double)
        double randStep = 1.0 / initialIterations
        return [active: true,
                complete: false,
                count: recordCount,
                randStep: randStep,
                randMin: 0.0D,
                randMax: 0.0D + randStep,
                traversed: 0L,
                host: host,
                databaseType: databaseType,
                ignoreFilters: ignoreFilters,
                selectionOnly: selectionOnly,
                ignoredFilterIds: ignoredFilterIds,
                query: query,
                zp: getZp(0.95)] as SseQueryData // TODO - currently z(p) is constant. Eventually we will want to pass in a confidence parameter and get z(p) from that.
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
            AndWhereClause newClause = new AndWhereClause(whereClauses: [])
            newClause.whereClauses << currentClause
            newClause.whereClauses << ([lhs: RANDOM_FIELD_NAME, operator: '>=', rhs: randMin] as SingularWhereClause)
            newClause.whereClauses << ([lhs: RANDOM_FIELD_NAME, operator: '<', rhs: randMax] as SingularWhereClause)
            query.filter.whereClause = newClause
        }
        else {
            boolean minClause = false, maxClause = false
            for(int x = 0; x < currentClause.whereClauses.size(); x++) {
                WhereClause clause = currentClause.whereClauses.get(x)
                if(clause instanceof SingularWhereClause && clause.lhs == RANDOM_FIELD_NAME && clause.operator == '>=') {
                    clause.rhs = randMin
                    minClause = true
                }
                else if(clause instanceof SingularWhereClause && clause.lhs == RANDOM_FIELD_NAME && clause.operator == '<') {
                    clause.rhs = randMax
                    maxClause = true
                }
            }
            if(!minClause) {
                currentClause.whereClauses << ([lhs: RANDOM_FIELD_NAME, operator: '>=', rhs: randMin] as SingularWhereClause)
            }
            if(!maxClause) {
                currentClause.whereClauses << ([lhs: RANDOM_FIELD_NAME, operator: '<', rhs: randMax] as SingularWhereClause)
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
    @Path("test/{name}")
    EventOutput testSse(@PathParam("name") String name) {
        final EventOutput OUTPUT = new EventOutput()
        Thread.start {
            try {
                for(int x = 0; x < 5; x++) {
                    Map m = [key: "The value is ${x}, ${name}."]
                    OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(m)).build())
                    Thread.sleep(1000)
                }
                OUTPUT.write(new OutboundEvent.Builder().data(String, "[\"complete\"]").build())
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
