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
import com.ncc.neon.sse.AggregationOperations
import com.ncc.neon.sse.RecordCounter
import com.ncc.neon.sse.RecordCounterFactory
import com.ncc.neon.sse.SseQueryData

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

/**
 * Asynchronously executes queries against a generic data store, and periodically returns estimated results.
 */
@Component
@Path("/ssequeryservice")
class SseQueryService {

    @Autowired
    RecordCounterFactory recordCounterFactory

    @Autowired
    GlobalFilterState filterState

    @Autowired
    QueryExecutorFactory queryExecutorFactory

    // Maximum time a single iteration of the aggregation should be allowed to take, in milliseconds.
    private static final int MAX_ITERATION_TIME = 1000

    // Number of records to be looked through on the first iteration.
    private static final int INITIAL_RECORDS_PER_ITERATION = 10000

    // Name of the random value field in data sets. TODO this should eventually be passed in as a part of queries, rather than hard-coded like this.
    private static final String RANDOM_FIELD_NAME= 'rand'

    // Map of all currently stored queries and query groups, packaged into lists of SseQueryData objects.
    private static final Map CURRENTLY_RUNNING_QUERIES_DATA = new ConcurrentHashMap<String, List<SseQueryData>>()

    // Map associating Json-string representations of queries and query groups with the UUIDs that are their keys in CURRENTLY_RUNNING_QUERIES_DATA
    private static final Map QUERIES_TO_UUIDS = new ConcurrentHashMap<String, String>()

    // Logger, for logging.
    private static final Logger LOGGER = LoggerFactory.getLogger(SseQueryService)

    /**
     * Takes a query and stores it in an SseQueryData object along with relevant information about it, then
     * puts it into the query-and-querygroup map and returns a UUID that can be used to access it.
     * @param host The name of the host on which the database being queried is located.
     * @param databaseType The type of database being queried.
     * @param ignoreFilters Whether or not the query should ignore filters.
     * @param selectionOnly Whether or not the query is selection-only.
     * @param ignoredFilterIds The IDs of filters to ignore, if any.
     * @param query The query to store.
     * @return The UUID that can be used to activate and receive updates from this query in the future.
     */
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

    /**
     * Takes a query group and stores each query in an SseQueryData object along with relevant information about it, then
     * puts them into the query-and-querygroup map and returns a UUID that can be used to access them.
     * @param host The name of the host on which the database being queried is located.
     * @param databaseType The type of database being queried.
     * @param ignoreFilters Whether or not the query group should ignore filters.
     * @param selectionOnly Whether or not the query group is selection-only.
     * @param ignoredFilterIds The IDs of filters to ignore, if any.
     * @param query The query group to store.
     * @return The UUID that can be used to activate and receive updates from this query group in the future.
     */
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

    /**
     * Begins or resumes execution of a query or query group, and returns the results over time using an EventOutput. The EventOutput
     * will send updates in "message" events, a notice that the query or query group has been halted in "cancel" events, and a notice
     * that the query or query group has been completed in a "done" event.
     * @param uuid The UUID corresponding to the query or query group to execute.
     * @return An EventOutput that will deliver updates on the progress of execution over time.
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @Path("getupdates/{uuid}")
    EventOutput executeByUuid(@PathParam("uuid") String uuid) {
        List<SseQueryData> queries = CURRENTLY_RUNNING_QUERIES_DATA[uuid]
        if(queries) {
            for(int x = 0; x < queries.size(); x++) {
                queries.get(x).active = true
            }
        }
        return threadOnlineQueryOrGroup(queries)
    }

    /**
     * Halts execution of a query or query group by setting them to inactive. This allows them to be resumed at a later time.
     * @param uuid The UUID corresponding to the query or query group to halt.
     * @return A 200 OK response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("cancel/{uuid}")
    Response cancelUuid(@PathParam("uuid") String uuid) {
        List<SseQueryData> queries = CURRENTLY_RUNNING_QUERIES_DATA[uuid]
        if(queries) {
            for(int x = 0; x < queries.size(); x++) {
                queries.get(x).active = false
            }
        }
        return Response.ok().build()
    }

    /**
     * Creates a new thread to run a query or group of queries, given the UUID associated with that query or group of queries.
     * Returns an SSE event output, through which updates as to the progress of the query or group will be sent.
     * @param uuid The UUID associated with the query or group of queries to be run, used to find the query or group in the map of stored queries.
     * @return An SSE event stream that will be used to pass updates to the client.
     */
    private EventOutput threadOnlineQueryOrGroup(List<SseQueryData> queries) {
        final EventOutput OUTPUT = new EventOutput()
        CopiedFilterState copiedFilterState = new CopiedFilterState(filterState)
        LOGGER.debug("Starting SSE Thread")
        Thread.start {
            try {
                def (boolean allComplete, boolean allActive) = checkActiveAndComplete(queries)
                while(!allComplete) {
                    if(!allActive || queries == null) {
                        OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson([])).id("cancel").build())
                        return
                    }
                    Map result = runSingleIteration(queries, copiedFilterState)
                    OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson(result)).id("update").build())
                    (allComplete, allActive) = checkActiveAndComplete(queries)
                }
                OUTPUT.write(new OutboundEvent.Builder().data(String, JsonOutput.toJson([])).id("done").build()) // Should format/send final results here for if someone requests an already-done query.
            }
            catch(final InterruptedException | IOException e) {
                LOGGER.warn("EventOutput had an error.\n$e")
            }
            finally {
                OUTPUT.close()
            }
        }
        return OUTPUT
    }

    /**
     * Checks a list of queries to determine whether or not they are all active and whether or not they are all complete.
     * @param queries The list of queries to check for activity and completion.
     * @return A list containing two boolean values, one that says whether or not all the given queries
     *      are complete and another that says whether or not all the queries are active.
     */
    private List checkActiveAndComplete(List<SseQueryData> queries) {
        boolean completed = true
        boolean active = true
        for(SseQueryData queryData : queries) { // Will set allComplete or allActive ot false if at least one query has complete or active value == false
            completed = completed && queryData.complete
            active = active && queryData.active
        }
        return [completed, active]
    }

    /**
     * Runs a single iteration of a query or group of queries, recording the time it takes and accordingly adjusting
     * how large a segment of records should be looked through on the next iteration. Before returning results, edits them to include
     * statistical data.
     * @param queryData An SseQueryData object containing all of the reuired data about the query to execute.
     * @return The estimated results, taking into account every iteration of the query up to the current one.
     */
    private Map runSingleIteration(List<SseQueryData> queries, CopiedFilterState copiedFilterState) {
        Map finalResult = [data: []]
        long startTime = System.currentTimeMillis()
        for(int x = 0; x < queries.size(); x++) {
            SseQueryData queryData = queries.get(x)
            applyRandomFilter(queryData.query, queryData.randMin, queryData.randMax)
            QueryOptions queryOptions = new QueryOptions(ignoreFilters: queryData.ignoreFilters, selectionOnly: queryData.selectionOnly, ignoredFilterIds: (queryData.ignoredFilterIds ?: ([] as Set)))
            QueryExecutor queryExecutor = queryExecutorFactory.getExecutor(new ConnectionInfo(host: queryData.host, dataSource: queryData.databaseType as DataSources))
            QueryResult result = queryExecutor.execute(queryData.query, queryOptions, copiedFilterState)
            List oneQueryResult = updateResults(result, queryData)
            queryData.randMin += queryData.randStep
            if(queryData.randMin > 1) {
                queryData.complete = true
                queryData.active = false
            }
            finalResult.data.addAll(oneQueryResult)
        }
        long endTime = System.currentTimeMillis()
        long elapsedTime = endTime - startTime
        for(int x = 0; x < queries.size(); x++) {
            SseQueryData queryData = queries.get(x)
            queryData.randStep *= (MAX_ITERATION_TIME / elapsedTime)
            queryData.randMax = (queryData.randMax + queryData.randStep > 1) ? 1 : queryData.randMax + queryData.randStep
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
     * @param result The results from a single iteration of a query.
     * @param The query that returned the given results object.
     */
    private List updateResults(QueryResult result, SseQueryData queryData) {
        switch(queryData.query.aggregates[0].operation) {
            case 'count':
                return AggregationOperations.count(result, queryData)
            case 'sum':
                return AggregationOperations.sum(result, queryData)
            case 'avg':
            case 'min':
            case 'max':
            deault:
                throw new UnsupportedOperationException("Don't know how to aggregate on operation ${queryData.query.aggregates[0].operation}.")
        }
    }

    /**
     * Adds a query or query group to the list of queries and query groups. Does this by turning the query or query group into a string and checking for it
     * in a map of strings to UUIDs. If it is not found, generates a random UUID, maps the string to it in the string-to-uuid map, and maps it to the query
     * or query group in a second map. If it is found, simply returns the UUID already associated with it.
     *
     * This allows the returning of a UUID through which the query or query group can be called (because UUIDs are fixed-length, and simply returning the query
     * string itself as an identifier may not work for large queries due to the limitations of GET used to trigger the query's activation) while also allowing
     * for the easy finding of queries/query groups by string--useful for checking if a query has already been added to the second map.
     * @param queryData A list of SseQueryData objects contaoining information about a query or group of queries.
     * @return The string form of a UUID that is linked to the query or guery group that was passed in.
     */
    private String addToRunningQueries(List queryData) {
        String uuid
        String queryString = ''
        for(int x = 0; x < queryData.size(); x++) {
            queryString += JsonOutput.toJson(queryData.get(x).query)
        }
        if(!QUERIES_TO_UUIDS[queryString]) {
            uuid = UUID.randomUUID().toString()
            QUERIES_TO_UUIDS.put(queryString, uuid)
            CURRENTLY_RUNNING_QUERIES_DATA.put(uuid, queryData)
        }
        else {
            uuid = QUERIES_TO_UUIDS[queryString]
        }
        return uuid
    }

    /**
     * Initializes the step/iteration values for a given collection. Gets the number of records in that
     * collection and sets the intial step, min, and max values for the random value field accordingly.
     * @param host The host on which the collection is stored.
     * @param databaseType The type of database on which the collection is stored (converted to a com.ncc.neon.connect.Datasources).
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
     *                             randMax: initial upper bound for random field,
     *                             host: host parameter,
     *                             databaseType: databaseType parameter,
     *                             ignoreFilters: ignoreFilters parameter,
     *                             selectionOnly: selectionOnly parameter,
     *                             ignoredFilterIds: ignoredFilterIds parameter,
     *                             query: query parameter
     *                             zp: the z(p) associated with the desired confidence of the query]
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
                host: host,
                databaseType: databaseType,
                ignoreFilters: ignoreFilters,
                selectionOnly: selectionOnly,
                ignoredFilterIds: ignoredFilterIds,
                query: query,
                zp: getZp(0.95)] as SseQueryData // TODO - Eventually we will want to pass in a confidence parameter and get z(p) from that.
    }

    /**
     * Gets the z(p) value that corresponds to a given requested confidence value.
     */
    private double getZp(double confidence) {
        NormalDistribution norm = new NormalDistribution(0, 1)
        double requiredVal = (confidence + 1) / 2
        for(double x = 0; x < 10; x += 0.01) {
            if(norm.cumulativeProbability(x) > requiredVal) {
                return x
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
    private void applyRandomFilter(Query query, double randMin, double randMax) {
        WhereClause currentClause = query.filter.whereClause
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
